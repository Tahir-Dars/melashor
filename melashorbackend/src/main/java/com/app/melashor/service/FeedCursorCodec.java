package com.app.melashor.service;

import com.app.melashor.domain.dto.record.FeedItemResponse;

import java.time.Instant;

public interface FeedCursorCodec {
    record FeedCursor(Instant createdAt, String postId) {
    }

    FeedCursor parse(String cursor);

    String encode(FeedItemResponse itemResponse);

    String encode(Instant createdAt, String postId);
}
