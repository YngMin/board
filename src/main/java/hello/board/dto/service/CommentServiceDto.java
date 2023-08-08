package hello.board.dto.service;

import hello.board.domain.Article;
import hello.board.domain.Comment;
import hello.board.domain.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommentServiceDto {

    @Getter
    public static final class Save {
        private final String content;

        private Save(String content) {
            this.content = content;
        }

        public static Save create(String content) {
            return new Save(content);
        }

        public Comment toEntity(Article article, User author) {
            return Comment.create(content, article, author);
        }
    }

    @Getter
    public static final class Update {

        private final String content;

        private Update(String content) {
            this.content = content;
        }

        public static Update create(String content) {
            return new Update(content);
        }

    }

    @Getter
    public static final class Find {

        private final String content;
        private final String author;
        private final LocalDateTime createdAt;

        private Find(String content, String author, LocalDateTime createdAt) {
            this.content = content;
            this.author = author;
            this.createdAt = createdAt;
        }

        public static Find create(String content, String author, LocalDateTime createdAt) {
            return new Find(content, author, createdAt);
        }
    }
}
