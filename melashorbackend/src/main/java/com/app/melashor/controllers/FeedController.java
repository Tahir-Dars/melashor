package com.app.melashor.controllers;

import com.app.melashor.domain.dto.record.TimeLinePageResponse;
import com.app.melashor.service.FeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/feed")
@RequiredArgsConstructor
public class FeedController {
    private final FeedService feedService;

    @GetMapping
    public TimeLinePageResponse getHomeFeed(
            @RequestParam String userId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "5") String limit
    ) {
        return feedService.getHomeFeed(userId,cursor,limit);
    }

}
