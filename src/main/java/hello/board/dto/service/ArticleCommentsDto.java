package hello.board.dto.service;

import hello.board.domain.Article;
import hello.board.domain.Comment;
import lombok.Getter;

import java.util.List;

@Getter
public class ArticleCommentsDto {

    private final Article article;
    private final List<Comment> comments;

    private ArticleCommentsDto(Article article, List<Comment> comments) {
        this.article = article;
        this.comments = comments;
    }

    public static ArticleCommentsDto from(Article article, List<Comment> comments) {
        return new ArticleCommentsDto(article, comments);
    }
}
