package hello.board.repository.custom;

import hello.board.domain.Article;
import hello.board.dto.service.search.ArticleSearchCond;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ArticleSearchRepository {
    Page<Article> search(Pageable pageable);
    Page<Article> search(ArticleSearchCond cond, Pageable pageable);
}
