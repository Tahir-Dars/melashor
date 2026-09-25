package com.app.melashor.domain.dto.record;

import java.time.Instant;

public record PostResponse(String postId, String authorId, String authorName, String authorHandle, String content,
                           Instant createdAt) {
}
