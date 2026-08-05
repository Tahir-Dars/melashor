package com.app.melashor.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Entity
@Table(name = "users")
@AllArgsConstructor
@Builder
@RequiredArgsConstructor
public class UserProfile {
    @Id
    private String id;

    @Column(unique = true, nullable = false)
    private String handle;

    @Column(nullable = false, length = 500)
    private String profileBio;

    @Column(nullable = false)
    private boolean hotUser;
}
