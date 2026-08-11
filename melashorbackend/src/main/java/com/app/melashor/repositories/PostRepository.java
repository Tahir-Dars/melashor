package com.app.melashor.repositories;

import com.app.melashor.domain.model.Post;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Repository
public interface PostRepository extends JpaRepository<Post, String> {
    @EntityGraph(attributePaths = "author")
    List<Post> findByAuthor_IdInOrderByCreatedAtDesc(Collection<String> authorIds, Pageable pageable);

    @EntityGraph(attributePaths = "author")
        @Query("""
                 select p from Post p
                             where p.author.userId in:authorIds
                                         and(p.createdAt<:createdAt or (p.createdAt)=:createdAt
                                                     and p.postId< :postId )
                                                                 order by p.createdAt desc, p.postId desc
                """)
    List<Post> findHomeFeedPageAfterCursor(@Param("authorIds") Set<String> authorIds,
                                           @Param("createdAt") Instant createdAt,
                                           @Param("postId") String postId,
                                           Pageable pageable);

    long countByAuthor_IdIn(Set<String> nonHotUserIds);
}
