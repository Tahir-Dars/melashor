package com.app.melashor.service;

public interface FeedMetricsService {
    long startTime();
    void recordHomeFeedRequest(
            long startAtNanos, String cacheOutcome,
            String mergeMode, boolean hasNextCursor, int itemsReturned
    );
    void recordHomeFeedRequestedPageSize(int requestedLimit, int normalizedPageSize);
    void recordUserFeedRequest(long startAtNanos,boolean hasNextCursor, int itemsReturned);
    public void recordUserFeedRequestedPageSize(int requestedLimit, int normalizedPageSize);
    void recordHomeFeedCacheLookup(String outcome);
}
