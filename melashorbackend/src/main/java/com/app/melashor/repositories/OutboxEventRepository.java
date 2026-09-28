package com.app.melashor.repositories;

import com.app.melashor.domain.OutboxEventStatus;
import com.app.melashor.domain.model.OutBoxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.awt.print.Pageable;
import java.time.Instant;
import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutBoxEvent, String> {

    List<OutBoxEvent> findByStatusAndNextAttemptTimeLessThanEqualOrderByCreatedAtAsc(
            OutboxEventStatus status,
            Instant nextAttempt,
            Pageable pageable
    );
}
