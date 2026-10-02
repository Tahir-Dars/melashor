package com.app.melashor.service.shedulers;

import com.app.melashor.repositories.OutboxEventRepository;
import com.app.melashor.service.FeedMetricsService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

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
    private final int nextAttempt;
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
            int nextAttempt
    ) {
        this.redisTemplate = redisTemplate;
        this.outboxEventRepository = outboxEventRepository;
        this.streamKey = streamKey;
        this.publishBatchSize = publishBatchSize;
        this.nextAttempt = nextAttempt;
        this.feedMetricsService = feedMetricsService;
    }

}

