package com.app.melashor.service.shedulers;

import com.app.melashor.config.FeedAsyncProperties;
import com.app.melashor.domain.model.DeadEventFeedEvent;
import com.app.melashor.domain.model.FeedEventFailure;
import com.app.melashor.domain.model.ProcessedFeedEvents;
import com.app.melashor.repositories.DeadLetterFERepository;
import com.app.melashor.repositories.FeedEventFailureRepository;
import com.app.melashor.repositories.ProcessedFeedEventsRepository;
import com.app.melashor.service.FeedMetricsService;
import com.app.melashor.service.FeedService;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(
        value = "feed.async.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@Getter
@RequiredArgsConstructor
public class FeedEventWorker {

    private final StringRedisTemplate redisTemplate;
    private final ProcessedFeedEventsRepository processedFeedEventsRepository;
    private final DeadLetterFERepository deadLetterFERepository;
    private final FeedEventFailureRepository feedEventFailureRepository;

    private final FeedService feedService;
    private final FeedMetricsService metricsService;
    private final ObjectMapper objectMapper;

    private final FeedAsyncProperties feedAsyncProperties;


    @PostConstruct
    public void initializeConsumerGroup() {
        try {
            String streamKey = feedAsyncProperties.getStream().getKey();
            String consumerGroup = feedAsyncProperties.getConsumer().getGroup();
            StreamInfo.XInfoGroups groups = redisTemplate
                    .opsForStream().groups(feedAsyncProperties.getStream().getKey());

            boolean exists = groups.stream().anyMatch(group ->
                    feedAsyncProperties.getConsumer().getGroup().equals(group.groupName()));
            if (!exists) {
                redisTemplate.opsForStream().createGroup(streamKey, ReadOffset.latest(), consumerGroup);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Scheduled(fixedDelayString = "${feed.async.consumer.fixed-delay-ms}")
    @Transactional
    public void consumePostEvents() {
        List<MapRecord<String, Object, Object>> records;
        try {
            records = redisTemplate.opsForStream().read(
                    Consumer.from(
                            feedAsyncProperties.getConsumer().getGroup(),
                            feedAsyncProperties.getConsumer().getName()
                    ),
                    StreamReadOptions.empty().count(feedAsyncProperties.getConsumer().getBatchSize()),
                    StreamOffset.create(feedAsyncProperties.getStream().getKey(), ReadOffset.lastConsumed())
            );
        } catch (Exception e) {
            metricsService.recordServiceError(
                    "consume_post_event",
                    "REDIS_READ_ERROR"
            );
            return;
        }
        if (records == null || records.isEmpty()) {
            return;
        }
        for (MapRecord<String, Object, Object> record : records) {
            process(record, "consume_post_event");
        }

    }

    private void process(MapRecord<String, Object, Object> record, String consumePostEvent) {
    }

    @Scheduled(fixedDelayString = "${feed.async.consumer.recover-fixed-delay-ms}")
    @Transactional
    public void recoverPendingPostEvents() {
        ;
        List<MapRecord<String, Object, Object>> pendingRecords;
        try {
            pendingRecords = redisTemplate.opsForStream().read(
                    Consumer.from(
                            feedAsyncProperties.getConsumer().getGroup(),
                            feedAsyncProperties.getConsumer().getName()
                    ),
                    StreamReadOptions.empty().count(feedAsyncProperties.getConsumer().getBatchSize()),
                    StreamOffset.create(feedAsyncProperties.getStream().getKey(), ReadOffset.from("0")));

        } catch (Exception e) {
            metricsService.recordServiceError(
                    "recover_post_event",
                    "REDIS_READ_ERROR"
            );
            return;
        }
        if (pendingRecords == null || pendingRecords.isEmpty()) {
            return;
        }
        for (MapRecord<String, Object, Object> record : pendingRecords) {
            processRecord(record, "recover_post_event");
        }
    }

    @Scheduled(fixedDelayString = "${feed.async.consumer.reclaim-fixed-delay-ms}")
    @Transactional
    public void reclaimPendingPostEvents() {
        if (!feedAsyncProperties.getConsumer().isReclaim()) {
            return;
        }
        List<MapRecord<String, Object, Object>> claimRecords = claimStalePendingRecords();
        for (MapRecord<String, Object, Object> record : claimRecords) {
            processRecord(record, "reclaim_post_event");
        }
    }

    private void processRecord(MapRecord<String, Object, Object> record, String operationName) {
        String streamKey = feedAsyncProperties.getStream().getKey();
        String consumerGroup = feedAsyncProperties.getConsumer().getGroup();

        Map<Object, Object> eventFields = record.getValue();


        String eventId = String.valueOf(eventFields.get("eventId"));
        String postId = String.valueOf(eventFields.get("postId").toString());

        if (eventId == null && postId == null) {
            redisTemplate.opsForStream().acknowledge(streamKey, consumerGroup, record.getId());
            metricsService.recordServiceError(operationName, "MALFORMED_EVENT");
        }
        try {
            try {
                processedFeedEventsRepository.saveAndFlush(new ProcessedFeedEvents(eventId));
            } catch (DataIntegrityViolationException duplicateEvent) {
                redisTemplate.opsForStream().acknowledge(streamKey, consumerGroup, record.getId());
                return;
            }
            feedService.processPostCreationEvent(postId);
            clearFailureState(eventId);

            redisTemplate.opsForStream().acknowledge(streamKey, consumerGroup, record.getId());
        } catch (Exception exception) {
            int attemptCount = recordFailureAttempt(eventId, exception);
            metricsService.recordServiceError(operationName, "PROCESS_ERROR");
            if (attemptCount < feedAsyncProperties.getPublisher().getMaxAttempt()) {
                metricsService.recordAsyncWorkerRetry(operationName);
            }

            movedToDeadLetter(record, eventFields, eventId, attemptCount, exception, operationName);
            redisTemplate.opsForStream().acknowledge(streamKey, consumerGroup, record.getId());
            feedEventFailureRepository.deleteById(eventId);
            metricsService.recoverAsyncWorkerDeadLetter(operationName);
        }
    }

    private void clearFailureState(String eventId) {
        feedEventFailureRepository.deleteById(eventId);
    }

    private void movedToDeadLetter(MapRecord<String, Object,
                                           Object> record, Map<Object, Object> eventFields,
                                   String eventId, int attemptCount,
                                   Exception exception, String operationName) {
        String payloadJson = toJson(eventFields);
        String failureReason = sanitizeExceptionMessage(exception);

        deadLetterFERepository.save(new DeadEventFeedEvent(attemptCount, failureReason, payloadJson,
                record.getId().getValue(), eventId));

        try {
            redisTemplate.opsForStream().add(MapRecord.create(
                    feedAsyncProperties.getConsumer().getDlqStreamKey(),
                    Map.of(
                            "eventId", eventId,
                            "sourceRecordId", record.getId().getValue(),
                            "failureReason", failureReason,
                            "attemptCount", attemptCount,
                            "payloadJson", payloadJson
                    )
            ));
        } catch (Exception redisException) {
            metricsService.recordServiceError(
                    operationName,
                    "DLQ_STREAM_PUBLISH_ERROR"
            );
        }
    }

    private String toJson(Map<Object, Object> eventFields) {
        try {
            return objectMapper.writeValueAsString(eventFields);
        } catch (Exception e) {
            return "{\"serialization\":true}";
        }
    }

    private int recordFailureAttempt(String eventId, Exception exception) {
        FeedEventFailure failure = feedEventFailureRepository.findById(eventId)
                .orElseGet(FeedEventFailure::new);
        int attemptCount = failure.recordFailures(sanitizeExceptionMessage(exception));
        feedEventFailureRepository.saveAndFlush(failure);
        return attemptCount;
    }

    private String sanitizeExceptionMessage(Exception exception) {
        String rawMessage = exception.getMessage() == null ?
                exception.getClass().getSimpleName() : exception.getMessage();
        return rawMessage.length() > 1000 ? rawMessage.substring(0, 1000) : rawMessage;
    }

    private List<MapRecord<String, Object, Object>> claimStalePendingRecords() {
        String streamKey = feedAsyncProperties.getStream().getKey();
        String consumerGroup = feedAsyncProperties.getConsumer().getGroup();
        String consumerName = feedAsyncProperties.getConsumer().getName();
        long reclaimIdleMs = feedAsyncProperties.getConsumer().getReclaimIdleMs();
        try {
            PendingMessages pendingMessages = redisTemplate.opsForStream()
                    .pending(streamKey,
                            consumerGroup,
                            Range.unbounded(),
                            feedAsyncProperties.getConsumer().getReclaimBatchSize());

            if (pendingMessages.isEmpty()) {
                return List.of();
            }

            List<RecordId> staleRecordIds = pendingMessages.stream().
                    // filter(()"this.isClaimPendingMessage").
                            map(PendingMessage::getId)
                    .toList();

            if (staleRecordIds.isEmpty()) {
                return List.of();
            }
            return redisTemplate.opsForStream().claim(
                    streamKey,
                    consumerGroup,
                    consumerName,
                    Duration.ofMillis(reclaimIdleMs),
                    staleRecordIds.toArray(new RecordId[0])
            );
        } catch (Exception e) {
            metricsService.recordServiceError(
                    "reclaim_post_event",
                    "RECLAIM_CLAIM_ERROR"
            );
        }
        return List.of();
    }

    private boolean isClaimPendingMessage(PendingMessage pendingMessage) {
        if (feedAsyncProperties.getConsumer().getName().equals(pendingMessage.getConsumerName())) {
            return false;
        }

        return pendingMessage.getElapsedTimeSinceLastDelivery().toMillis() >=
                feedAsyncProperties.getConsumer().getReclaimIdleMs();
    }
}
