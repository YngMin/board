package hello.board.repository.custom;

import hello.board.domain.Article;
import hello.board.dto.service.search.ArticleSearchCond;
import hello.board.dto.service.search.ArticleSearchDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ArticleSearchRepository {
    Page<Article> search(Pageable pageable);
    Page<Article> search(ArticleSearchCond cond, Pageable pageable);

    Page<ArticleSearchDto> searchUpgradeWithSubQuery(ArticleSearchCond cond, Pageable pageable);
    Page<ArticleSearchDto> searchUpgradeWithJoin(ArticleSearchCond cond, Pageable pageable);

    Page<Article> searchUpgradeWithFetchJoin(ArticleSearchCond cond, Pageable pageable);

}
