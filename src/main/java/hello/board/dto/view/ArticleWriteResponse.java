package hello.board.dto.view;

import hello.board.domain.Article;
import lombok.Getter;

@Getter
public class ArticleWriteResponse {

    private final Long id;
    private final String title;
    private final String content;

    private ArticleWriteResponse(Long id, String title, String content) {
        this.id = id;
        this.title = title;
        this.content = content;
    }

    public static ArticleWriteResponse empty() {
        return new ArticleWriteResponse(null, "", "");
    }

    public static ArticleWriteResponse from(Article article) {
        return new ArticleWriteResponse(article.getId(), article.getTitle(), article.getContent());
    }
}
