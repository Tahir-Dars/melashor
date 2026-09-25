package com.app.melashor.domain.dto.record;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "post_creation_requests")
@Getter
@Setter
@NoArgsConstructor
public class PostCreationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String userId;

    private String idempotencyKey;

    private String requestMatch;

}
