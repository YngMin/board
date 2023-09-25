package hello.board.dto.view;

import hello.board.domain.Comment;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.security.core.parameters.P;

import java.time.LocalDateTime;

@Getter
@EqualsAndHashCode
public final class CommentViewResponse {

    private final Long id;
    private final String content;
    private final String author;
    private final LocalDateTime createdAt;

    private CommentViewResponse(Long id, String content, String author, LocalDateTime createdAt) {
        this.id = id;
        this.content = content;
        this.author = author;
        this.createdAt = createdAt;
    }

    public static CommentViewResponse of(Comment comment) {
        return new CommentViewResponse(
                comment.getId(),
                comment.getContent(),
                comment.getAuthor().getName(),
                comment.getCreatedAt()
        );
    }


}
