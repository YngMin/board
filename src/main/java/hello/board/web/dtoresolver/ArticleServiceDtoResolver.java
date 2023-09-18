package hello.board.web.dtoresolver;

import hello.board.dto.api.ArticleApiDto;
import hello.board.dto.service.ArticleServiceDto;
import hello.board.dto.service.search.ArticleSearchCond;
import hello.board.dto.view.BoardRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public class ArticleServiceDtoResolver {

    @Value("${view.board.article-page-size}")
    private int ARTICLE_PAGE_SIZE;

    @Value("${view.board.comment-page-size}")
    private int COMMENT_PAGE_SIZE;

    public ArticleServiceDto.Save toSaveDto(ArticleApiDto.SaveRequest saveRequest) {
        return ArticleServiceDto.Save.create(saveRequest.getTitle(), saveRequest.getContent());
    }

    public ArticleServiceDto.Update toUpdateDto(ArticleApiDto.UpdateRequest updateRequest) {
        return ArticleServiceDto.Update.create(updateRequest.getTitle(), updateRequest.getContent());
    }

    public Pageable toPageable(ArticleApiDto.FindRequest findRequest) {
        return PageRequest.of(findRequest.getPage() - 1, findRequest.getSize());
    }

    public Pageable toPageable(BoardRequest.ListView findRequest) {
        return PageRequest.of(findRequest.getPage() - 1, ARTICLE_PAGE_SIZE);
    }

    public Pageable toPageable(BoardRequest.View findRequest) {
        return PageRequest.of(findRequest.getPage() - 1, COMMENT_PAGE_SIZE);
    }

    public ArticleSearchCond toSearchCond(ArticleApiDto.FindRequest findRequest) {
        return ArticleSearchCond.create(findRequest.getKeyword(), findRequest.getType());
    }

    public ArticleSearchCond toSearchCond(BoardRequest.ListView findRequest) {
        return ArticleSearchCond.create(findRequest.getKeyword(), findRequest.getType());
    }

}
