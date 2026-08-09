package com.app.melashor.service;

import com.app.melashor.domain.dto.record.TimeLinePageResponse;

import java.util.Optional;

public interface FeedCacheService {
    Optional<TimeLinePageResponse> getHomeFeed(String userId);
}
