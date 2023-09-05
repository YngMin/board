package hello.board.dto.service.search;

import hello.board.domain.Article;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class ArticleCommentCountDto {

    private final Article article;
    private final long numberOfComments;
}
