package com.app.melashor.service;

import com.app.melashor.domain.dto.record.*;
import jakarta.validation.Valid;

import java.util.List;

public interface FeedService {
    TimeLinePageResponse getHomeFeed(String userId, String cursor, String limit);

    TimeLinePageResponse getUserFeed(String userId, String cursor, int limit);

    List<UserProfileResponse> getUser();

    FollowingResponse getFollowing(String followerId);

    FollowResponse follow(String followerId, String userId);

    FollowResponse unFollow(String followerId, String userId);

    PostResponse createPost(@Valid CreatePostRequest postRequest);
}
