package hello.board.dto.view;

import hello.board.domain.Comment;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CommentViewResponse {

    private final String content;
    private final String author;
    private final LocalDateTime createdAt;

    public CommentViewResponse(String content, String author, LocalDateTime createdAt) {
        this.content = content;
        this.author = author;
        this.createdAt = createdAt;
    }

    public static CommentViewResponse from(Comment comment) {
        return new CommentViewResponse(comment.getContent(), comment.getAuthor().getUsername(), comment.getCreatedAt());
    }
}
