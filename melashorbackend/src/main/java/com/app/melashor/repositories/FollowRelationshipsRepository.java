package com.app.melashor.repositories;

import com.app.melashor.domain.model.FollowRelationships;
import com.app.melashor.domain.model.FollowRelationshipsId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FollowRelationshipsRepository extends JpaRepository<FollowRelationships, FollowRelationshipsId> {
    List<FollowRelationships> findByFollower_UserId(String followerId);

    List<FollowRelationships> findByFollowed_UserId(String followedId);

    long countByFollower_UserId(String followerId);

    long contactByFollower_UserId(String followerId);
}
