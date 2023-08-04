package hello.board.repository.article;

import hello.board.domain.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, Long>, ArticleSearchRepository {

    @Override
    @EntityGraph(attributePaths = {"author"})
    @NonNull
    List<Article> findAll();

    @Override
    @EntityGraph(attributePaths = {"author"})
    @NonNull
    Page<Article> findAll(@NonNull Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"author"})
    @NonNull
    Optional<Article> findById(@NonNull Long id);

    @Query("""
            select a from Article a
            join fetch a.author
            left join fetch a.comments c
            left join fetch c.author
            where a.id = :id
    """)
    Optional<Article> findArticleWithComments(Long id);


}
