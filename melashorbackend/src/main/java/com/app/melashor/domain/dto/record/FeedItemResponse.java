package com.app.melashor.domain.dto.record;

import java.time.Instant;

public record FeedItemResponse(String postId, String authorId, String authHandle, String authName, String content,
                               Instant createdAt, double ranking, String deliveryStrategy, String rankingReason)
{

}
