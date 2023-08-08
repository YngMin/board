package hello.board.dto.service.search;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public final class ArticleSearchCond {

    private String keyword;
    private ArticleSearchType type = ArticleSearchType.TITLE_AND_CONTENT;

    private ArticleSearchCond(String keyword, ArticleSearchType type) {
        this.keyword = keyword;
        this.type = type;
    }

    public static ArticleSearchCond create(String keyword, ArticleSearchType method) {
        return new ArticleSearchCond(keyword, method);
    }
}
