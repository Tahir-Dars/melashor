package com.app.melashor.service.serviceImpl;

import com.app.melashor.domain.dto.record.TimeLinePageResponse;
import com.app.melashor.service.FeedCacheService;
import com.app.melashor.service.FeedMetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedCacheServiceMgr implements FeedCacheService {
    public static final int DEFAULT_PAGE_SIZE = 5;
    private static final Duration HOME_FEED_TTL = Duration.ofMinutes(5);

    private final StringRedisTemplate redisTemplate;
    private final FeedMetricsService metricsService;
    private final ObjectMapper objectMapper;

    @Override
    public Optional<TimeLinePageResponse> getHomeFeed(String userId) {
        try {
            String payload = redisTemplate.opsForValue().get(homeFeedKey(userId));
            if (payload == null) {
                return Optional.empty();
            }

            return Optional.of(objectMapper.readValue(payload, TimeLinePageResponse.class));

        } catch (Exception e) {
            log.info("Something is not OK with REDIS: {},{}", userId, e.getMessage());
            return Optional.empty();
        }
    }

    private String homeFeedKey(String userId) {
        return "feed:home " + userId;
    }

    private void cacheHomeFeed(TimeLinePageResponse timeLinePageResponse) {
        try {
            String payload = objectMapper.writeValueAsString(timeLinePageResponse);
            redisTemplate.opsForValue().set(homeFeedKey
                    (timeLinePageResponse.timelineOwnerId()), payload, HOME_FEED_TTL);
        } catch (Exception e) {
            log.info("Something is wrong with the redis on fetching userId: {},{} ", timeLinePageResponse.timelineOwnerId(), e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
