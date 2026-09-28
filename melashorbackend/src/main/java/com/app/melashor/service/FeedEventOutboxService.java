package com.app.melashor.service;

import com.app.melashor.domain.model.Post;

public interface FeedEventOutboxService {
    void enqueuePostCreated(Post post);
}
