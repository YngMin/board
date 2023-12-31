package hello.board.dto.view;

import hello.board.domain.Article;
import hello.board.domain.Comment;
import hello.board.dto.service.ArticleServiceDto;
import hello.board.dto.service.search.ArticleSearchDto;
import lombok.*;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ArticleResponse {

    @Getter
    @EqualsAndHashCode
    public static final class View {

        private final Long id;
        private final String title;
        private final String content;
        private final String author;
        private final LocalDateTime createdAt;
        private final Long view;
        private final Page<CommentViewResponse> comments;

        @Builder
        private View(Long id, String title, String content, String author, LocalDateTime createdAt, Long view, Page<CommentViewResponse> comments) {
            this.id = id;
            this.title = title;
            this.content = content;
            this.author = author;
            this.createdAt = createdAt;
            this.view = view;
            this.comments = comments;
        }

        public static View of(ArticleServiceDto.LookUp param) {

            Article article = param.getArticle();
            Page<Comment> comments = param.getComments();

            return View.builder()
                    .id(article.getId())
                    .title(article.getTitle())
                    .content(article.getContent())
                    .author(article.getAuthor().getName())
                    .createdAt(article.getCreatedAt())
                    .view(article.getView())
                    .comments(comments.map(CommentViewResponse::of))
                    .build();
        }
    }

    @Getter
    @EqualsAndHashCode
    public static final class ListView {

        private final Long id;
        private final String title;
        private final String author;
        private final LocalDateTime createdAt;
        private final Long view;
        private final Long numComments;

        @Builder
        private ListView(Long id, String title, String author, LocalDateTime createdAt, Long view, Long numComments) {
            this.id = id;
            this.title = title;
            this.author = author;
            this.createdAt = createdAt;
            this.view = view;
            this.numComments = numComments;
        }

        public static ListView from(ArticleSearchDto param) {
            Article article = param.getArticle();

            return ListView.builder()
                    .id(article.getId())
                    .title(article.getTitle())
                    .author(article.getAuthor().getName())
                    .createdAt(article.getCreatedAt())
                    .view(article.getView())
                    .numComments(param.getNumComments())
                    .build();
        }
    }

    @Getter
    @EqualsAndHashCode
    public static final class Write {

        private final Long id;
        private final String title;
        private final String content;

        private Write(Long id, String title, String content) {
            this.id = id;
            this.title = title;
            this.content = content;
        }

        public static Write empty() {
            return new Write(null, "", "");
        }

        public static Write from(Article article) {
            return new Write(article.getId(), article.getTitle(), article.getContent());
        }
    }

}
