package hello.board.repository.article;

import hello.board.domain.Article;
import hello.board.dto.service.ArticleSearchCond;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ArticleSearchRepository {

    Page<Article> search(Pageable pageable);
    Page<Article> search(ArticleSearchCond cond, Pageable pageable);
}
