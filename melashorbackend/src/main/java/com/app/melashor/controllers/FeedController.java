package com.app.melashor.controllers;

import com.app.melashor.service.FeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/feed")
@RequiredArgsConstructor
public class FeedController {
    private final FeedService feedService;

}
