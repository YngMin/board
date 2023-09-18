package hello.board.dto.service.search;

import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode(exclude = {"keyword", "type"})
@NoArgsConstructor
public final class ArticleSearchCond {

    private String keyword;
    private ArticleSearchType type = ArticleSearchType.TITLE_AND_CONTENT;

    private ArticleSearchCond(String keyword, ArticleSearchType type) {
        this.keyword = keyword;
        this.type = type;
    }

    public static ArticleSearchCond create(String keyword, ArticleSearchType type) {
        return new ArticleSearchCond(keyword, type);
    }

    public static ArticleSearchCond empty() {
        return new ArticleSearchCond("", ArticleSearchType.TITLE_AND_CONTENT);
    }

}
