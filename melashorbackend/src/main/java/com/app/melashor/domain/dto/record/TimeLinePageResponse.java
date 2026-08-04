package com.app.melashor.domain.dto.record;

import com.app.melashor.domain.dto.TimeLineMode;

import java.util.List;

public record TimeLinePageResponse(String timelineOwnerId, List<FeedItemResponse> feedItemResponses, TimeLineMode mode,
                                   int totalItems, String nextCursor) {



}
