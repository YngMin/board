package hello.board.repository;

import hello.board.domain.Article;
import hello.board.dto.service.ArticleCommentFlatDto;
import hello.board.repository.custom.ArticleSearchRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;

import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, Long>, ArticleSearchRepository {

    @Override
    @NonNull
    @EntityGraph(attributePaths = {"author"})
    Optional<Article> findById(@NonNull Long id);

    @Query("""
            select a from Article a
            left join fetch a.author
            left join fetch a.comments c
            left join fetch c.author
            where a.id = :id
    """)
    Optional<Article> findWithComments(Long id);

    @Query("""
            select new hello.board.dto.service.ArticleCommentFlatDto(a, c) from Article a
            left join fetch a.author
            left join a.comments c
            left join fetch c.author
            where a.id = :id
    """)
    Page<ArticleCommentFlatDto> findWithComments(Long id, Pageable pageable);

}
