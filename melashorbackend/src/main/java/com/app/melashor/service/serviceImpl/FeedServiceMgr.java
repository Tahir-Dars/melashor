package com.app.melashor.service.serviceImpl;

import com.app.melashor.domain.dto.record.TimeLinePageResponse;
import com.app.melashor.service.FeedService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FeedServiceMgr implements FeedService {
    @Override
    public TimeLinePageResponse getHomeFeed(String userId, String cursor, String limit) {
        return null;
    }
}
