package hello.board.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static hello.board.domain.util.EntityReflectionUtils.*;
import static org.assertj.core.api.Assertions.assertThat;

class ArticleTest {

    @Test
    void create() {
        //given
        User author = createUserByReflection("user", "user@board.com", "password");

        //when
        Article article = Article.create("title", "content", author);

        //then
        assertThat(article.getTitle())
                .as("게시글 제목")
                .isEqualTo("title");

        assertThat(article.getContent())
                .as("게시글 내용")
                .isEqualTo("content");

        assertThat(article.getAuthor())
                .as("게시글 작성자")
                .isSameAs(author);

        assertThat(article.getView())
                .as("게시글 조회수")
                .isEqualTo(0L);

        assertThat(article.getComments().isEmpty())
                .as("게시글 댓글")
                .isTrue();
    }

    @Test
    void modifyTitle() {
        //given
        User author = createUserByReflection("user", "user@board.com", "password");
        Article article = createArticleByReflection("title", "content", author);

        //when
        article.modifyTitle("new title");

        //then
        assertThat(article.getTitle())
                .as("수정된 게시글 제목")
                .isEqualTo("new title");

        assertThat(article.getContent())
                .as("게시글 내용")
                .isEqualTo("content");

        assertThat(article.getAuthor())
                .as("게시글 작성자")
                .isSameAs(author);

        assertThat(article.getView())
                .as("게시글 조회수")
                .isEqualTo(0L);

        assertThat(article.getComments().isEmpty())
                .as("게시글 댓글")
                .isTrue();
    }

    @Test
    void modifyTitle_null() {
        //given
        User author = createUserByReflection("user", "user@board.com", "password");
        Article article = createArticleByReflection("title", "content", author);

        //when
        article.modifyTitle(null);

        //then
        assertThat(article.getTitle())
                .as("수정되지 않은 게시글 제목")
                .isEqualTo("title");

        assertThat(article.getContent())
                .as("게시글 내용")
                .isEqualTo("content");

        assertThat(article.getAuthor())
                .as("게시글 작성자")
                .isSameAs(author);

        assertThat(article.getView())
                .as("게시글 조회수")
                .isEqualTo(0L);

        assertThat(article.getComments().isEmpty())
                .as("게시글 댓글")
                .isTrue();
    }

    @Test
    void modifyContent() {
        //given
        User author = createUserByReflection("user", "user@board.com", "password");
        Article article = createArticleByReflection("title", "content", author);

        //when
        article.modifyContent("new content");

        //then
        assertThat(article.getTitle())
                .as("게시글 제목")
                .isEqualTo("title");

        assertThat(article.getContent())
                .as("수정된 게시글 내용")
                .isEqualTo("new content");

        assertThat(article.getAuthor())
                .as("게시글 작성자")
                .isSameAs(author);

        assertThat(article.getView())
                .as("게시글 조회수")
                .isEqualTo(0L);

        assertThat(article.getComments().isEmpty())
                .as("게시글 댓글")
                .isTrue();
    }

    @Test
    void modifyContent_null() {
        //given
        User author = createUserByReflection("user", "user@board.com", "password");
        Article article = createArticleByReflection("title", "content", author);

        //when
        article.modifyContent(null);

        //then
        assertThat(article.getTitle())
                .as("게시글 제목")
                .isEqualTo("title");

        assertThat(article.getContent())
                .as("수정되지 않은 게시글 내용")
                .isEqualTo("content");

        assertThat(article.getAuthor())
                .as("게시글 작성자")
                .isSameAs(author);

        assertThat(article.getView())
                .as("게시글 조회수")
                .isEqualTo(0L);

        assertThat(article.getComments().isEmpty())
                .as("게시글 댓글")
                .isTrue();
    }

    @Test
    void addComment() {
        //given
        User author = createUserByReflection("user", "user@board.com", "password");
        Article article = createArticleByReflection("title", "content", author);
        Comment comment = createCommentByReflection("content", article, author);

        //when
        article.addComment(comment);

        //then
        assertThat(article.getComments().size())
                .as("댓글 수")
                .isEqualTo(1);

        assertThat(article.getComments())
                .as("댓글")
                .containsExactly(comment);
    }

    @Test
    void deleteComment() {
        //given
        User author = createUserByReflection("user", "user@board.com", "password");
        Article article = createArticleByReflection("title", "content", author);
        Comment comment = createCommentByReflection("content", article, author);

        List<Comment> comments = article.getComments();
        comments.add(comment);

        //when
        article.deleteComment(comment);

        //then
        assertThat(article.getComments().isEmpty())
                .as("댓글 없음")
                .isTrue();
    }

    @Test
    void increaseView() {
        //given
        User author = createUserByReflection("user", "user@board.com", "password");
        Article article = createArticleByReflection("title", "content", author);

        //when
        article.increaseView();

        //then
        assertThat(article.getTitle())
                .as("게시글 제목")
                .isEqualTo("title");

        assertThat(article.getContent())
                .as("게시글 내용")
                .isEqualTo("content");

        assertThat(article.getAuthor())
                .as("게시글 작성자")
                .isSameAs(author);

        assertThat(article.getView())
                .as("게시글 조회수")
                .isEqualTo(1L);

        assertThat(article.getComments().isEmpty())
                .as("게시글 댓글")
                .isTrue();
    }

    @Test
    void isAuthorId_correct() {
        //given
        User author = createUserByReflection("user", "user@board.com", "password");
        Article article = createArticleByReflection("title", "content", author);

        setIdOfUser(author, 1L);

        final Long authorId = author.getId();

        //when
        boolean result = article.isAuthorId(authorId);

        //then
        assertThat(result)
                .as("게시글 작성자의 ID가 맞는지 확인")
                .isTrue();
    }

    @Test
    void isAuthorId_other() {
        //given
        User author = createUserByReflection("user", "user@board.com", "password");
        User otherAuthor = createUserByReflection("other", "other@board.com", "other");

        setIdOfUser(author, 1L);
        setIdOfUser(otherAuthor, 2L);

        Article article = createArticleByReflection("title", "content", author);

        Long otherAuthorId = otherAuthor.getId();

        //when
        boolean result = article.isAuthorId(otherAuthorId);

        //then
        assertThat(result)
                .as("게시글 작성자의 ID가 맞는지 확인")
                .isFalse();
    }

    @Test
    void isAuthorId_wrong() {
        //given
        User author = createUserByReflection("user", "user@board.com", "password");
        Article article = createArticleByReflection("title", "content", author);

        setIdOfUser(author, 1L);

        final Long WRONG_AUTHOR_ID = 666L;

        //when
        boolean result = article.isAuthorId(WRONG_AUTHOR_ID);

        //then
        assertThat(result)
                .as("게시글 작성자의 ID가 맞는지 확인")
                .isFalse();
    }
}