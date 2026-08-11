package com.app.melashor.service.serviceImpl;

import com.app.melashor.domain.dto.TimeLineMode;
import com.app.melashor.domain.dto.record.FeedItemResponse;
import com.app.melashor.domain.dto.record.TimeLinePageResponse;
import com.app.melashor.domain.model.FollowRelationships;
import com.app.melashor.domain.model.Post;
import com.app.melashor.domain.model.UserProfile;
import com.app.melashor.repositories.FollowRelationshipsRepository;
import com.app.melashor.repositories.PostRepository;
import com.app.melashor.repositories.UserProfileRepository;
import com.app.melashor.service.FeedCacheService;
import com.app.melashor.service.FeedCursorCodec;
import com.app.melashor.service.FeedMetricsService;
import com.app.melashor.service.FeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FeedServiceMgr implements FeedService {

    private static final Comparator<FeedItemResponse> FEED_ORDER =
            Comparator.comparing(FeedItemResponse::createdAt)
                    .thenComparing(FeedItemResponse::postId)
                    .reversed();

    private final FeedMetricsService metricsService;
    private final UserProfileRepository userProfileRepository;
    private final FeedCursorCodecMgr codecMgr;
    private final FollowRelationshipsRepository followRelationshipsRepo;
    private final FeedCacheService feedCacheService;
    private final PostRepository postRepository;


    @Override
    public TimeLinePageResponse getHomeFeed(String userId, String cursor, String limit) {
        long startedAtNanos = metricsService.startTime();
        int limitInInt = Integer.parseInt(limit);
        int pageSize = normalizedLimit(limitInInt);
        metricsService.recordHomeFeedRequestedPageSize(limitInInt, pageSize);

        try {
            UserProfile viewer = getUser(userId);
            FeedCursorCodec.FeedCursor pageCursor = codecMgr.parse(cursor);

            VisibleAuthors visibleAuthors = getVisibleAuthors(viewer);
            NormalFeedSliceResult normalFeedSliceResult =
                    getNormalHomeFeedSlice(viewer.getUserId(), visibleAuthors.nonHotAuthorIds(), pageCursor, pageSize);

            FeedSlice hotSlice = getHotHomeFeedSlice(viewer.getUserId(), visibleAuthors.hotAuthorIds(), pageCursor, pageSize);
            int totalItems = Math.toIntExact(postRepository.countByAuthor_IdIn(visibleAuthors.allAuthorIds));

            TimeLinePageResponse pageResponse = mergeHomeFeedSlices(
                    userId, totalItems, pageSize, normalFeedSliceResult.slice, hotSlice
            );

            metricsService.recordHomeFeedRequest(startedAtNanos,
                    normalFeedSliceResult.cacheOutcome,
                    determineMergeMode(normalFeedSliceResult.slice, hotSlice),
                    pageResponse.nextCursor() != null,
                    pageResponse.feedItemResponses().size());
            return pageResponse;
        } catch (ResponseStatusException e) {
            metricsService.recordServiceError("get_home_feed", e.getStatusCode().toString());
            throw e;
        }
    }

    @Override
    public TimeLinePageResponse getUserFeed(String userId, String cursor, int limit) {
        return null;
    }

    private TimeLinePageResponse mergeHomeFeedSlices(String userId, int totalItems, int pageSize, FeedSlice normalSlice, FeedSlice hotSlice) {

        List<FeedItemResponse> mergedItems = new ArrayList<>(pageSize + 1);

        int normalIndex = 0;
        int hotIndex = 0;
        int normalItemsUsed = 0;
        int hotItemsUsed = 0;

        while (mergedItems.size() < pageSize + 1
                && (normalIndex < normalSlice.itemResponses().size()
                || hotIndex < hotSlice.itemResponses().size())) {
            FeedItemResponse nextNormal = normalIndex < normalSlice.itemResponses().size()
                    ? normalSlice.itemResponses().get(normalIndex)
                    : null;
            FeedItemResponse nextH0t = hotIndex < hotSlice.itemResponses().size()
                    ? hotSlice.itemResponses().get(hotIndex)
                    : null;

            if (nextH0t == null || (nextNormal != null && FEED_ORDER.compare(nextNormal, nextH0t) <= 0)) {
                mergedItems.add(nextNormal);
                normalIndex++;
                normalItemsUsed++;
            } else {
                mergedItems.add(nextH0t);
                hotIndex++;
                hotItemsUsed++;
            }

        }
        boolean hasMore = mergedItems.size() > pageSize
                || normalIndex < normalSlice.itemResponses().size()
                || hotIndex < hotSlice.itemResponses().size()
                || normalSlice.hasMore()
                || hotSlice.hasMore();
        List<FeedItemResponse> pageItems = mergedItems.size() > pageSize ? mergedItems.subList(0, pageSize)
                : mergedItems;
        String nextCursor = hasMore && !pageItems.isEmpty()
                ? codecMgr.encode(pageItems.getLast()) : null;

        metricsService.recordHomeFeedMerge(determineMergeMode(normalSlice, hotSlice),
                normalItemsUsed,
                hotItemsUsed);


        return new TimeLinePageResponse(
                userId,
                pageItems,
                TimeLineMode.HOME,
                totalItems,
                nextCursor
        );
    }

    private String determineMergeMode(FeedSlice normalSlice, FeedSlice hotSlice) {
        boolean hasNormal = !normalSlice.itemResponses().isEmpty();
        boolean hasHot = !hotSlice.itemResponses().isEmpty();
        if (hasNormal && hasHot) {
            return "mixed";
        }
        if (hasNormal) {
            return "normal-only";
        }
        if (hasHot) {
            return "Hot Only";
        }
        return "empty";
    }

    private FeedSlice getHotHomeFeedSlice(String userId, Set<String> hotAuthorIds,
                                          FeedCursorCodec.FeedCursor pageCursor, int pageSize) {
        if (hotAuthorIds.isEmpty()) {
            return new FeedSlice(List.of(), false);
        }
        List<Post> posts = fetchHomeFeedPosts(hotAuthorIds, pageCursor, pageSize + 1);

        return buildFeedSlice(posts, pageSize, userId, hotAuthorIds);
    }

    private NormalFeedSliceResult getNormalHomeFeedSlice(String userId, Set<String> nonHotUserIds,
                                                         FeedCursorCodec.FeedCursor pageCursor, int pageSize) {
        if (nonHotUserIds.isEmpty()) {
            FeedSlice slice = new FeedSlice(List.of(), false);
            return new NormalFeedSliceResult(slice, "empty_non_hot");
        }

        Optional<FeedSlice> cacheSlice = getCachedNormalHomeFeedSlice(userId, pageCursor, pageSize);
        if (cacheSlice.isPresent()) {
            return new NormalFeedSliceResult(cacheSlice.get(), "hit");
        }
        List<Post> posts = fetchHomeFeedPosts(nonHotUserIds, pageCursor, pageSize + 1);
        FeedSlice slice = buildFeedSlice(posts, pageSize, userId, nonHotUserIds);
        if (pageCursor == null && pageSize == FeedCacheServiceMgr.DEFAULT_PAGE_SIZE) {
            TimeLinePageResponse pageResponse = new TimeLinePageResponse(
                    userId,
                    slice.itemResponses,
                    TimeLineMode.HOME,
                    Math.toIntExact(postRepository
                            .countByAuthor_IdIn(nonHotUserIds)),
                    slice.hasMore() && !slice.itemResponses.isEmpty()
                            ? codecMgr.encode(slice.itemResponses().getLast()) : null
            );
            feedCacheService.cacheHomeFeed(pageResponse);
        }
        return new NormalFeedSliceResult(slice, "miss");
    }

    private FeedSlice buildFeedSlice(List<Post> posts, int pageSize,
                                     String userId, Set<String> visibleAuthorIds) {
        boolean hasMore = posts.size() > pageSize;

        List<Post> pagePosts = hasMore ? posts.subList(0, pageSize) : posts;

        List<FeedItemResponse> pageItems = pagePosts.stream()
                .map(post -> toFeedItems(post, userId, visibleAuthorIds))
                .toList();
        return new FeedSlice(pageItems, hasMore);
    }

    private FeedItemResponse toFeedItems(Post post, String viewerId,
                                         Set<String> visibleAuthorIds) {

        UserProfile author = post.getAuthor();
        double recencyScore = 1.0 - Duration.between(post
                .getCreatedAt(), Instant.now()).toMinutes();
        recencyScore /= 600.0;
        double hotUserPenalty = author.isHotUser() ? 0.15 : 0.0;

        double affinityBoost = visibleAuthorIds.contains(author.getUserId()) &&
                viewerId.equals(author.getUserId()) ? 0.0 : 0.2;
        double rankingScore = Math.round((recencyScore + affinityBoost + hotUserPenalty) * 100.0) / 100.0;
        String deliveryStrategy = author.isHotUser() ? "hybrid-pull" : "fan-out-on-write";

        String rankinOnReason = author.isHotUser()
                ? "Hot user content is blended with pull-based bias to reduce write amplifications"
                : "Recent content from followed accounts is promoted for freshness and affinity";


        return new FeedItemResponse(
                post.getPostId(), author.getUserId(),
                author.getHandle(), author.getName(),
                post.getContent(), post.getCreatedAt(),
                rankingScore, deliveryStrategy, rankinOnReason
        );
    }

    private List<Post> fetchHomeFeedPosts(Set<String> authorIds,
                                          FeedCursorCodec.FeedCursor pageCursor,
                                          int fetchSize) {
        PageRequest pageRequest = PageRequest.of(0, fetchSize);
        if (pageCursor == null) {
            return postRepository.findByAuthor_IdInOrderByCreatedAtDesc(authorIds, pageRequest);
        }
        return postRepository.findHomeFeedPageAfterCursor(authorIds, pageCursor.createdAt(), pageCursor.postId(), pageRequest);
    }

    private Optional<FeedSlice> getCachedNormalHomeFeedSlice(String userId, FeedCursorCodec.FeedCursor pageCursor,
                                                             int pageSize) {
        if (pageCursor != null) {
            metricsService.recordHomeFeedCacheLookup("bypass_cursor");
            return Optional.empty();
        }

        if (pageSize > FeedCacheServiceMgr.DEFAULT_PAGE_SIZE) {
            metricsService.recordHomeFeedCacheLookup("Bypass_page_size");
            return Optional.empty();
        }
        Optional<TimeLinePageResponse> cachedPage = feedCacheService.getHomeFeed(userId);

        if (cachedPage.isEmpty()) {
            metricsService.recordHomeFeedCacheLookup("miss");
            return Optional.empty();
        }

        Optional<FeedSlice> adapted = adaptCachedFirstPage(cachedPage.get(), pageSize);
        metricsService.recordHomeFeedCacheLookup(adapted.isPresent() ? "hit" : "incomplete");
        return adapted;
    }

    private Optional<FeedSlice> adaptCachedFirstPage(TimeLinePageResponse cachedPage, int pageSize) {
        if (cachedPage.feedItemResponses().size() < pageSize || cachedPage.nextCursor() != null) {
            return Optional.empty();
        }
        List<FeedItemResponse> pageItems = cachedPage.feedItemResponses()
                .stream().limit(pageSize).toList();
        FeedSlice feedSlice = new FeedSlice(pageItems,
                cachedPage.feedItemResponses().size() > pageItems.size());
        return Optional.of(feedSlice);
    }

    private VisibleAuthors getVisibleAuthors(UserProfile viewer) {
        List<FollowRelationships> followRelations = followRelationshipsRepo
                .findByFollowed_Id(viewer.getUserId());
        Set<String> allAuthorIds = new LinkedHashSet<>();
        Set<String> hotAuthorIds = new LinkedHashSet<>();
        Set<String> nonHotAuthorIds = new LinkedHashSet<>();

        allAuthorIds.add(viewer.getUserId());
        if (viewer.isHotUser()) {
            hotAuthorIds.add(viewer.getUserId());
        } else {
            nonHotAuthorIds.add(viewer.getUserId());
        }

        for (FollowRelationships relations : followRelations) {
            UserProfile author = relations.getFollowed();
            allAuthorIds.add(viewer.getUserId());
            if (author.isHotUser()) {
                hotAuthorIds.add(author.getUserId());
            } else {
                nonHotAuthorIds.add(author.getUserId());
            }
        }
        return new VisibleAuthors(allAuthorIds, hotAuthorIds, nonHotAuthorIds);
    }

    private int normalizedLimit(int limit) {
        if (limit <= 0) {
            return FeedCacheServiceMgr.DEFAULT_PAGE_SIZE;
        }
        return Math.min(limit, 20);
    }

    private UserProfile getUser(String userId) {
        return userProfileRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User Not Found !!"));
    }

    private record VisibleAuthors(Set<String> allAuthorIds, Set<String> hotAuthorIds, Set<String> nonHotAuthorIds) {
    }

    private record FeedSlice(List<FeedItemResponse> itemResponses, boolean hasMore) {
    }

    private record NormalFeedSliceResult(FeedSlice slice, String cacheOutcome) {
    }
}
