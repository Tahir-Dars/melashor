package com.app.melashor.service.serviceImpl;

import com.app.melashor.domain.model.OutBoxEvent;
import com.app.melashor.domain.model.Post;
import com.app.melashor.repositories.OutboxEventRepository;
import com.app.melashor.service.FeedEventOutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FeedEventOutboxServiceMgr implements FeedEventOutboxService {

    private final OutboxEventRepository outboxEventRepository;

    @Override
    public void enqueuePostCreated(Post post) {

        outboxEventRepository.save(new OutBoxEvent(post));
    }
}
