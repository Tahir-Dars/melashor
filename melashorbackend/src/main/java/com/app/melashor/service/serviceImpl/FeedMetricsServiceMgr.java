package com.app.melashor.service.serviceImpl;

import com.app.melashor.service.FeedMetricsService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class FeedMetricsServiceMgr implements FeedMetricsService {

    private final MeterRegistry meterRegistry;

    @Override
    public long startTime() {
        return System.nanoTime();
    }

    @Override
    public void recordHomeFeedRequest(
            long startAtNanos, String cacheOutcome,
            String mergeMode, boolean hasNextCursor, int itemsReturned
    ) {
        Timer.builder("feedme.feed.home.latency")
                .description("Home feed request latency")
                .tag("cache_outcome", cacheOutcome)
                .tag("merge_mode", mergeMode)
                .register(meterRegistry)
                .record(System.nanoTime() - startAtNanos, TimeUnit.NANOSECONDS);

        Counter.builder("feedme.feed.home.requests")
                .description("Home feed requests")
                .tag("cache_outcome", cacheOutcome)
                .tag("merge_mode", mergeMode)
                .register(meterRegistry)
                .increment();

        DistributionSummary.builder("feedme.feed.home.items_returned")
                .description("Number of items returned to home feed pages")
                .baseUnit("items")
                .register(meterRegistry)
                .record(itemsReturned);

        if (hasNextCursor) {
            meterRegistry.counter("feedme.feed.home.next_cursor").increment();
        }
    }

    @Override
    public void recordHomeFeedRequestedPageSize(int requestedLimit, int normalizedPageSize) {

        DistributionSummary.builder("feedme.feed.home.requested_page_size")
                .description("Requested and normalized home feed pages feed")
                .baseUnit("items")
                .tag("requested_limit", String.valueOf(requestedLimit))
                .register(meterRegistry)
                .record(normalizedPageSize);
    }

    @Override
    public void recordUserFeedRequest(long startAtNanos, boolean hasNextCursor, int itemsReturned) {
        Timer.builder("feedme.feed.user.latency")
                .description("User feed request latency")
                .register(meterRegistry)
                .record(System.nanoTime() - startAtNanos, TimeUnit.NANOSECONDS);

        Counter.builder("feedme.feed.user.requests")
                .description("User feed requests")
                .register(meterRegistry)
                .increment();

        DistributionSummary.builder("feedme.feed.user.items_returned")
                .description("Number of items returned to user feed pages")
                .baseUnit("items")
                .register(meterRegistry)
                .record(itemsReturned);

        if (hasNextCursor) {
            meterRegistry.counter("feedme.feed.user.next_cursor").increment();
        }
    }
    @Override
    public void recordUserFeedRequestedPageSize(int requestedLimit, int normalizedPageSize) {

        DistributionSummary.builder("feedme.feed.user.requested_page_size")
                .description("Requested and normalized user feed pages feed")
                .baseUnit("items")
                .tag("requested_limit", String.valueOf(requestedLimit))
                .register(meterRegistry)
                .record(normalizedPageSize);
    }
}
