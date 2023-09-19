package hello.board.dto.service;

import hello.board.domain.Article;
import hello.board.domain.Comment;
import hello.board.domain.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ArticleServiceDto {

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

    @Getter
    public static class Find {

        private final String title;
        private final String content;
        private final String author;
        private final long view;
        private final LocalDateTime createdAt;

        @Setter
        private Page<CommentServiceDto.Find> comments;

        private Find(String title, String content, String author, long view, LocalDateTime createdAt) {
            this.title = title;
            this.content = content;
            this.author = author;
            this.view = view;
            this.createdAt = createdAt;
        }

        public static Find create(String title, String content, String author, long view, LocalDateTime createdAt) {
            return new Find(title, content, author, view, createdAt);
        }
    }

    @Getter
    public static final class LookUp {

        private final Article article;
        private final Page<Comment> comments;

        private LookUp(Article article, Page<Comment> comments) {
            this.article = article;
            this.comments = comments;
        }

        public static LookUp from(Article article, Page<Comment> comments) {
            return new LookUp(article, comments);
        }
    }
}
