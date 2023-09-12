package hello.board.service.command;

import hello.board.domain.Article;
import hello.board.domain.Comment;
import hello.board.domain.User;
import hello.board.dto.service.CommentServiceDto.Save;
import hello.board.dto.service.CommentServiceDto.Update;
import hello.board.exception.FailToFindEntityException;
import hello.board.exception.NoAuthorityException;
import hello.board.repository.ArticleRepository;
import hello.board.repository.CommentRepository;
import hello.board.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class CommentServiceTest {

    @Autowired
    CommentService commentService;
    
    @Autowired
    UserRepository userRepository;
    
    @Autowired
    ArticleRepository articleRepository;
    
    @Autowired
    CommentRepository commentRepository;


    @TestConfiguration
    static class Config {

        @Bean
        CommentService commentService(CommentRepository commentRepository, UserRepository userRepository, ArticleRepository articleRepository) {
            return new CommentService(commentRepository, articleRepository, userRepository);
        }
    }

    private User createAndSaveUser(String name, String email, String password) {
        User user = User.create(name, email, password);
        userRepository.save(user);
        return user;
    }

    private Article createAndSaveArticle(String title, String content, User author) {
        Article article = Article.create(title, content, author);
        articleRepository.save(article);
        return article;
    }

    @Test
    @DisplayName("저장 성공")
    void save() {
        //given
        User author = createAndSaveUser("author", "author@board.com", "");
        Article article = createAndSaveArticle("title", "content", author);

        final Long authorId = author.getId();
        final Long articleId = article.getId();

        //when
        final Long id = commentService.save(articleId, authorId, Save.create("comment"));
        
        //then
        Comment comment = commentRepository.findById(id).orElseThrow();

        assertThat(comment.getContent())
                .as("내용")
                .isEqualTo("comment");
        
        assertThat(comment.getAuthor())
                .as("작성자")
                .isEqualTo(author);
    }

    @Test
    @DisplayName("저장 실패 - 존재하지 않는 게시글 ID")
    void save_fail_wrongArticleId() {
        //given
        User author = createAndSaveUser("author", "author@board.com", "");
        Article article = createAndSaveArticle("title", "content", author);
        
        final Long authorId = author.getId();
        final Long WRONG_ID = 4444L;

        //when & then
        assertThatThrownBy(() -> commentService.save(WRONG_ID, authorId, Save.create("comment")))
                .as("존재하지 않는 게시글 ID")
                .isInstanceOf(FailToFindEntityException.class);
    }

    @Test
    @DisplayName("저장 실패 - 존재하지 않는 사용자 ID")
    void save_fail_wrongUserId() {
        //given
        User author = createAndSaveUser("author", "author@board.com", "");
        Article article = createAndSaveArticle("title", "content", author);

        final Long articleId = article.getId();
        final Long WRONG_ID = 4444L;

        //when & then
        assertThatThrownBy(() -> commentService.save(articleId, WRONG_ID, Save.create("comment")))
                .as("존재하지 않는 사용자 ID")
                .isInstanceOf(FailToFindEntityException.class);
    }

    @Test
    @DisplayName("수정 성공 - 내용")
    void update_content() {
        //given
        User author = createAndSaveUser("author", "author@board.com", "");
        Article article = createAndSaveArticle("title", "content", author);

        final Long authorId = author.getId();
        final Long articleId = article.getId();

        Comment comment = Comment.create("comment", article, author);
        commentRepository.save(comment);

        final Long id = comment.getId();

        //when
        commentService.update(id, articleId, authorId, Update.create("commentUpdate"));

        //then
        Comment updatedComment = commentRepository.findById(id).orElseThrow();

        assertThat(updatedComment.getContent())
                .as("내용 수정")
                .isEqualTo("commentUpdate");
    }

    @Test
    @DisplayName("수정 성공 - 빈 UpdateDto")
    void update_content_empty() {
        //given
        User author = createAndSaveUser("author", "author@board.com", "");
        Article article = createAndSaveArticle("title", "content", author);

        final Long authorId = author.getId();
        final Long articleId = article.getId();

        Comment comment = Comment.create("comment", article, author);
        commentRepository.save(comment);

        final Long id = comment.getId();

        //when
        commentService.update(id, articleId, authorId, Update.create(null));

        //then
        Comment updatedComment = commentRepository.findById(id).orElseThrow();

        assertThat(updatedComment.getContent())
                .as("내용 수정되지 않음")
                .isEqualTo("comment");
    }

    @Test
    @DisplayName("수정 성공 - null")
    void update_content_null() {
        //given
        User author = createAndSaveUser("author", "author@board.com", "");
        Article article = createAndSaveArticle("title", "content", author);

        final Long authorId = author.getId();
        final Long articleId = article.getId();

        Comment comment = Comment.create("comment", article, author);
        commentRepository.save(comment);

        final Long id = comment.getId();

        //when
        commentService.update(id, articleId, authorId, null);

        //then
        Comment updatedComment = commentRepository.findById(id).orElseThrow();

        assertThat(updatedComment.getContent())
                .as("내용 수정되지 않음")
                .isEqualTo("comment");
    }

    @Test
    @DisplayName("수정 실패 - 존재하지 않는 댓글 ID로 수정 시도")
    void update_fail_wrongId() {
        //given
        User author = createAndSaveUser("author", "author@board.com", "");
        Article article = createAndSaveArticle("title", "content", author);

        final Long authorId = author.getId();
        final Long articleId = article.getId();

        Comment comment = Comment.create("comment", article, author);
        commentRepository.save(comment);

        final Long WRONG_ID = 4444L;

        //when & then
        assertThatThrownBy(() -> commentService.update(WRONG_ID, articleId, authorId, Update.create("")))
                .as("존재하지 않는 댓글 ID")
                .isInstanceOf(FailToFindEntityException.class);
    }

    @Test
    @DisplayName("수정 실패 - 다른 게시글 ID로 수정 시도")
    void update_fail_otherArticleId() {
        //given
        User author = createAndSaveUser("author", "author@board.com", "");
        Article article = createAndSaveArticle("title", "content", author);
        Article other = createAndSaveArticle("other", "other", author);

        final Long authorId = author.getId();
        final Long OTHER_ARTICLE_ID = other.getId();

        Comment comment = Comment.create("comment", article, author);
        commentRepository.save(comment);

        final Long id = comment.getId();

        //when & then
        assertThatThrownBy(() -> commentService.update(id, OTHER_ARTICLE_ID, authorId, Update.create("")))
                .as("다른 게시글 ID로 수정")
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("수정 실패 - 존재하지 않는 게시글 ID로 수정 시도")
    void update_fail_wrongArticleId() {
        //given
        User author = createAndSaveUser("author", "author@board.com", "");
        Article article = createAndSaveArticle("title", "content", author);

        final Long authorId = author.getId();
        final Long WRONG_ARTICLE_ID = 4444L;

        Comment comment = Comment.create("comment", article, author);
        commentRepository.save(comment);

        final Long id = comment.getId();

        //when & then
        assertThatThrownBy(() -> commentService.update(id, WRONG_ARTICLE_ID, authorId, Update.create("")))
                .as("존재하지 않는 게시글 ID로 수정")
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("수정 실패 - 작성자가 아닌 사용자의 수정 시도")
    void update_fail_noAuthority() {
        //given
        User author = createAndSaveUser("author", "author@board.com", "");
        User other = createAndSaveUser("other", "other@board.com", "");
        Article article = createAndSaveArticle("title", "content", author);

        final Long OTHER_USER_ID = other.getId();
        final Long articleId = article.getId();

        Comment comment = Comment.create("comment", article, author);
        commentRepository.save(comment);

        final Long id = comment.getId();

        //when & then
        assertThatThrownBy(() -> commentService.update(id, articleId, OTHER_USER_ID, Update.create("")))
                .as("작성자가 아닌 사용자의 수정")
                .isInstanceOf(NoAuthorityException.class);
    }

    @Test
    @DisplayName("수정 실패 - 존재하지 않는 사용자 ID로 수정 시도")
    void update_fail_wrongUserId() {
        //given
        User author = createAndSaveUser("author", "author@board.com", "");
        Article article = createAndSaveArticle("title", "content", author);

        final Long WRONG_USER_ID = 4444L;
        final Long articleId = article.getId();

        Comment comment = Comment.create("comment", article, author);
        commentRepository.save(comment);

        final Long id = comment.getId();

        //when & then
        assertThatThrownBy(() -> commentService.update(id, articleId, WRONG_USER_ID, Update.create("")))
                .as("존재하지 않는 사용자 ID로 수정")
                .isInstanceOf(NoAuthorityException.class);
    }

    @Test
    @DisplayName("수정 실패 - 작성자가 아닌 사용자가 다른 게시글 ID로 수정 시도")
    void update_fail_otherUser_otherAuthor() {
        //given
        User author = createAndSaveUser("author", "author@board.com", "");
        User otherUser = createAndSaveUser("other", "other@board.com", "");
        Article article = createAndSaveArticle("title", "content", author);
        Article otherArticle = createAndSaveArticle("other", "other", otherUser);

        final Long OTHER_USER_ID = otherUser.getId();
        final Long OTHER_ARTICLE_ID = otherArticle.getId();

        Comment comment = Comment.create("comment", article, author);
        commentRepository.save(comment);

        final Long id = comment.getId();

        //when & then
        assertThatThrownBy(() -> commentService.update(id, OTHER_ARTICLE_ID, OTHER_USER_ID, Update.create("")))
                .as("작성자가 아닌 사용자 + 다른 게시글 수정 ")
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("삭제 성공")
    void delete() {
        //given
        User author = createAndSaveUser("author", "author@board.com", "");
        Article article = createAndSaveArticle("title", "content", author);

        final Long authorId = author.getId();
        final Long articleId = article.getId();

        Comment comment = Comment.create("comment", article, author);
        commentRepository.save(comment);

        final Long id = comment.getId();

        //when
        commentService.delete(id, articleId, authorId);

        //then
        Optional<Comment> commentOptional = commentRepository.findById(id);

        assertThat(commentOptional.isEmpty())
                .as("삭제된 댓글")
                .isTrue();
    }

    @Test
    @DisplayName("삭제 실패 - 존재하지 않는 댓글 ID로 삭제 시도")
    void delete_fail_wrongId() {
        //given
        User author = createAndSaveUser("author", "author@board.com", "");
        Article article = createAndSaveArticle("title", "content", author);

        final Long authorId = author.getId();
        final Long articleId = article.getId();

        Comment comment = Comment.create("comment", article, author);
        commentRepository.save(comment);

        final Long WRONG_ID = 4444L;

        //when & then
        assertThatThrownBy(() -> commentService.delete(WRONG_ID, articleId, authorId))
                .as("존재하지 않는 댓글 ID")
                .isInstanceOf(FailToFindEntityException.class);
    }

    @Test
    @DisplayName("삭제 실패 - 다른 게시글 ID로 삭제 시도")
    void delete_fail_otherArticleId() {
        //given
        User author = createAndSaveUser("author", "author@board.com", "");
        Article article = createAndSaveArticle("title", "content", author);
        Article other = createAndSaveArticle("other", "other", author);

        final Long authorId = author.getId();
        final Long OTHER_ARTICLE_ID = other.getId();

        Comment comment = Comment.create("comment", article, author);
        commentRepository.save(comment);

        final Long id = comment.getId();

        //when & then
        assertThatThrownBy(() -> commentService.delete(id, OTHER_ARTICLE_ID, authorId))
                .as("다른 게시글 ID로 삭제")
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("삭제 실패 - 존재하지 않는 게시글 ID로 삭제 시도")
    void delete_fail_wrongArticleId() {
        //given
        User author = createAndSaveUser("author", "author@board.com", "");
        Article article = createAndSaveArticle("title", "content", author);

        final Long authorId = author.getId();
        final Long WRONG_ARTICLE_ID = 4444L;

        Comment comment = Comment.create("comment", article, author);
        commentRepository.save(comment);

        final Long id = comment.getId();

        //when & then
        assertThatThrownBy(() -> commentService.delete(id, WRONG_ARTICLE_ID, authorId))
                .as("존재하지 않는 게시글 ID로 삭제")
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("삭제 실패 - 작성자가 아닌 사용자의 삭제 시도")
    void delete_fail_noAuthority() {
        //given
        User author = createAndSaveUser("author", "author@board.com", "");
        User other = createAndSaveUser("other", "other@board.com", "");
        Article article = createAndSaveArticle("title", "content", author);

        final Long OTHER_USER_ID = other.getId();
        final Long articleId = article.getId();

        Comment comment = Comment.create("comment", article, author);
        commentRepository.save(comment);

        final Long id = comment.getId();

        //when & then
        assertThatThrownBy(() -> commentService.delete(id, articleId, OTHER_USER_ID))
                .as("작성자가 아닌 사용자의 삭제")
                .isInstanceOf(NoAuthorityException.class);
    }

    @Test
    @DisplayName("삭제 실패 - 존재하지 않는 사용자 ID로 삭제 시도")
    void delete_fail_wrongUserId() {
        //given
        User author = createAndSaveUser("author", "author@board.com", "");
        Article article = createAndSaveArticle("title", "content", author);

        final Long WRONG_USER_ID = 4444L;
        final Long articleId = article.getId();

        Comment comment = Comment.create("comment", article, author);
        commentRepository.save(comment);

        final Long id = comment.getId();

        //when & then
        assertThatThrownBy(() -> commentService.delete(id, articleId, WRONG_USER_ID))
                .as("존재하지 않는 사용자 ID로 삭제")
                .isInstanceOf(NoAuthorityException.class);
    }

    @Test
    @DisplayName("삭제 실패 - 작성자가 아닌 사용자가 다른 게시글 ID로 삭제 시도")
    void delete_fail_otherUser_otherAuthor() {
        //given
        User author = createAndSaveUser("author", "author@board.com", "");
        User otherUser = createAndSaveUser("other", "other@board.com", "");
        Article article = createAndSaveArticle("title", "content", author);
        Article otherArticle = createAndSaveArticle("other", "other", otherUser);

        final Long OTHER_USER_ID = otherUser.getId();
        final Long OTHER_ARTICLE_ID = otherArticle.getId();

        Comment comment = Comment.create("comment", article, author);
        commentRepository.save(comment);

        final Long id = comment.getId();

        //when & then
        assertThatThrownBy(() -> commentService.delete(id, OTHER_ARTICLE_ID, OTHER_USER_ID))
                .as("작성자가 아닌 사용자 + 다른 게시글 삭제")
                .isInstanceOf(IllegalArgumentException.class);
    }
}