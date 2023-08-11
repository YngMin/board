package hello.board.dto.service.search;

import hello.board.domain.Article;
import lombok.Getter;

@Getter
public class ArticleSearchDto {

    private final Article article;
    private final Long numComments;

    public ArticleSearchDto(Article article, Long numComments) {
        this.article = article;
        this.numComments = numComments == null ? 0 : numComments;
    }
}
