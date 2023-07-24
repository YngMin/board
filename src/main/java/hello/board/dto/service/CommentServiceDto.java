package hello.board.dto.service;

import hello.board.domain.Article;
import hello.board.domain.Comment;
import hello.board.domain.User;
import lombok.Getter;

public abstract class CommentServiceDto {

    @Getter
    public static class Save {

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
    public static class Update {

        private final String content;

        private Update(String content) {
            this.content = content;
        }

        public static Update create(String content) {
            return new Update(content);
        }

    }
}
