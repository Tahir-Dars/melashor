package com.app.melashor.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "processed_feed_event")
@Getter
@NoArgsConstructor
public class ProcessedFeedEvents {
    @Id
    private String eventId;

    @Column(nullable = false, updatable = false)
    private Instant processedAt;

    public ProcessedFeedEvents(String eventId) {
        this.eventId = eventId;
        this.processedAt = Instant.now();
    }
}
