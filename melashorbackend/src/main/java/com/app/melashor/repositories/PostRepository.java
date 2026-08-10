package com.app.melashor.repositories;

import com.app.melashor.domain.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, String> {
    List<Post> findByAuthor_IdInOrderByCreatedAtDesc(Collection<String> authorIds);
}
