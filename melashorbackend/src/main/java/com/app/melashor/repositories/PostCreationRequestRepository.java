package com.app.melashor.repositories;

import com.app.melashor.domain.dto.classses.PostCreationRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostCreationRequestRepository extends JpaRepository<PostCreationRequest, Long> {
}
