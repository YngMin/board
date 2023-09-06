package hello.board.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Objects;

@Entity
@Getter
@ToString(of = {"id", "content"})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id", updatable = false)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    private Comment(String content, Article article, User author) {
        this.content = content;
        this.article = article;
        this.author = author;

        article.addComment(this);
    }

    public static Comment create(String content, Article article, User author) {
        return new Comment(content, article, author);
    }

    public void modifyContent(String content) {
        if (content != null) {
            this.content = content;
        }
    }

    public void deleteFromArticle() {
        article.deleteComment(this);
    }

    public boolean isIdOfMyArticle(Long articleId) {
        return Objects.equals(article.getId(), articleId);
    }

    public boolean isIdOfAuthor(Long userId) {
        return Objects.equals(author.getId(), userId);
    }
}
