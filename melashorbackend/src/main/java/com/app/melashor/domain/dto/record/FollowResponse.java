package com.app.melashor.domain.dto.record;

public record FollowResponse(
        String followerId,
        String targetUserId,
        boolean isFollowing,
        int totalFollowing
) {
}
