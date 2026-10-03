package com.app.melashor.repositories;

import com.app.melashor.domain.model.DeadEventFeedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeadLetterFERepository extends JpaRepository<DeadEventFeedEvent, String> {
}
