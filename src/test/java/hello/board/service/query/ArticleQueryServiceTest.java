package hello.board.service.query;

import hello.board.domain.Article;
import hello.board.domain.Comment;
import hello.board.domain.User;
import hello.board.dto.service.search.ArticleSearchDto;
import hello.board.exception.FailToFindEntityException;
import hello.board.exception.WrongPageRequestException;
import hello.board.repository.ArticleRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceUnitUtil;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.proxy.HibernateProxy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
@ExtendWith(SpringExtension.class)
@DataJpaTest
@Transactional
class ArticleQueryServiceTest {

    @Autowired
    ArticleQueryService articleQueryService;

    @Autowired
    EntityManager em;

    PersistenceUnitUtil persistenceUnitUtil;

    @TestConfiguration
    static class Config {

        @Bean
        ArticleQueryService articleQueryService(ArticleRepository articleRepository) {
            return new ArticleQueryService(articleRepository);
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

    private User createUserAndPersist(String name, String email, String password) {
        User user = User.create(name, email, password);
        em.persist(user);
        return user;
    }

    private static Predicate<Object> isNotProxy() {
        return a -> !(a instanceof HibernateProxy);
    }

    @Test
    void findById() {
        //given
        User user = createUserAndPersist("user", "test@gmail.com", "1234");

        Article article = Article.create("title", "content", user);
        em.persist(article);
        final Long id = article.getId();

        em.flush();
        em.clear();

        //when
        Article findArticle = articleQueryService.findById(id);

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
        User author = article.getAuthor();
        assertThat(article)
                .as("작성자 페치 조인 성공")
                .isNotInstanceOf(HibernateProxy.class);

        assertThat(author.getName())
                .as("작성자 이름")
                .isEqualTo("user");

    }

    @Test
    void findById_fail() {
        //given
        User user = createUserAndPersist("user", "test@gmail.com", "1234");

        Article article = Article.create("title", "content", user);
        em.persist(article);

        final Long id = article.getId();

        final Long WRONG_ID = 4444L;

        em.flush();
        em.clear();

        //when
        assertThatThrownBy(() -> articleQueryService.findById(WRONG_ID))
                .as("존재하지 않는 게시글")
                .isInstanceOf(FailToFindEntityException.class);
    }

    @Test
    void findWithComments() {
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
        Article findArticle = articleQueryService.findWithComments(id);

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
        User author = article.getAuthor();
        assertThat(author)
                .as("작성자 페치 조인 성공")
                .isNotInstanceOf(HibernateProxy.class);

        assertThat(author.getName())
                .as("작성자 이름")
                .isEqualTo("user1");

        //article.comments
        List<Comment> comments = findArticle.getComments();
        assertThat(persistenceUnitUtil.isLoaded(comments))
                .as("댓글 페치 조인 성공")
                .isTrue();

        assertThat(comments)
                .extracting("content")
                .as("댓글 내용")
                .containsExactly("comment1", "comment2");

        assertThat(comments)
                .extracting("author")
                .as("댓글 작성자 페치 조인 성공")
                .allMatch(isNotProxy());

        assertThat(comments)
                .extracting("author")
                .extracting("name")
                .as("댓글 작성자 이름")
                .containsExactly("user1", "user2");
    }

    @Test
    void findWithComments_fail() {
        //given
        User user1 = createUserAndPersist("user1", "test1@gmail.com", "12341");
        User user2 = createUserAndPersist("user2", "test2@gmail.com", "12342");

        Article article = Article.create("title", "content", user1);
        em.persist(article);

        final Long WRONG_ID = 4444L;

        Comment comment1 = Comment.create("comment1", article, user1);
        Comment comment2 = Comment.create("comment2", article, user2);

        em.persist(comment1);
        em.persist(comment2);

        em.flush();
        em.clear();

        //when & then
        assertThatThrownBy(() -> articleQueryService.findWithComments(WRONG_ID))
                .as("존재하지 않는 게시글")
                .isInstanceOf(FailToFindEntityException.class);
    }

    @Test
    void search() {
        //given
        User user1 = createUserAndPersist("user1", "test1@gmail.com", "12341");
        User user2 = createUserAndPersist("user2", "test2@gmail.com", "12342");

        final int NUMBER_OF_ARTICLES = 123;

        for (int i = 0; i < NUMBER_OF_ARTICLES; i++) {
            Article article = Article.create("title " + i, "content " + i, (i % 2 == 1) ? user1 : user2);
            em.persist(article);

            for (int j = 0; j < i; j++) {
                Comment comment = Comment.create("comment " + i, article, (j % 2 == 1) ? user1 : user2);
                em.persist(comment);
            }
        }

        final int PAGE_1 = 0, SIZE_1 = 10;
        final int PAGE_2 = 2, SIZE_2 = 20;
        final int PAGE_3 = 19, SIZE_3 = 5;

        em.flush();
        em.clear();

        //when
        Page<ArticleSearchDto> result1 = articleQueryService.search(PAGE_1, SIZE_1);
        Page<ArticleSearchDto> result2 = articleQueryService.search(PAGE_2, SIZE_2);
        Page<ArticleSearchDto> result3 = articleQueryService.search(PAGE_3, SIZE_3);

        //then
        assertThat(result1.getTotalElements())
                .as("총 게시물 수")
                .isEqualTo(NUMBER_OF_ARTICLES);

        assertThat(result1.getContent())
                .extracting("article")
                .extracting("title")
                .as("게시물 제목")
                .containsExactly(getTitles(NUMBER_OF_ARTICLES, PAGE_1, SIZE_1));

        assertThat(result2.getContent())
                .extracting("article")
                .extracting("title")
                .as("게시물 제목")
                .containsExactly(getTitles(NUMBER_OF_ARTICLES, PAGE_2, SIZE_2));

        assertThat(result3.getContent())
                .extracting("article")
                .extracting("title")
                .as("게시물 제목")
                .containsExactly(getTitles(NUMBER_OF_ARTICLES, PAGE_3, SIZE_3));

        assertThat(result1.getContent())
                .extracting("article")
                .extracting("content")
                .as("게시물 내용")
                .containsExactly(getContents(NUMBER_OF_ARTICLES, PAGE_1, SIZE_1));

        assertThat(result2.getContent())
                .extracting("article")
                .extracting("content")
                .as("게시물 내용")
                .containsExactly(getContents(NUMBER_OF_ARTICLES, PAGE_2, SIZE_2));

        assertThat(result3.getContent())
                .extracting("article")
                .extracting("content")
                .as("게시물 내용")
                .containsExactly(getContents(NUMBER_OF_ARTICLES, PAGE_3, SIZE_3));

        assertThat(result1.getContent())
                .extracting("article")
                .extracting("author")
                .as("작성자 페치 조인 성공")
                .allMatch(isNotProxy());

        assertThat(result2.getContent())
                .extracting("article")
                .extracting("author")
                .as("작성자 페치 조인 성공")
                .allMatch(isNotProxy());

        assertThat(result3.getContent())
                .extracting("article")
                .extracting("author")
                .as("작성자 페치 조인 성공")
                .allMatch(isNotProxy());

        assertThat(result1.getContent())
                .extracting("numComments")
                .as("댓글 수")
                .containsExactly(getNumsOfComments(NUMBER_OF_ARTICLES, PAGE_1, SIZE_1));

        assertThat(result2.getContent())
                .extracting("numComments")
                .as("댓글 수")
                .containsExactly(getNumsOfComments(NUMBER_OF_ARTICLES, PAGE_2, SIZE_2));

        assertThat(result3.getContent())
                .extracting("numComments")
                .as("댓글 수")
                .containsExactly(getNumsOfComments(NUMBER_OF_ARTICLES, PAGE_3, SIZE_3));
    }

    private static String[] getTitles(int total, int page, int size) {
        int startNum = total - page * size - 1;

        String[] values = new String[size];
        for (int i = 0; i < size; i++) {
            values[i] = "title " + startNum--;
        }

        return values;
    }

    private static String[] getContents(int total, int page, int size) {
        int startNum = total - page * size - 1;

        String[] values = new String[size];
        for (int i = 0; i < size; i++) {
            values[i] = "content " + startNum--;
        }

        return values;
    }

    private static Long[] getNumsOfComments(int total, int page, int size) {
        long startNum = total - (long) page * size - 1;

        Long[] values = new Long[size];
        for (int i = 0; i < size; i++) {
            values[i] = startNum--;
        }

        return values;
    }

    @Test
    void search_fail() {
        //given
        User user1 = createUserAndPersist("user1", "test1@gmail.com", "12341");
        User user2 = createUserAndPersist("user2", "test2@gmail.com", "12342");

        final int NUMBER_OF_ARTICLES = 123;

        for (int i = 0; i < NUMBER_OF_ARTICLES; i++) {
            Article article = Article.create("title " + i, "content " + i, (i % 2 == 1) ? user1 : user2);
            em.persist(article);

            for (int j = 0; j < i; j++) {
                Comment comment = Comment.create("comment " + i, article, (j % 2 == 1) ? user1 : user2);
                em.persist(comment);
            }
        }

        final int PAGE_1 = 2, SIZE_1 = 0;
        final int PAGE_2 = -1, SIZE_2 = 5;

        em.flush();
        em.clear();

        //when
        assertThatThrownBy(() -> articleQueryService.search(PAGE_1, SIZE_1))
                .as("페이지 크기가 1보다 작음")
                .isInstanceOf(WrongPageRequestException.class);

        assertThatThrownBy(() -> articleQueryService.search(PAGE_2, SIZE_2))
                .as("페이지 번호가 0보다 작음")
                .isInstanceOf(WrongPageRequestException.class);
    }
}