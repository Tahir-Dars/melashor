package com.app.melashor.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "dead_letter_feed_event")
@Getter
@NoArgsConstructor
public class DeadEventFeedEvent {
    @Id
    private String id;

    private String eventId;

    private String eventRecordId;

    private String payloadJson;

    private String reasonOfFailure;

    private int attempts;

    private Instant movedToDlqAt;

    public DeadEventFeedEvent(int attempts, String reasonOfFailure,
                              String payloadJson, String eventRecordId,
                              String eventId) {
        this.id = UUID.randomUUID().toString();
        this.attempts = attempts;
        this.reasonOfFailure = reasonOfFailure;
        this.payloadJson = payloadJson;
        this.eventRecordId = eventRecordId;
        this.eventId = eventId;
    }

    @PrePersist
    void onCreate() {
        this.movedToDlqAt = Instant.now();
    }
}
