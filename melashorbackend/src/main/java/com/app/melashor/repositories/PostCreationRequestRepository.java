package com.app.melashor.repositories;

import com.app.melashor.domain.dto.classses.PostCreationRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostCreationRequestRepository extends JpaRepository<PostCreationRequest, Long> {

    Optional<PostCreationRequest> findByUserIdAndIdempotencyKey(String authorId, String idempotencyKey);
}
