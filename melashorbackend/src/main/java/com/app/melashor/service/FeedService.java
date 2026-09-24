package com.app.melashor.service;

import com.app.melashor.domain.dto.record.TimeLinePageResponse;
import com.app.melashor.domain.dto.record.UserProfileResponse;

import java.util.List;

public interface FeedService {
    TimeLinePageResponse getHomeFeed(String userId, String cursor, String limit);

    TimeLinePageResponse getUserFeed(String userId, String cursor, int limit);

    List<UserProfileResponse> getUser();
}
