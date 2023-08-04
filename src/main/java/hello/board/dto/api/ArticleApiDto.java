package hello.board.dto.api;

import hello.board.domain.Article;
import hello.board.dto.service.ArticleServiceDto;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

public abstract class ArticleApiDto {
    @Getter
    @Setter
    public static class SaveRequest {

        @NotBlank
        private String title;

        @NotBlank
        private String content;

        public ArticleServiceDto.Save toDto() {
            return ArticleServiceDto.Save.create(title, content);
        }
    }

    @Getter
    public static class SaveResponse {

        private final Long id;
        private SaveResponse(Long id) {
            this.id = id;
        }
        public static SaveResponse create(Long id) {
            return new SaveResponse(id);
        }
    }

    @Getter
    public static class FindResponse {

        private final String title;
        private final String content;
        private final String author;
        private final Long view;
        private final List<CommentApiDto.FindResponse> comments;
        private final LocalDateTime createdAt;


        @Builder
        private FindResponse(String title, String content, String author, Long view, List<CommentApiDto.FindResponse> comments, LocalDateTime createdAt) {
            this.title = title;
            this.content = content;
            this.author = author;
            this.view = view;
            this.comments = comments;
            this.createdAt = createdAt;
        }

        public static FindResponse from(Article article) {
            List<CommentApiDto.FindResponse> comments = article.getComments().stream()
                    .map(CommentApiDto.FindResponse::from)
                    .toList();

            return FindResponse.builder()
                    .title(article.getTitle())
                    .content(article.getContent())
                    .author(article.getAuthor().getUsername())
                    .view(article.getView())
                    .comments(comments)
                    .createdAt(article.getCreatedAt())
                    .build();
        }
    }

    @Getter
    public static class FindListResponse {

        private final String title;
        private final String content;
        private final String author;
        private final Long view;
        private final LocalDateTime createdAt;


        @Builder
        private FindListResponse(String title, String content, String author, Long view, LocalDateTime createdAt) {
            this.title = title;
            this.content = content;
            this.author = author;
            this.view = view;
            this.createdAt = createdAt;
        }

        public static FindListResponse from(Article article) {
            return FindListResponse.builder()
                    .title(article.getTitle())
                    .content(article.getContent())
                    .author(article.getAuthor().getUsername())
                    .view(article.getView())
                    .createdAt(article.getCreatedAt())
                    .build();
        }
    }

    @Getter
    @Setter
    public static class UpdateRequest {

        @NotBlank
        private String title;

        @NotBlank
        private String content;

        public ArticleServiceDto.Update toDto() {
            return ArticleServiceDto.Update.create(title, content);
        }
    }

    @Getter
    public static class UpdateResponse {

        private final String title;
        private final String content;
        private final LocalDateTime modifiedAt;

        @Builder
        public UpdateResponse(String title, String content, LocalDateTime modifiedAt) {
            this.title = title;
            this.content = content;
            this.modifiedAt = modifiedAt;
        }

        public static UpdateResponse from(Article article) {
            return UpdateResponse.builder()
                    .title(article.getTitle())
                    .content(article.getContent())
                    .modifiedAt(article.getModifiedAt())
                    .build();
        }
    }

}
