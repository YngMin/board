package hello.board.service.query;

import hello.board.domain.Article;
import hello.board.domain.Comment;
import hello.board.domain.User;
import hello.board.exception.FailToFindEntityException;
import hello.board.exception.WrongPageRequestException;
import hello.board.repository.CommentRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceUnitUtil;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.proxy.HibernateProxy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
@DataJpaTest
class CommentQueryServiceTest {

    @Autowired
    CommentQueryService commentQueryService;

    @Autowired
    EntityManager em;

    PersistenceUnitUtil persistenceUnitUtil;

    @TestConfiguration
    static class Config {

        @Bean
        CommentQueryService commentQueryService(CommentRepository commentRepository) {
            return new CommentQueryService(commentRepository);
        }
    }

    @BeforeEach
    void beforeEach() {
        persistenceUnitUtil = em.getEntityManagerFactory().getPersistenceUnitUtil();
    }

    @AfterEach
    void afterEach() {
        em.clear();
    }

    private static Predicate<Object> isNotProxy() {
        return a -> !(a instanceof HibernateProxy);
    }

    private User createUserAndPersist(String name, String email, String password) {
        User user = User.create(name, email, password);
        em.persist(user);
        return user;
    }

    private Article createArticleAndPersist(String title, String content, User user) {
        Article article = Article.create(title, content, user);
        em.persist(article);
        return article;
    }

    @Test
    @DisplayName("findById 성공")
    void findById() {
        //given
        User user = createUserAndPersist("user", "test@gmail.com", "1234");
        Article article = createArticleAndPersist("title", "content", user);

        Comment comment = Comment.create("comment", article, user);
        em.persist(comment);
        final Long id = comment.getId();

        em.flush();
        em.clear();

        //when
        Comment findComment = commentQueryService.findById(id);

        //then
        assertThat(findComment.getContent())
                .as("댓글 내용")
                .isEqualTo("comment");

        User author = findComment.getAuthor();
        assertThat(author)
                .as("작성자 페치 조인 성공")
                .isNotInstanceOf(HibernateProxy.class);

        assertThat(author.getName())
                .as("작성자 이름")
                .isEqualTo("user");
    }

    @Test
    @DisplayName("findById 실패")
    void findById_fail() {
        //given
        User user = createUserAndPersist("user", "test@gmail.com", "1234");
        Article article = createArticleAndPersist("title", "content", user);

        Comment comment = Comment.create("comment", article, user);
        em.persist(comment);

        final Long WRONG_ID = 4444L;

        em.flush();
        em.clear();

        //when & then
        assertThatThrownBy(() -> commentQueryService.findById(WRONG_ID))
                .as("존재하지 않는 댓글")
                .isInstanceOf(FailToFindEntityException.class);
    }

    @Test
    @DisplayName("게시글과 함께 조회 성공")
    void findWithArticle() {
        //given
        User user1 = createUserAndPersist("user1", "test1@gmail.com", "12341");
        User user2 = createUserAndPersist("user2", "test2@gmail.com", "12342");

        Article article = createArticleAndPersist("title", "content", user1);

        final Long articleId = article.getId();

        Comment comment = Comment.create("comment", article, user2);
        em.persist(comment);

        final Long id = comment.getId();

        em.flush();
        em.clear();

        //when
        Comment findComment = commentQueryService.findWithArticle(id, articleId);

        //then
        assertThat(findComment.getContent())
                .as("댓글 내용")
                .isEqualTo("comment");

        User author = findComment.getAuthor();
        assertThat(author)
                .as("댓글 작성자 페치 조인 성공")
                .isNotInstanceOf(HibernateProxy.class);

        assertThat(author.getName())
                .as("댓글 작성자 이름")
                .isEqualTo("user2");

        Article findCommentArticle = findComment.getArticle();
        assertThat(findCommentArticle)
                .as("게시글 페치 조인 성공")
                .isNotInstanceOf(HibernateProxy.class);

        assertThat(findCommentArticle.getTitle())
                .as("게시글 제목")
                .isEqualTo("title");

    }

    @Test
    @DisplayName("게시글과 함께 조회 실패")
    void findWithArticle_fail() {
        //given
        User user1 = createUserAndPersist("user1", "test1@gmail.com", "12341");
        User user2 = createUserAndPersist("user2", "test2@gmail.com", "12342");

        Article article1 = createArticleAndPersist("title1", "content1", user1);
        Article article2 = createArticleAndPersist("title2", "content2", user2);

        final Long article1Id = article1.getId();
        final Long article2Id = article2.getId();

        Comment comment = Comment.create("comment", article1, user2);
        em.persist(comment);

        final Long id = comment.getId();

        final Long WRONG_ID = 4444L;

        em.flush();
        em.clear();

        //when & then
        assertThatThrownBy(() -> commentQueryService.findWithArticle(WRONG_ID, article1Id))
                .as("존재하지 않는 댓글 ID")
                .isInstanceOf(FailToFindEntityException.class);

        assertThatThrownBy(() -> commentQueryService.findWithArticle(id, article2Id))
                .as("다른 게시글 ID")
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> commentQueryService.findWithArticle(id, WRONG_ID))
                .as("존재하지 않는 게시글 ID")
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> commentQueryService.findWithArticle(WRONG_ID, article2Id))
                .as("존재하지 않는 댓글 ID & 다른 게시글 ID")
                .isInstanceOf(FailToFindEntityException.class);
    }

    @Test
    @DisplayName("게시글 ID로 조회 성공")
    void findByArticleId() {
        //given
        User user1 = createUserAndPersist("user1", "test1@gmail.com", "12341");
        User user2 = createUserAndPersist("user2", "test2@gmail.com", "12342");

        Article article1 = createArticleAndPersist("title1", "content1", user1);
        Article article2 = createArticleAndPersist("title2", "content2", user2);

        Long article1Id = article1.getId();
        Long article2Id = article2.getId();


        final int NUMBER_OF_COMMENTS = 100;

        for (int i = 0; i < NUMBER_OF_COMMENTS; i++) {
            Comment comment = Comment.create("comment " + i, (i % 3 == 2) ? article1 : article2 , (i % 2 == 1) ? user1 : user2);
            em.persist(comment);
        }

        final int PAGE_1 = 0, SIZE_1 = 10;
        final int PAGE_2 = 2, SIZE_2 = 13;
        final int PAGE_3 = 3, SIZE_3 = 5;

        final PageRequest pageable1 = PageRequest.of(PAGE_1, SIZE_1);
        final PageRequest pageable2 = PageRequest.of(PAGE_2, SIZE_2);
        final PageRequest pageable3 = PageRequest.of(PAGE_3, SIZE_3);

        em.flush();
        em.clear();

        //when
        Page<Comment> commentsOfArticle1_1 = commentQueryService.findByArticleId(article1Id, pageable1);
        Page<Comment> commentsOfArticle1_2 = commentQueryService.findByArticleId(article1Id, pageable2);
        Page<Comment> commentsOfArticle1_3 = commentQueryService.findByArticleId(article1Id, pageable3);

        Page<Comment> commentsOfArticle2_1 = commentQueryService.findByArticleId(article2Id, pageable1);
        Page<Comment> commentsOfArticle2_2 = commentQueryService.findByArticleId(article2Id, pageable2);
        Page<Comment> commentsOfArticle2_3 = commentQueryService.findByArticleId(article2Id, pageable3);

        //then
        assertThat(commentsOfArticle1_1.getTotalElements())
                .as("총 댓글 수")
                .isEqualTo(33L);

        assertThat(commentsOfArticle1_2.getTotalElements())
                .as("총 댓글 수")
                .isEqualTo(33L);

        assertThat(commentsOfArticle1_3.getTotalElements())
                .as("총 댓글 수")
                .isEqualTo(33L);

        assertThat(commentsOfArticle2_1.getTotalElements())
                .as("총 댓글 수")
                .isEqualTo(67L);

        assertThat(commentsOfArticle2_2.getTotalElements())
                .as("총 댓글 수")
                .isEqualTo(67L);

        assertThat(commentsOfArticle2_3.getTotalElements())
                .as("총 댓글 수")
                .isEqualTo(67L);

        assertThat(commentsOfArticle1_1.getContent())
                .extracting("content")
                .as("댓글 내용")
                .containsExactly("comment 2", "comment 5", "comment 8", "comment 11", "comment 14", "comment 17", "comment 20", "comment 23", "comment 26", "comment 29");

        assertThat(commentsOfArticle1_2.getContent())
                .extracting("content")
                .as("댓글 내용")
                .containsExactly("comment 80", "comment 83", "comment 86", "comment 89", "comment 92", "comment 95", "comment 98");

        assertThat(commentsOfArticle1_3.getContent())
                .extracting("content")
                .as("댓글 내용")
                .containsExactly("comment 47", "comment 50", "comment 53", "comment 56", "comment 59");

        assertThat(commentsOfArticle2_1.getContent())
                .extracting("content")
                .as("댓글 내용")
                .containsExactly("comment 0", "comment 1", "comment 3", "comment 4", "comment 6", "comment 7", "comment 9", "comment 10", "comment 12", "comment 13");

        assertThat(commentsOfArticle2_2.getContent())
                .extracting("content")
                .as("댓글 내용")
                .containsExactly("comment 39", "comment 40", "comment 42", "comment 43", "comment 45", "comment 46", "comment 48", "comment 49", "comment 51", "comment 52", "comment 54", "comment 55", "comment 57");

        assertThat(commentsOfArticle2_3.getContent())
                .extracting("content")
                .as("댓글 내용")
                .containsExactly("comment 22", "comment 24", "comment 25", "comment 27", "comment 28");

        assertThat(commentsOfArticle1_1.getContent())
                .extracting("author")
                .as("작성자 페치 조인 성공")
                .allMatch(isNotProxy());

        assertThat(commentsOfArticle1_2.getContent())
                .extracting("author")
                .as("작성자 페치 조인 성공")
                .allMatch(isNotProxy());

        assertThat(commentsOfArticle1_3.getContent())
                .extracting("author")
                .as("작성자 페치 조인 성공")
                .allMatch(isNotProxy());

        assertThat(commentsOfArticle2_1.getContent())
                .extracting("author")
                .as("작성자 페치 조인 성공")
                .allMatch(isNotProxy());

        assertThat(commentsOfArticle2_2.getContent())
                .extracting("author")
                .as("작성자 페치 조인 성공")
                .allMatch(isNotProxy());

        assertThat(commentsOfArticle2_3.getContent())
                .extracting("author")
                .as("작성자 페치 조인 성공")
                .allMatch(isNotProxy());
    }
}