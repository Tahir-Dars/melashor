package com.app.melashor.domain.model;

import com.app.melashor.domain.OutboxEventStatus;
import com.app.melashor.domain.OutboxEventType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
@Getter
@NoArgsConstructor
public class OutBoxEvent {
    @Id
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxEventStatus status;

    @Column(nullable = false)
    private String postId;

    @Column(nullable = false)
    private String authorId;

    @Column(nullable = false)
    private boolean hotUser;

    @Column(nullable = false)
    private Instant eventTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxEventType eventType;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(nullable = false)
    private Instant nextAttemptTime;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    public OutBoxEvent(Post post) {
        this.id = UUID.randomUUID().toString();
        this.status = OutboxEventStatus.PENDING;
        this.postId = post.getPostId();
        this.authorId = post.getAuthor().getUserId();
        this.hotUser = post.getAuthor().isHotUser();
        this.eventTime = post.getCreatedAt();
        this.eventType = OutboxEventType.POST_CREATED;
        this.attemptCount = 0;
        this.nextAttemptTime = Instant.now();
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (nextAttemptTime == null) {
            nextAttemptTime = now;
        }
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public void markPublished() {
        this.status = OutboxEventStatus.PUBLISHED;
    }

    public void scheduleRetry(Instant nextAttemptTime, int maxAttempt) {
        this.attemptCount++;
        this.nextAttemptTime = nextAttemptTime;
        if (this.attemptCount >= maxAttempt) {
            this.status=OutboxEventStatus.FAILED;
        }
    }

}
