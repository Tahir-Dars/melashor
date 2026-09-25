package com.app.melashor.domain.dto.record;

import com.app.melashor.domain.dto.PostCreationStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "post_creation_requests")
@Getter
@Setter
@NoArgsConstructor
public class PostCreationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "idempotency_key", nullable = false, length = 100)
    private String idempotencyKey;

    @Column(name = "request_hash", nullable = false, length = 64)
    private String requestMatch;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PostCreationStatus status;

    @Column(name = "post_id")
    private String postId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

}
