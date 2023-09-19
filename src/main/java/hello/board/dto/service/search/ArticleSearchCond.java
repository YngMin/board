package hello.board.dto.service.search;

import lombok.*;

@Getter
@ToString
@EqualsAndHashCode(exclude = {"keyword", "type"})
public final class ArticleSearchCond {

    private final String keyword;
    private final ArticleSearchType type;

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
