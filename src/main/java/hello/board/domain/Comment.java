package hello.board.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id", updatable = false)
    private Long id;

    @Column(nullable = false)
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

    public void update(String content) {
        if (content != null) {
            this.content = content;
        }
    }

    public void deleteFromArticle() {
        article.deleteComment(this);
    }
}
