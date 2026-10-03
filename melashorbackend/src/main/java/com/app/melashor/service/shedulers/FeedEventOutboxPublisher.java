package com.app.melashor.service.shedulers;

import com.app.melashor.domain.OutboxEventStatus;
import com.app.melashor.domain.model.OutBoxEvent;
import com.app.melashor.repositories.OutboxEventRepository;
import com.app.melashor.service.FeedMetricsService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.print.Pageable;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(
        value = "feed.async.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@Getter
public class FeedEventOutboxPublisher {

    private final StringRedisTemplate redisTemplate;
    private final OutboxEventRepository outboxEventRepository;
    private final String streamKey;
    private final int publishBatchSize;
    private final int maxAttempt;
    private final FeedMetricsService feedMetricsService;

    public FeedEventOutboxPublisher(
            StringRedisTemplate redisTemplate,
            OutboxEventRepository outboxEventRepository,
            FeedMetricsService feedMetricsService,
            @Value("${feed.async.stream.key}")
            String streamKey,
            @Value("${feed.publisher.batch-size}")
            int publishBatchSize,
            @Value("${feed.publisher.max-attempt}")
            int maxAttempt
    ) {
        this.redisTemplate = redisTemplate;
        this.outboxEventRepository = outboxEventRepository;
        this.streamKey = streamKey;
        this.publishBatchSize = publishBatchSize;
        this.maxAttempt = maxAttempt;
        this.feedMetricsService = feedMetricsService;
    }

    @Scheduled(fixedDelayString = "${feed.aync.publisher.fixed-delayed-ms}")
    @Transactional
    public void publishedPendingEvents() {
        List<OutBoxEvent> pendingEvents = outboxEventRepository
                .findByStatusAndNextAttemptTimeLessThanEqualOrderByCreatedAtAsc(
                        OutboxEventStatus.PENDING,
                        Instant.now(),
                        (Pageable) PageRequest.of(0, publishBatchSize));

        for (OutBoxEvent event : pendingEvents) {
            try {
                Map<String, String> fields = new HashMap<>();
                fields.put("eventId", event.getId());
                fields.put("eventType", event.getEventType().name());
                fields.put("postId", event.getPostId());
                fields.put("authorId", event.getAuthorId());
                fields.put("hotUser", String.valueOf(event.isHotUser()));
                fields.put("occurredAtEpochMillis", String.valueOf(event.getEventTime().toEpochMilli()));


                redisTemplate.opsForStream().add(MapRecord.create(streamKey, fields));
                event.markPublished();

            } catch (Exception e) {
                long delayedSeconds = getDelaySecond(event);
                event.scheduleRetry(Instant.now().plusSeconds(delayedSeconds), maxAttempt);
                feedMetricsService.recordServiceError("publish_outbox_event", "REDIS_ERROR");
            }
        }
    }

    private static long getDelaySecond(OutBoxEvent event) {
        int nextAttempt = event.getAttemptCount() + 1;
        return Math.min(300, 1L << Math.min(10, nextAttempt));
        //<< means shift the binary bits to the left.
    }

}

