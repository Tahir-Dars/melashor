package com.app.melashor.service.serviceImpl;

import com.app.melashor.domain.dto.record.TimeLinePageResponse;
import com.app.melashor.domain.model.UserProfile;
import com.app.melashor.repositories.UserProfileRepository;
import com.app.melashor.service.FeedCursorCodec;
import com.app.melashor.service.FeedMetricsService;
import com.app.melashor.service.FeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FeedServiceMgr implements FeedService {

    private final FeedMetricsService metricsService;
    private final UserProfileRepository userProfileRepository;
    private final FeedCursorCodecMgr codecMgr;

    @Override
    public TimeLinePageResponse getHomeFeed(String userId, String cursor, String limit) {
        long startedAtNanos = metricsService.startTime();
        int limitInInt = Integer.parseInt(limit);
        int pageSize = normalizedLimit(limitInInt);
        metricsService.recordHomeFeedRequestedPageSize(limitInInt, pageSize);

        try {
            UserProfile viewer = getUser(userId);
            FeedCursorCodec.FeedCursor pageCursor = codecMgr.parse(cursor);

            VisibleAuthors visibleAuthors=getVisibleAuthors(viewer);
        } catch (ResponseStatusException e) {
            metricsService.recordServiceError("get_home_feed", e.getStatusCode().toString());
            throw e;
        }
        return null;
    }

    private VisibleAuthors getVisibleAuthors(UserProfile viewer) {
        return new VisibleAuthors(new HashSet<>(),new HashSet<>(),new HashSet<>());
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
