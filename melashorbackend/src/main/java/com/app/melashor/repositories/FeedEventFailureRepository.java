package com.app.melashor.repositories;

import com.app.melashor.domain.model.FeedEventFailure;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedEventFailureRepository extends JpaRepository<FeedEventFailure, String> {
}
