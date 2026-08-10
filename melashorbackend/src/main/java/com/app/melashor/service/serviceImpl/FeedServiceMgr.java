package com.app.melashor.service.serviceImpl;

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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FeedServiceMgr implements FeedService {

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

            FeedSlice hotSlice = getHotHomeFeedSlice(viewer.getUserId(), visibleAuthors, pageCursor, pageSize);
        } catch (ResponseStatusException e) {
            metricsService.recordServiceError("get_home_feed", e.getStatusCode().toString());
            throw e;
        }
        return null;
    }

    private FeedSlice getHotHomeFeedSlice(String userId, VisibleAuthors visibleAuthors,
                                          FeedCursorCodec.FeedCursor pageCursor, int pageSize) {
        return null;
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
        return null;
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

    private FeedItemResponse toFeedItems(Post post, String viewerId, Set<String> visibleAuthorIds) {

        UserProfile author = post.getAuthor();
        double recencyScore = 1.0 - Duration.between(post
                .getCreatedAt(), Instant.now()).toMinutes();
        recencyScore /= 600.0;
        double hotUserPenalty = author.isHotUser() ? 0.15 : 0.0;

//        double affinityBoost = visibleAuthorIds(author.getUserId(),  viewerId.equals(author.getUserId()));

        return null;
    }

    private List<Post> fetchHomeFeedPosts(Set<String> authorIds, FeedCursorCodec.FeedCursor pageCursor, int fetchSize) {
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
        FeedSlice feedSlice = new FeedSlice(pageItems, cachedPage.feedItemResponses().size() > pageItems.size());
        return Optional.of(feedSlice);
    }

    private record VisibleAuthors(Set<String> allAuthorIds, Set<String> hotAuthorIds, Set<String> nonHotAuthorIds) {
    }

    private record FeedSlice(List<FeedItemResponse> itemResponses, boolean hasMore) {
    }

    private record NormalFeedSliceResult(FeedSlice slice, String cacheOutcome) {
    }

    private VisibleAuthors getVisibleAuthors(UserProfile viewer) {
        List<FollowRelationships> followRelations = followRelationshipsRepo.findByFollowed_Id(viewer.getUserId());
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
}
