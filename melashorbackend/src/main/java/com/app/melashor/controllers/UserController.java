package com.app.melashor.controllers;

import com.app.melashor.domain.dto.record.UserProfileResponse;
import com.app.melashor.service.FeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/api/users")
@RequiredArgsConstructor
public class UserController {

    private final FeedService feedService;

    @GetMapping
    public List<UserProfileResponse> getUser() {
        return feedService.getUser();
    }
}
