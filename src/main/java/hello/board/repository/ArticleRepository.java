package hello.board.repository;

import hello.board.domain.Article;
import hello.board.repository.custom.ArticleSearchRepository;
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
            join fetch a.author
            left join fetch a.comments c
            left join fetch c.author
            where a.id = :id
    """)
    Optional<Article> findWithComments(Long id);

}
