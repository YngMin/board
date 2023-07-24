package hello.board.repository;

import hello.board.domain.Article;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    @Override
    @EntityGraph(attributePaths = {"author"})
    @NonNull List<Article> findAll();

    @Override
    @EntityGraph(attributePaths = {"author"})
    @NonNull Optional<Article> findById(@NonNull Long id);

}
