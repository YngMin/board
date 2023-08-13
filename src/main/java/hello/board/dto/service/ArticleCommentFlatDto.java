package hello.board.dto.service;

import hello.board.domain.Article;
import hello.board.domain.Comment;
import lombok.Getter;

@Getter
public final class ArticleCommentFlatDto {

    private final Article article;
    private final Comment comment;

    public ArticleCommentFlatDto(Article article, Comment comment) {
        this.article = article;
        this.comment = comment;
    }
}
