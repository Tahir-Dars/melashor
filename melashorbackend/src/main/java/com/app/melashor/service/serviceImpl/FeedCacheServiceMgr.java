package com.app.melashor.service.serviceImpl;

import com.app.melashor.domain.dto.record.FeedItemResponse;
import com.app.melashor.domain.dto.record.TimeLinePageResponse;
import com.app.melashor.service.FeedCacheService;
import com.app.melashor.service.FeedMetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
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
    private final FeedCursorCodecMgr codecMgr;

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

    @Override
    public void cacheHomeFeed(TimeLinePageResponse pageResponse) {
        writeHomeFeed(pageResponse);
        metricsService.recordCacheMutation("write_first_page");
    }

    @Override
    public void evictHomeFeed(String userId) {
        try {
            redisTemplate.delete(homeFeedKey(userId));
            metricsService.recordCacheMutation("evict");
        } catch (Exception e) {
            log.error("Error occurred while processing request", e);
        }

    }

    @Override
    public void prependToHomeFeed(String userId, FeedItemResponse item) {
        try {
            Optional<TimeLinePageResponse> cacheFeed = getHomeFeed(userId);
            if (cacheFeed.isEmpty()) {
                return;
            }
            TimeLinePageResponse existing = cacheFeed.get();

            List<FeedItemResponse> updatedItems = new ArrayList<>();
            updatedItems.add(item);

            existing.feedItems().stream().filter(
                    existingItem -> !existingItem.postId().equals(item.postId())
            ).forEach(updatedItems::add);

            if (updatedItems.size() > DEFAULT_PAGE_SIZE) {
                updatedItems = updatedItems.subList(0, DEFAULT_PAGE_SIZE);
            }

            writeHomeFeed(new TimeLinePageResponse(
                    existing.timelineOwnerId(),
                    updatedItems,
                    existing.mode(),
                    existing.totalItems(),
                    existing.totalItems() + 1 > updatedItems.size()
                            && !updatedItems.isEmpty() ? codecMgr.encode(updatedItems.getLast()) : null
            ));

            metricsService.recordCacheMutation("prepend");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void writeHomeFeed(TimeLinePageResponse timeLinePageResponse) {
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
