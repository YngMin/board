package hello.board.dto.view;

import hello.board.domain.Article;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ArticleListViewResponse {

    private final Long id;
    private final String title;
    private final String author;
    private final LocalDateTime createdAt;
    private final Long view;

    private ArticleListViewResponse(Long id, String title, String author, LocalDateTime createdAt, Long view) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.createdAt = createdAt;
        this.view = view;
    }

    public static ArticleListViewResponse from(Article article) {
        return new ArticleListViewResponse(article.getId(), article.getTitle(), article.getAuthor().getUsername(), article.getCreatedAt(), article.getView());
    }
}
