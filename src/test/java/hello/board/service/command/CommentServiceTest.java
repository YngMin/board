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
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
@DataJpaTest
class CommentServiceTest {

    @Autowired
    CommentService commentService;

    @Autowired
    EntityManager em;

    @TestConfiguration
    static class Config {

        @Bean
        CommentService commentService(CommentRepository commentRepository, UserRepository userRepository, ArticleRepository articleRepository) {
            return new CommentService(commentRepository, articleRepository, userRepository);
        }
    }

    @AfterEach
    void afterEach() {
        em.clear();
    }

    private User createUserAndPersist(String name, String email, String password) {
        User user = User.create(name, email, password);
        em.persist(user);
        return user;
    }

    private Article createArticleAndPersist(String title, String content, User author) {
        Article article = Article.create(title, content, author);
        em.persist(article);
        return article;
    }

    @Test
    @DisplayName("저장 성공")
    void save() {
        //given
        User user = createUserAndPersist("user", "test@gmail.com", "1234");
        Article article = createArticleAndPersist("title", "content", user);

        //when
        final Long id = commentService.save(article.getId(), user.getId(), Save.create("comment"));

        em.flush();
        em.clear();

        //then
        Comment comment = em.find(Comment.class, id);

        assertThat(comment.getContent())
                .as("내용")
                .isEqualTo("comment");
    }

    @Test
    @DisplayName("저장 실패")
    void save_fail() {
        //given
        User user = createUserAndPersist("user", "test@gmail.com", "1234");
        Article article = createArticleAndPersist("title", "content", user);

        final Long WRONG_ID = 4444L;

        //when & then
        assertThatThrownBy(() -> commentService.save(WRONG_ID, user.getId(), Save.create("comment")))
                .as("존재하지 않는 게시글 ID")
                .isInstanceOf(FailToFindEntityException.class);

        assertThatThrownBy(() -> commentService.save(article.getId(), WRONG_ID, Save.create("comment")))
                .as("존재하지 않는 사용자 ID")
                .isInstanceOf(FailToFindEntityException.class);
    }

    @Test
    @DisplayName("수정 성공")
    void update() {
        //given
        User user = createUserAndPersist("user", "test@gmail.com", "1234");
        final Long userId = user.getId();
        Article article = createArticleAndPersist("title", "content", user);
        final Long articleId = article.getId();

        Comment comment = Comment.create("comment", article, user);
        em.persist(comment);
        final Long id = comment.getId();

        em.flush();
        em.clear();

        //when 1
        commentService.update(id, articleId, userId, Update.create("modifiedComment"));

        em.flush();
        em.clear();

        //then 1
        Comment updatedComment1 = em.find(Comment.class, id);

        assertThat(updatedComment1.getContent())
                .as("내용 수정")
                .isEqualTo("modifiedComment");

        //when 2
        commentService.update(id, articleId, userId, Update.create(null));

        em.flush();
        em.clear();

        //then 2
        Comment updatedComment2 = em.find(Comment.class, id);

        assertThat(updatedComment2.getContent())
                .as("내용 수정되지 않음")
                .isEqualTo("modifiedComment");

        //when 3
        commentService.update(id, articleId, userId, null);

        em.flush();
        em.clear();

        //then 3
        Comment updatedComment3 = em.find(Comment.class, id);

        assertThat(updatedComment3.getContent())
                .as("내용 수정되지 않음")
                .isEqualTo("modifiedComment");

    }

    @Test
    @DisplayName("수정 실패")
    void update_fail() {
        //given
        User user1 = createUserAndPersist("user1", "test1@gmail.com", "12341");
        final Long user1Id = user1.getId();

        User user2 = createUserAndPersist("user2", "test2@gmail.com", "12342");
        final Long user2Id = user2.getId();

        Article article1 = createArticleAndPersist("title1", "content1", user1);
        final Long article1Id = article1.getId();

        Article article2 = createArticleAndPersist("title2", "content2", user2);
        final Long article2Id = article2.getId();

        Comment comment = Comment.create("comment", article1, user1);

        em.persist(comment);

        final Long id = comment.getId();

        final Long WRONG_ID = 4444L;

        em.flush();
        em.clear();

        //when & then
        assertThatThrownBy(() -> commentService.update(WRONG_ID, article1Id, user1Id, Update.create("modifiedComment")))
                .as("존재하지 않는 댓글 ID")
                .isInstanceOf(FailToFindEntityException.class);

        assertThatThrownBy(() -> commentService.update(id, article2Id, user1Id, Update.create("modifiedComment")))
                .as("다른 게시글 ID")
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> commentService.update(id, WRONG_ID, user1Id, Update.create("modifiedComment")))
                .as("존재하지 않는 게시글 ID")
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> commentService.update(id, article1Id, user2Id, Update.create("modifiedComment")))
                .as("작성자가 아닌 사용자의 수정")
                .isInstanceOf(NoAuthorityException.class);

        assertThatThrownBy(() -> commentService.update(id, article1Id, WRONG_ID, Update.create("modifiedComment")))
                .as("작성자가 아닌 사용자의 수정 - 존재하지 않는 사용자 ID")
                .isInstanceOf(NoAuthorityException.class);

        assertThatThrownBy(() -> commentService.update(id, article2Id, user2Id, Update.create("modifiedComment")))
                .as("다른 게시글 ID & 작성자가 아닌 사용자의 ID")
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("삭제 성공")
    void delete() {
        //given
        User user = createUserAndPersist("user", "test@gmail.com", "1234");
        final Long userId = user.getId();
        Article article = createArticleAndPersist("title", "content", user);
        final Long articleId = article.getId();

        Comment comment = Comment.create("comment", article, user);
        em.persist(comment);
        final Long id = comment.getId();

        em.flush();
        em.clear();

        //when
        commentService.delete(id, articleId, userId);

        em.flush();
        em.clear();

        //then
        Comment findComment = em.find(Comment.class, id);

        assertThat(findComment)
                .as("삭제된 댓글")
                .isNull();
    }

    @Test
    @DisplayName("삭제 실패")
    void delete_fail() {
        //given
        User user1 = createUserAndPersist("user1", "test1@gmail.com", "12341");
        final Long user1Id = user1.getId();

        User user2 = createUserAndPersist("user2", "test2@gmail.com", "12342");
        final Long user2Id = user2.getId();

        Article article1 = createArticleAndPersist("title1", "content1", user1);
        final Long article1Id = article1.getId();

        Article article2 = createArticleAndPersist("title2", "content2", user2);
        final Long article2Id = article2.getId();

        Comment comment = Comment.create("comment", article1, user1);

        em.persist(comment);

        final Long id = comment.getId();

        final Long WRONG_ID = 4444L;

        em.flush();
        em.clear();

        //when & then
        assertThatThrownBy(() -> commentService.delete(WRONG_ID, article1Id, user1Id))
                .as("존재하지 않는 댓글 ID")
                .isInstanceOf(FailToFindEntityException.class);

        assertThatThrownBy(() -> commentService.delete(id, article2Id, user1Id))
                .as("다른 게시글 ID")
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> commentService.delete(id, WRONG_ID, user1Id))
                .as("존재하지 않는 게시글 ID")
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> commentService.delete(id, article1Id, user2Id))
                .as("작성자가 아닌 사용자의 삭제")
                .isInstanceOf(NoAuthorityException.class);

        assertThatThrownBy(() -> commentService.delete(id, article1Id, WRONG_ID))
                .as("작성자가 아닌 사용자의 삭제 - 존재하지 않는 사용자 ID")
                .isInstanceOf(NoAuthorityException.class);

        assertThatThrownBy(() -> commentService.delete(id, article2Id, user2Id))
                .as("다른 게시글 ID & 작성자가 아닌 사용자의 ID")
                .isInstanceOf(IllegalArgumentException.class);
    }

}