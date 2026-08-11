package com.app.melashor.service;

import com.app.melashor.domain.dto.record.TimeLinePageResponse;

public interface FeedService {
    TimeLinePageResponse getHomeFeed(String userId, String cursor, String limit);

    TimeLinePageResponse getUserFeed(String userId, String cursor, int limit);
}
