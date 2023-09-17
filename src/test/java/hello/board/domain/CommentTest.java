package hello.board.domain;

import org.junit.jupiter.api.Test;

import static hello.board.domain.EntityReflectionUtils.*;
import static org.assertj.core.api.Assertions.assertThat;

class CommentTest {

    @Test
    void create() {
        //given
        User author = createUserByReflection("user", "user@board.com", "password");
        Article article = createArticleByReflection("title", "content", author);

        //when
        Comment comment = Comment.create("content", article, author);

        //then
        assertThat(comment.getContent())
                .as("댓글 내용")
                .isEqualTo("content");

        assertThat(comment.getAuthor())
                .as("댓글 작성자")
                .isSameAs(author);

        assertThat(comment.getArticle())
                .as("게시글")
                .isSameAs(article);

        assertThat(article.getComments().size())
                .as("게시글의 댓글 수")
                .isEqualTo(1);

        assertThat(article.getComments())
                .as("게시글의 댓글")
                .containsExactly(comment);
    }


    @Test
    void modifyContent() {
        //given
        User author = createUserByReflection("user", "user@board.com", "password");
        Article article = createArticleByReflection("title", "content", author);
        Comment comment = createCommentByReflection("content", article, author);

        article.addComment(comment);

        //when
        comment.modifyContent("new content");

        //then
        assertThat(comment.getContent())
                .as("수정된 댓글 내용")
                .isEqualTo("new content");

        assertThat(comment.getAuthor())
                .as("댓글 작성자")
                .isSameAs(author);

        assertThat(comment.getArticle())
                .as("게시글")
                .isSameAs(article);

        assertThat(article.getComments().size())
                .as("게시글의 댓글 수")
                .isEqualTo(1);

        assertThat(article.getComments())
                .as("게시글의 댓글")
                .containsExactly(comment);

    }

    @Test
    void modifyContent_null() {
        //given
        User author = createUserByReflection("user", "user@board.com", "password");
        Article article = createArticleByReflection("title", "content", author);
        Comment comment = createCommentByReflection("content", article, author);

        article.addComment(comment);

        //when
        comment.modifyContent(null);

        //then
        assertThat(comment.getContent())
                .as("수정되지 않은 댓글 내용")
                .isEqualTo("content");

        assertThat(comment.getAuthor())
                .as("댓글 작성자")
                .isSameAs(author);

        assertThat(comment.getArticle())
                .as("게시글")
                .isSameAs(article);

        assertThat(article.getComments().size())
                .as("게시글의 댓글 수")
                .isEqualTo(1);

        assertThat(article.getComments())
                .as("게시글의 댓글")
                .containsExactly(comment);

    }

    @Test
    void deleteFromArticle() {
        //given
        User author = createUserByReflection("user", "user@board.com", "password");
        Article article = createArticleByReflection("title", "content", author);

        Comment comment = createCommentByReflection("content", article, author);
        article.addComment(comment);

        //when
        comment.deleteFromArticle();

        //then
        assertThat(comment.getArticle())
                .as("댓글 -> 게시글 참조 제거")
                .isNull();

        assertThat(article.getComments().contains(comment))
                .as("게시글 -> 댓글 참조 제거")
                .isFalse();
    }

    @Test
    void isNotMyArticle_true_otherArticleId() {
        //given
        User author = createUserByReflection("user", "user@board.com", "password");
        Article article = createArticleByReflection("title", "content", author);
        Article other = createArticleByReflection("other", "other", author);
        Comment comment = createCommentByReflection("content", article, author);

        setIdOfArticle(article, 1L);
        setIdOfArticle(other, 2L);

        //when
        boolean notMyArticle = comment.isNotMyArticle(other.getId());

        //then
        assertThat(notMyArticle)
                .as("댓글이 달린 게시글의 ID가 아님")
                .isTrue();
    }

    @Test
    void isNotMyArticle_true_wrongArticleId() {
        //given
        User author = createUserByReflection("user", "user@board.com", "password");
        Article article = createArticleByReflection("title", "content", author);
        Article other = createArticleByReflection("other", "other", author);
        Comment comment = createCommentByReflection("content", article, author);

        setIdOfArticle(article, 1L);
        setIdOfArticle(other, 2L);

        final Long WRONG_ARTICLE_ID = 4444L;

        //when
        boolean notMyArticle = comment.isNotMyArticle(WRONG_ARTICLE_ID);

        //then
        assertThat(notMyArticle)
                .as("존재하지 않는 게시글 ID")
                .isTrue();
    }

    @Test
    void isNotMyArticle_false() {
        //given
        User author = createUserByReflection("user", "user@board.com", "password");
        Article article = createArticleByReflection("title", "content", author);
        Article other = createArticleByReflection("other", "other", author);
        Comment comment = createCommentByReflection("content", article, author);

        setIdOfArticle(article, 1L);
        setIdOfArticle(other, 2L);

        //when
        boolean notMyArticle = comment.isNotMyArticle(article.getId());

        //then
        assertThat(notMyArticle)
                .as("댓글이 달린 게시글의 ID가 맞음")
                .isFalse();
    }

    @Test
    void isIdOfAuthor_true() {
        //given
        User author = createUserByReflection("user", "user@board.com", "password");
        User other = createUserByReflection("other", "other@board.com", "password");

        Article article = createArticleByReflection("title", "content", author);

        Comment comment = createCommentByReflection("content", article, author);

        setIdOfUser(author, 1L);
        setIdOfUser(other, 2L);

        //when
        boolean idOfAuthor = comment.isIdOfAuthor(author.getId());

        //then
        assertThat(idOfAuthor)
                .as("작성자 ID가 맞음")
                .isTrue();
    }

    @Test
    void isIdOfAuthor_false_otherAuthorId() {
        //given
        User author = createUserByReflection("user", "user@board.com", "password");
        User other = createUserByReflection("other", "other@board.com", "password");

        Article article = createArticleByReflection("title", "content", author);

        Comment comment = createCommentByReflection("content", article, author);

        setIdOfUser(author, 1L);
        setIdOfUser(other, 2L);

        //when
        boolean idOfAuthor = comment.isIdOfAuthor(other.getId());

        //then
        assertThat(idOfAuthor)
                .as("작성자 ID가 아님")
                .isFalse();
    }

    @Test
    void isIdOfAuthor_false_wrongUserId() {
        //given
        User author = createUserByReflection("user", "user@board.com", "password");
        User other = createUserByReflection("other", "other@board.com", "password");

        Article article = createArticleByReflection("title", "content", author);

        Comment comment = createCommentByReflection("content", article, author);

        setIdOfUser(author, 1L);
        setIdOfUser(other, 2L);

        final Long WRONG_USER_ID = 4444L;

        //when
        boolean idOfAuthor = comment.isIdOfAuthor(WRONG_USER_ID);

        //then
        assertThat(idOfAuthor)
                .as("존재하지 않는 사용자 ID")
                .isFalse();
    }
}