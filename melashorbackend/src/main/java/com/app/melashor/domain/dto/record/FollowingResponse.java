package com.app.melashor.domain.dto.record;

import java.util.List;

public record FollowingResponse(
        String followerId,
        List<String> targetUserIds,
        int totalFollowing
) {
}
