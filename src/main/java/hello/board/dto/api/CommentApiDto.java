package hello.board.dto.api;

import hello.board.domain.Comment;
import hello.board.dto.service.CommentServiceDto;
import jakarta.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommentApiDto {

    @Getter
    @Setter
    public static final class SaveRequest {

        @NotEmpty
        private String content;

        public CommentServiceDto.Save toDto() {
            return CommentServiceDto.Save.create(content);
        }
    }

    @Getter
    public static final class SaveResponse {

        private final Long id;

        private SaveResponse(Long id) {
            this.id = id;
        }

        public static SaveResponse create(Long id) {
            return new SaveResponse(id);
        }
    }

    @Getter
    public static final class FindResponse {

        private final Long id;
        private final String content;
        private final String author;

        private FindResponse(Long id, String content, String author) {
            this.id = id;
            this.content = content;
            this.author = author;
        }

        public static FindResponse of(Comment comment) {
            return new FindResponse(comment.getId(), comment.getContent(), comment.getAuthor().getName());
        }
    }

    @Getter
    @Setter
    public static final class UpdateRequest {

        @NotEmpty
        private String content;

        public CommentServiceDto.Update toDto() {
            return CommentServiceDto.Update.create(content);
        }
    }

    @Getter
    public static final class UpdateResponse {

        private final String content;
        private final LocalDateTime modifiedAt;

        private UpdateResponse(String content, LocalDateTime modifiedAt) {
            this.content = content;
            this.modifiedAt = modifiedAt;
        }

        public static UpdateResponse of(Comment comment) {
            return new UpdateResponse(comment.getContent(), comment.getModifiedAt());
        }
    }

}
