package hello.board.dto.service.search;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public final class CommentCountDto {

    private final Long articleId;
    private final long count;
}
