package hello.board.service.query;

import hello.board.domain.Article;
import hello.board.dto.service.search.ArticleSearchCond;
import hello.board.dto.service.search.ArticleSearchDto;
import hello.board.exception.FailToFindEntityException;
import hello.board.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ArticleQueryService {

    private final ArticleRepository articleRepository;

    public Article findById(Long id) {
        return articleRepository.findById(id)
                .orElseThrow(() -> FailToFindEntityException.of("Article"));
    }

    public Page<ArticleSearchDto> search(ArticleSearchCond cond, int page, int size) {
        return isConditionEmpty(cond)
                ? articleRepository.search(PageRequest.of(page, size))
                : articleRepository.search(cond, PageRequest.of(page, size));
    }

    public Page<ArticleSearchDto> search(int page, int size) {
        return articleRepository.search(PageRequest.of(page, size));
    }

    public Article findWithComments(Long id) {
        return articleRepository.findWithComments(id)
                .orElseThrow(() -> FailToFindEntityException.of("Article"));
    }

    /* ################################################## */

    private static boolean isConditionEmpty(ArticleSearchCond cond) {
        return cond == null || !StringUtils.hasText(cond.getKeyword());
    }
}
