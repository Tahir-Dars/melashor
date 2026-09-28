package com.app.melashor.controllers;

import com.app.melashor.domain.dto.record.CreatePostRequest;
import com.app.melashor.domain.dto.record.PostResponse;
import com.app.melashor.service.FeedService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final FeedService feedService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostResponse createPost(@Valid @RequestBody CreatePostRequest postRequest) {
        return feedService.createPost(postRequest);
    }

    @GetMapping("/{postId}")
    public PostResponse getPost(@PathVariable String postId) {
        return feedService.getPost(postId);
    }
}
