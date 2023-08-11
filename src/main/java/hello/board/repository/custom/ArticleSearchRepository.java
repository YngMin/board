package hello.board.repository.custom;

import hello.board.dto.service.search.ArticleSearchCond;
import hello.board.dto.service.search.ArticleSearchDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ArticleSearchRepository {
    Page<ArticleSearchDto> search(Pageable pageable);
    Page<ArticleSearchDto> search(ArticleSearchCond cond, Pageable pageable);

}
