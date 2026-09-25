package com.app.melashor.controllers;

import com.app.melashor.domain.dto.record.FollowResponse;
import com.app.melashor.domain.dto.record.FollowingResponse;
import com.app.melashor.service.FeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/follows")
@RequiredArgsConstructor
public class FollowController {

    private final FeedService feedService;

    @GetMapping
    public FollowingResponse getFollowing(@RequestParam String followerId) {
        return feedService.getFollowing(followerId);
    }

    @PostMapping("/{userId}")
    private FollowResponse follow(@RequestParam String followerId, @PathVariable String userId){
        return feedService.follow(followerId,userId);
    }

    @DeleteMapping("/{userId}")
    private FollowResponse unFollow(@RequestParam String followerId, @PathVariable String userId){
        return feedService.unFollow(followerId,userId);
    }

}
