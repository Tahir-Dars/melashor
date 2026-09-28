package com.app.melashor.config;

import com.app.melashor.domain.model.FollowRelationships;
import com.app.melashor.domain.model.Post;
import com.app.melashor.domain.model.UserProfile;
import com.app.melashor.repositories.FollowRelationshipsRepository;
import com.app.melashor.repositories.PostRepository;
import com.app.melashor.repositories.UserProfileRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedFeedMeData(UserProfileRepository userProfileRepository,
                                     FollowRelationshipsRepository followRelationshipsRepository,
                                     PostRepository postRepository) {
        return ignore -> {
            if (userProfileRepository.count() > 0L) {
                return;
            }

            UserProfile profile1 = UserProfile.builder()
                    .userId("u1")
                    .name("Ahmed")
                    .handle("ahmed01")
                    .profileBio("BioMedical Engineer")
                    .hotUser(false)
                    .build();
            UserProfile profile2 = UserProfile.builder()
                    .userId("u2")
                    .name("Khan Mohd")
                    .handle("kmUnar")
                    .profileBio("Chemical Engineer and Researcher")
                    .hotUser(true)
                    .build();
            UserProfile profile3 = UserProfile.builder()
                    .userId("u3")
                    .name("Samad")
                    .handle("cjondesk")
                    .profileBio("CS Engr")
                    .hotUser(false)
                    .build();
            UserProfile profile4 = UserProfile.builder()
                    .userId("u4")
                    .name("Safeel Khatti")
                    .handle("sfkhatti")
                    .profileBio("Specialist in Material Sciences")
                    .hotUser(false)
                    .build();

            List<UserProfile> allProfiles = List.of(profile1, profile2, profile3, profile4);
            userProfileRepository.saveAll(allProfiles);

            followRelationshipsRepository.saveAll(
                    List.of(new FollowRelationships(profile1, profile2),
                            new FollowRelationships(profile3, profile1),
                            new FollowRelationships(profile4, profile3),
                            new FollowRelationships(profile1, profile4),
                            new FollowRelationships(profile2, profile3),
                            new FollowRelationships(profile4, profile1),
                            new FollowRelationships(profile2, profile4))
            );

            postRepository.saveAll(
                    Stream.of(
                            Post.builder().postId(UUID.randomUUID().toString())
                                    .author(profile1).content("content1").build(),
                            Post.builder().postId(UUID.randomUUID().toString())
                                    .author(profile2).content("content2A").build(),
                            Post.builder().postId(UUID.randomUUID().toString())
                                    .author(profile2).content("content2B").build(),
                            Post.builder().postId(UUID.randomUUID().toString())
                                    .author(profile3).content("content3").build(),
                            Post.builder().postId(UUID.randomUUID().toString())
                                    .author(profile4).content("content4").build()
                    ).toList()
            );
        };
    }
}
