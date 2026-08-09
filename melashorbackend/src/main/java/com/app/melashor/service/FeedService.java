package com.app.melashor.service;

import com.app.melashor.domain.dto.record.TimeLinePageResponse;

import java.util.Set;

public interface FeedService {
    TimeLinePageResponse getHomeFeed(String userId, String cursor, String limit);

    record VisibleAuthors(Set<String> allAuthorIds, Set<String> hotAuthorIds, Set<String> nonHotAuthorIds) {
    }
}
