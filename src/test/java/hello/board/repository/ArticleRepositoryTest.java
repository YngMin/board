package hello.board.repository;

import hello.board.domain.Article;
import hello.board.domain.Comment;
import hello.board.domain.User;
import hello.board.dto.service.ArticleCommentFlatDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceUnitUtil;
import org.hibernate.proxy.HibernateProxy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class ArticleRepositoryTest {

    @Autowired
    ArticleRepository articleRepository;

    @Autowired
    EntityManager em;

    PersistenceUnitUtil persistenceUnitUtil;

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


    @Test
    @DisplayName("findById 성공")
    void findById() {
        //given
        User user = createUserAndPersist("user", "test@gmail.com", "1234");

        Article article = Article.create("title", "content", user);
        em.persist(article);

        final Long id = article.getId();

        em.flush();
        em.clear();

        //when
        Article findArticle = articleRepository.findById(id).orElseThrow();

        //then
        //article
        assertThat(findArticle.getTitle())
                .as("제목")
                .isEqualTo("title");

        assertThat(findArticle.getContent())
                .as("내용")
                .isEqualTo("content");

        assertThat(findArticle.getView())
                .as("조회수")
                .isEqualTo(0L);

        //article.author
        assertThat(article.getAuthor())
                .as("작성자 페치 조인 성공")
                .isNotInstanceOf(HibernateProxy.class);
    }

    @Test
    @DisplayName("findById 실패")
    void findById_fail() {
        //given
        User user = createUserAndPersist("user", "test@gmail.com", "1234");

        Article article = Article.create("title", "content", user);
        em.persist(article);

        final Long WRONG_ID = 4444L;

        em.flush();
        em.clear();

        //when
        Optional<Article> articleOptional = articleRepository.findById(WRONG_ID);

        //then
        assertThat(articleOptional.isEmpty())
                .as("존재하지 않는 게시글")
                .isTrue();
    }

    @Test
    @DisplayName("댓글과 함께 조회 성공")
    void findWithComments_noPaging() {
        //given
        User user1 = createUserAndPersist("user1", "test1@gmail.com", "12341");
        User user2 = createUserAndPersist("user2", "test2@gmail.com", "12342");

        Article article = Article.create("title", "content", user1);
        em.persist(article);
        final Long id = article.getId();

        Comment comment1 = Comment.create("comment1", article, user1);
        Comment comment2 = Comment.create("comment2", article, user2);

        em.persist(comment1);
        em.persist(comment2);

        em.flush();
        em.clear();

        //when
        Article findArticle = articleRepository.findWithComments(id).orElseThrow();

        //then
        //article
        assertThat(findArticle.getTitle())
                .as("제목")
                .isEqualTo("title");

        assertThat(findArticle.getContent())
                .as("내용")
                .isEqualTo("content");

        assertThat(findArticle.getView())
                .as("조회수")
                .isEqualTo(0L);

        //article.author
        assertThat(article.getAuthor())
                .as("작성자 페치 조인 성공")
                .isNotInstanceOf(HibernateProxy.class);

        //article.comments
        List<Comment> comments = findArticle.getComments();

        assertThat(persistenceUnitUtil.isLoaded(comments))
                .as("댓글 페치 조인 성공")
                .isTrue();

        assertThat(comments.size())
                .as("조회된 댓글 수")
                .isEqualTo(2);

        //article.comments[*].author
        assertThat(comments)
                .extracting("author")
                .as("댓글 작성자 페치 조인 성공")
                .allMatch(isNotProxy());
    }

    @Test
    @DisplayName("댓글과 함께 조회 실패")
    void findWithComments_noPaging_fail() {
        //given
        User user1 = createUserAndPersist("user1", "test1@gmail.com", "12341");
        User user2 = createUserAndPersist("user2", "test2@gmail.com", "12342");

        Article article = Article.create("title", "content", user1);
        em.persist(article);

        Comment comment1 = Comment.create("comment1", article, user1);
        Comment comment2 = Comment.create("comment2", article, user2);

        em.persist(comment1);
        em.persist(comment2);

        final Long WRONG_ID = 666L;

        em.flush();
        em.clear();

        //when
        Optional<Article> articleOptional = articleRepository.findById(WRONG_ID);

        //then
        assertThat(articleOptional.isEmpty())
                .as("존재하지 않는 게시글")
                .isTrue();
    }


    @Test
    @DisplayName("페이징된 댓글과 함께 조회 성공")
    void findWithComments_paging() {
        //given
        User user1 = createUserAndPersist("user1", "test1@gmail.com", "12341");
        User user2 = createUserAndPersist("user2", "test2@gmail.com", "12342");

        Article article = Article.create("title", "content", user1);
        em.persist(article);
        final Long id = article.getId();

        final int NUMBER_OF_COMMENTS = 123;
        final int PAGE = 2, SIZE = 20;
        final Pageable pageable = PageRequest.of(PAGE, SIZE);

        for (int i = 0; i < NUMBER_OF_COMMENTS; i++) {
            Comment comment = Comment.create("comment " + i, article, (i % 2 == 1) ? user1 : user2);
            em.persist(comment);
        }

        em.flush();
        em.clear();

        //when
        Page<ArticleCommentFlatDto> flatDtos = articleRepository.findWithComments(id, pageable);

        // then
        final List<ArticleCommentFlatDto> content = flatDtos.getContent();

        //article
        assertThat(content)
                .extracting("article")
                .as("동일한 article 엔티티")
                .allMatch(isSameArticle(id));

        Article findArticle = content.stream()
                .map(ArticleCommentFlatDto::getArticle)
                .findAny()
                .orElseThrow();

        assertThat(findArticle.getTitle())
                .as("제목")
                .isEqualTo("title");

        assertThat(findArticle.getContent())
                .as("내용")
                .isEqualTo("content");

        assertThat(findArticle.getView())
                .as("조회수")
                .isEqualTo(0L);

        //article.author
        assertThat(findArticle.getAuthor())
                .as("게시글 작성자 페치 조인 성공")
                .isNotInstanceOf(HibernateProxy.class);

        //article.comments[*].author
        assertThat(content)
                .extracting("comment")
                .extracting("author")
                .as("댓글 작성자 페치 조인 성공")
                .allMatch(isNotProxy());

    }

    @Test
    @DisplayName("페이징된 댓글과 함께 조회 실패")
    void findWithComments_paging_fail() {
        //given
        User user1 = createUserAndPersist("user1", "test1@gmail.com", "12341");
        User user2 = createUserAndPersist("user2", "test2@gmail.com", "12342");

        Article article = Article.create("title", "content", user1);
        em.persist(article);

        final Long WRONG_ID = 666L;
        final int NUMBER_OF_COMMENTS = 123;
        final int PAGE = 2, SIZE = 20;
        final Pageable pageable = PageRequest.of(PAGE, SIZE);

        for (int i = 0; i < NUMBER_OF_COMMENTS; i++) {
            Comment comment = Comment.create("comment " + i, article, (i % 2 == 1) ? user1 : user2);
            em.persist(comment);
        }

        em.flush();
        em.clear();

        //when
        Page<ArticleCommentFlatDto> flatDtos = articleRepository.findWithComments(WRONG_ID, pageable);

        // then
        assertThat(flatDtos.isEmpty())
                .as("존재하지 않는 게시글")
                .isTrue();
    }

    private static Predicate<Object> isSameArticle(Long id) {
        return o -> {
            if (o instanceof Article findArticle) {
                return Objects.equals(findArticle.getId(), id);
            }
            return false;
        };
    }
}