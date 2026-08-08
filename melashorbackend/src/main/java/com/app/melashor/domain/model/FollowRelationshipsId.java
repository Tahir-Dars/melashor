package com.app.melashor.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode
public class FollowRelationshipsId {
    @Column(name = "follower_id")
    private String followerId;

    @Column(name = "followed_id")
    private String followedId;
}
