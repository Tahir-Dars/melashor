package com.app.melashor.controllers;

import com.app.melashor.domain.dto.record.FollowingResponse;
import com.app.melashor.service.FeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/follows")
@RequiredArgsConstructor
public class FollowController {

    private final FeedService feedService;

    @GetMapping
    public FollowingResponse getFollowing(@RequestParam String followerId) {
        return feedService.getFollowing(followerId);
    }

}
