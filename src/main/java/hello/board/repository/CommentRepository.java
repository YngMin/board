package hello.board.repository;

import hello.board.domain.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Override
    @NonNull
    @EntityGraph(attributePaths = {"author"})
    Optional<Comment> findById(@NonNull Long id);

    @EntityGraph(attributePaths = {"author"})
    Page<Comment> findByArticleId(Long articleId, Pageable pageable);

    @EntityGraph(attributePaths = {"article", "author"})
    Optional<Comment> findWithArticleById(Long id);

}
