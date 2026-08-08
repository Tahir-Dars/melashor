package com.app.melashor.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "follow_relations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FollowRelationships {
    @EmbeddedId
    private FollowRelationshipsId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("followerId")
    @JoinColumn(name = "follower_id", nullable = false)
    private UserProfile follower;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("followedId")
    @JoinColumn(name = "followed_id", nullable = false)
    private UserProfile followed;

    public FollowRelationships(UserProfile follower,UserProfile followed){
        this.id=new FollowRelationshipsId(follower.getUserId(),followed.getUserId());
        this.follower=follower;
        this.followed=followed;
    }
}

