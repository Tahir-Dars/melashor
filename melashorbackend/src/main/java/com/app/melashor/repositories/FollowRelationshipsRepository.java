package com.app.melashor.repositories;

import com.app.melashor.domain.model.FollowRelationships;
import com.app.melashor.domain.model.FollowRelationshipsId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FollowRelationshipsRepository extends JpaRepository<FollowRelationships, FollowRelationshipsId> {
    List<FollowRelationships> findByFollower_Id(String followerId);

    List<FollowRelationships> findByFollowed_Id(String followedId);

    long countByFollower_Id(String followerId);
}
