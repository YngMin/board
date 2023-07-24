package hello.board.dto.service;

import hello.board.domain.Article;
import hello.board.domain.User;
import lombok.Getter;

public abstract class ArticleServiceDto {

    @Getter
    public static class Save {

        private final String title;
        private final String content;

        private Save(String title, String content) {
            this.title = title;
            this.content = content;
        }

        public static Save create(String title, String content) {
            return new Save(title, content);
        }

        public Article toEntity(User author) {
            return Article.create(title, content, author);
        }
    }

    @Getter
    public static class Update {

        private final String title;
        private final String content;

        private Update(String title, String content) {
            this.title = title;
            this.content = content;
        }

        public static Update create(String title, String content) {
            return new Update(title, content);
        }
    }
}
