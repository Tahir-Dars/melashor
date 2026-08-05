package com.app.melashor.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "users")
@NoArgsConstructor
public class UserProfile {
    @Id
    private String userId;

    @Column(unique = true, nullable = false)
    private String handle;

    @Column(nullable = false, length = 500)
    private String profileBio;

    @Column(nullable = false)
    private boolean hotUser;
}
