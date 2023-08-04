package hello.board.dto.view;

import hello.board.domain.Article;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class ArticleViewResponse {

    private final Long id;
    private final String title;
    private final String content;
    private final String author;
    private final LocalDateTime createdAt;
    private final Long view;
    private final List<CommentViewResponse> comments;

    @Builder
    private ArticleViewResponse(Long id, String title, String content, String author, LocalDateTime createdAt, Long view, List<CommentViewResponse> comments) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.author = author;
        this.createdAt = createdAt;
        this.view = view;
        this.comments = comments;
    }


    public static ArticleViewResponse from(Article article) {
        return ArticleViewResponse.builder()
                .id(article.getId())
                .title(article.getTitle())
                .content(article.getContent())
                .author(article.getAuthor().getUsername())
                .createdAt(article.getCreatedAt())
                .view(article.getView())
                .comments(article.getComments().stream()
                        .map(CommentViewResponse::from)
                        .toList())
                .build();
    }
}
