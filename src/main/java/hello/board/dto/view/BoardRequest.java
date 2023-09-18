package hello.board.dto.view;

import hello.board.dto.service.search.ArticleSearchType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BoardRequest {

    @Getter
    @Setter
    public static class ListView {

        @Min(1)
        private int page = 1;

        @NotNull
        private String keyword = "";

        @NotNull
        private ArticleSearchType type = ArticleSearchType.TITLE_AND_CONTENT;
    }

    @Getter
    @Setter
    public static class View {

        @Min(1)
        private int page = 1;
    }
}
