package com.app.melashor.service.serviceImpl;

import com.app.melashor.domain.dto.record.FeedItemResponse;
import com.app.melashor.service.FeedCursorCodec;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.regex.Pattern;

@NoArgsConstructor
public class FeedCursorCodecMgr implements FeedCursorCodec {

    private static final String SEPARATOR = "|";
    private static final String SEPARATOR_REGEX = Pattern.quote(SEPARATOR);

    @Override
    public FeedCursor parse(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        String[] parts = cursor.split(SEPARATOR_REGEX, 2);
        if (parts.length != 2 || parts[1].isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Cursor");
        }
        try {
            long epocMillis = Long.parseLong(parts[0]);
            return new FeedCursor(Instant.ofEpochMilli(epocMillis), parts[1]);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Cursor");
        }
    }

    @Override
    public String encode(FeedItemResponse item) {
        return encode(item.createdAt(), item.postId());
    }

    @Override
    public String encode(Instant createdAt, String postId) {
        return createdAt.toEpochMilli() + SEPARATOR + postId;
    }

}
