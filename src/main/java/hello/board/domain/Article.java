package hello.board.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@ToString(of = {"id", "title", "content", "view"})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Article extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "article_id", updatable = false)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    @Column(name = "views", nullable = false)
    private long view = 0L;

    @OneToMany(mappedBy = "article", cascade = CascadeType.REMOVE)
    private List<Comment> comments = new ArrayList<>();

    private Article(String title, String content, User author) {
        this.title = title;
        this.content = content;
        this.author = author;
    }

    public static Article create(String title, String content, User author) {
        return new Article(title, content, author);
    }

    public void modifyTitle(String title) {
        if (title != null) {
            this.title = title;
        }
    }

    public void modifyContent(String content) {
        if (content != null) {
            this.content = content;
        }
    }

    public void addComment(Comment comment) {
        comments.add(comment);
    }

    public void deleteComment(Comment comment) {
        comments.remove(comment);
    }

    public Article increaseView() {
        view++;
        return this;
    }

    public boolean isIdOfAuthor(Long userId) {
        return Objects.equals(author.getId(), userId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Article article)) return false;
        return Objects.equals(getId(), article.getId())
                && Objects.equals(getTitle(), article.getTitle())
                && Objects.equals(getContent(), article.getContent());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getTitle(), getContent());
    }
}
