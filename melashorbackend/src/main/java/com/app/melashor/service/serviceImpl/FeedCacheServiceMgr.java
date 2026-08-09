package com.app.melashor.service.serviceImpl;

import com.app.melashor.domain.dto.record.TimeLinePageResponse;
import com.app.melashor.service.FeedCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FeedCacheServiceMgr implements FeedCacheService {
    public static final int DEFAULT_PAGE_SIZE = 5;

    @Override
    public Optional<TimeLinePageResponse> getHomeFeed(String userId) {
        return Optional.empty();
    }
}
