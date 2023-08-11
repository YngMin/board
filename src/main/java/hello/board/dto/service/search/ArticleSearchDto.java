package hello.board.dto.service.search;

import hello.board.domain.Article;
import lombok.Getter;

@Getter
public class ArticleSearchDto {

    private final Article article;
    private final Long numberOfComments;

    public ArticleSearchDto(Article article, Long numberOfComments) {
        this.article = article;
        this.numberOfComments = numberOfComments == null ? 0 : numberOfComments;
    }
}
