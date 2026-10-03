package com.app.melashor.repositories;

import com.app.melashor.domain.model.ProcessedFeedEvents;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedFeedEventsRepository extends JpaRepository<ProcessedFeedEvents, String> {
}
