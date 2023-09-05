package hello.board.service.query;

import hello.board.domain.Article;
import hello.board.domain.Comment;
import hello.board.domain.User;
import hello.board.dto.service.search.ArticleSearchCond;
import hello.board.dto.service.search.ArticleSearchDto;
import hello.board.dto.service.search.ArticleSearchType;
import hello.board.exception.FailToFindEntityException;
import hello.board.exception.WrongPageRequestException;
import hello.board.repository.ArticleRepository;
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

import java.util.List;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
@DataJpaTest
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
        assertThatThrownBy(() -> articleQueryService.findById(WRONG_ID))
                .as("존재하지 않는 게시글")
                .isInstanceOf(FailToFindEntityException.class);
    }

    @Test
    @DisplayName("댓글과 함께 조회 성공")
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
    @DisplayName("댓글과 함께 조회 실패")
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
    @DisplayName("검색 조건 없이 검색 성공")
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

        assertThat(result2.getTotalElements())
                .as("총 게시물 수")
                .isEqualTo(NUMBER_OF_ARTICLES);

        assertThat(result3.getTotalElements())
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
    @DisplayName("검색 조건 없이 검색 실패")
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

    @Test
    @DisplayName("검색 조건 존재하는 검색 성공")
    void searchWithCondition() {
        //given
        User user1 = createUserAndPersist("user1", "test1@gmail.com", "12341");
        User user2 = createUserAndPersist("user2", "test2@gmail.com", "12342");

        final int NUMBER_OF_ARTICLES = 100;

        for (int i = 0; i < NUMBER_OF_ARTICLES; i++) {
            Article article = Article.create("title " + i, "content " + i, (i % 2 == 1) ? user1 : user2);
            em.persist(article);

            for (int j = 0; j < i; j++) {
                Comment comment = Comment.create("comment " + i, article, (j % 2 == 1) ? user1 : user2);
                em.persist(comment);
            }
        }

        final int PAGE_1 = 0, SIZE_1 = 10;
        final int PAGE_2 = 1, SIZE_2 = 7;

        final ArticleSearchCond titleAndContent = ArticleSearchCond.create("3", ArticleSearchType.TITLE_AND_CONTENT);
        final ArticleSearchCond title = ArticleSearchCond.create("e 7", ArticleSearchType.TITLE);
        final ArticleSearchCond content = ArticleSearchCond.create("5", ArticleSearchType.CONTENT);
        final ArticleSearchCond author = ArticleSearchCond.create("er1", ArticleSearchType.AUTHOR);

        em.flush();
        em.clear();

        //when
        Page<ArticleSearchDto> titleAndContentResult1 = articleQueryService.search(titleAndContent, PAGE_1, SIZE_1);
        Page<ArticleSearchDto> titleAndContentResult2 = articleQueryService.search(titleAndContent, PAGE_2, SIZE_2);

        Page<ArticleSearchDto> titleResult1 = articleQueryService.search(title, PAGE_1, SIZE_1);
        Page<ArticleSearchDto> titleResult2 = articleQueryService.search(title, PAGE_2, SIZE_2);

        Page<ArticleSearchDto> contentResult1 = articleQueryService.search(content, PAGE_1, SIZE_1);
        Page<ArticleSearchDto> contentResult2 = articleQueryService.search(content, PAGE_2, SIZE_2);

        Page<ArticleSearchDto> authorResult1 = articleQueryService.search(author, PAGE_1, SIZE_1);
        Page<ArticleSearchDto> authorResult2 = articleQueryService.search(author, PAGE_2, SIZE_2);

        //then
        // totalElements
        assertThat(titleAndContentResult1.getTotalElements())
                .as("검색된 게시물 수")
                .isEqualTo(19L);

        assertThat(titleAndContentResult2.getTotalElements())
                .as("검색된 게시물 수")
                .isEqualTo(19L);

        assertThat(titleResult1.getTotalElements())
                .as("검색된 게시물 수")
                .isEqualTo(11L);

        assertThat(titleResult2.getTotalElements())
                .as("검색된 게시물 수")
                .isEqualTo(11L);

        assertThat(contentResult1.getTotalElements())
                .as("검색된 게시물 수")
                .isEqualTo(19L);

        assertThat(contentResult2.getTotalElements())
                .as("검색된 게시물 수")
                .isEqualTo(19L);

        assertThat(authorResult1.getTotalElements())
                .as("검색된 게시물 수")
                .isEqualTo(50L);

        assertThat(authorResult2.getTotalElements())
                .as("검색된 게시물 수")
                .isEqualTo(50L);

        // title of Article
        assertThat(titleAndContentResult1.getContent())
                .extracting("article")
                .extracting("title")
                .as("게시물 제목")
                .containsExactly("title 93", "title 83", "title 73", "title 63", "title 53", "title 43", "title 39", "title 38", "title 37", "title 36");

        assertThat(titleAndContentResult2.getContent())
                .extracting("article")
                .extracting("title")
                .as("게시물 제목")
                .containsExactly("title 38", "title 37", "title 36", "title 35", "title 34", "title 33", "title 32");

        assertThat(titleResult1.getContent())
                .extracting("article")
                .extracting("title")
                .as("게시물 제목")
                .containsExactly("title 79", "title 78", "title 77", "title 76", "title 75", "title 74", "title 73", "title 72", "title 71", "title 70");

        assertThat(titleResult2.getContent())
                .extracting("article")
                .extracting("title")
                .as("게시물 제목")
                .containsExactly("title 72", "title 71", "title 70", "title 7");

        assertThat(contentResult1.getContent())
                .extracting("article")
                .extracting("title")
                .as("게시물 제목")
                .containsExactly("title 95", "title 85", "title 75", "title 65", "title 59", "title 58", "title 57", "title 56", "title 55", "title 54");

        assertThat(contentResult2.getContent())
                .extracting("article")
                .extracting("title")
                .as("게시물 제목")
                .containsExactly("title 56", "title 55", "title 54", "title 53", "title 52", "title 51", "title 50");

        assertThat(authorResult1.getContent())
                .extracting("article")
                .extracting("title")
                .as("게시물 제목")
                .containsExactly("title 99", "title 97", "title 95", "title 93", "title 91", "title 89", "title 87", "title 85", "title 83", "title 81");

        assertThat(authorResult2.getContent())
                .extracting("article")
                .extracting("title")
                .as("게시물 제목")
                .containsExactly("title 85", "title 83", "title 81", "title 79", "title 77", "title 75", "title 73");

        // fetch join with Author
        assertThat(titleAndContentResult1.getContent())
                .extracting("article")
                .extracting("author")
                .as("작성자 페치 조인 성공")
                .allMatch(isNotProxy());

        assertThat(titleAndContentResult2.getContent())
                .extracting("article")
                .extracting("author")
                .as("작성자 페치 조인 성공")
                .allMatch(isNotProxy());

        assertThat(titleResult1.getContent())
                .extracting("article")
                .extracting("author")
                .as("작성자 페치 조인 성공")
                .allMatch(isNotProxy());

        assertThat(titleResult2.getContent())
                .extracting("article")
                .extracting("author")
                .as("작성자 페치 조인 성공")
                .allMatch(isNotProxy());

        assertThat(contentResult1.getContent())
                .extracting("article")
                .extracting("author")
                .as("작성자 페치 조인 성공")
                .allMatch(isNotProxy());

        assertThat(contentResult2.getContent())
                .extracting("article")
                .extracting("author")
                .as("작성자 페치 조인 성공")
                .allMatch(isNotProxy());

        assertThat(authorResult1.getContent())
                .extracting("article")
                .extracting("author")
                .as("작성자 페치 조인 성공")
                .allMatch(isNotProxy());

        assertThat(authorResult2.getContent())
                .extracting("article")
                .extracting("author")
                .as("작성자 페치 조인 성공")
                .allMatch(isNotProxy());

        // numbers of comments
        assertThat(titleAndContentResult1.getContent())
                .extracting("numComments")
                .as("댓글 수")
                .containsExactly(93L, 83L, 73L, 63L, 53L, 43L, 39L, 38L, 37L, 36L);

        assertThat(titleAndContentResult2.getContent())
                .extracting("numComments")
                .as("댓글 수")
                .containsExactly(38L, 37L, 36L, 35L, 34L, 33L, 32L);

        assertThat(titleResult1.getContent())
                .extracting("numComments")
                .as("댓글 수")
                .containsExactly(79L, 78L, 77L, 76L, 75L, 74L, 73L, 72L, 71L, 70L);

        assertThat(titleResult2.getContent())
                .extracting("numComments")
                .as("댓글 수")
                .containsExactly(72L, 71L, 70L, 7L);

        assertThat(contentResult1.getContent())
                .extracting("numComments")
                .as("댓글 수")
                .containsExactly(95L, 85L, 75L, 65L, 59L, 58L, 57L, 56L, 55L, 54L);

        assertThat(contentResult2.getContent())
                .extracting("numComments")
                .as("댓글 수")
                .containsExactly(56L, 55L, 54L, 53L, 52L, 51L, 50L);

        assertThat(authorResult1.getContent())
                .extracting("numComments")
                .as("댓글 수")
                .containsExactly(99L, 97L, 95L, 93L, 91L, 89L, 87L, 85L, 83L, 81L);

        assertThat(authorResult2.getContent())
                .extracting("numComments")
                .as("댓글 수")
                .containsExactly(85L, 83L, 81L, 79L, 77L, 75L, 73L);

    }

    @Test
    @DisplayName("빈 검색 조건 존재하는 검색 성공")
    void searchWithEmptyCondition() {
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

        final ArticleSearchCond emptyKeyword = ArticleSearchCond.create(" ", ArticleSearchType.TITLE_AND_CONTENT);

        em.flush();
        em.clear();

        //when
        Page<ArticleSearchDto> emptyKeywordResult1 = articleQueryService.search(emptyKeyword, PAGE_1, SIZE_1);
        Page<ArticleSearchDto> nullConditionResult1 = articleQueryService.search(null, PAGE_1, SIZE_1);

        Page<ArticleSearchDto> emptyKeywordResult2 = articleQueryService.search(emptyKeyword, PAGE_2, SIZE_2);
        Page<ArticleSearchDto> nullConditionResult2 = articleQueryService.search(null, PAGE_2, SIZE_2);

        Page<ArticleSearchDto> emptyKeywordResult3 = articleQueryService.search(emptyKeyword, PAGE_3, SIZE_3);
        Page<ArticleSearchDto> nullConditionResult3 = articleQueryService.search(null, PAGE_3, SIZE_3);

        //then
        //total elements
        assertThat(emptyKeywordResult1.getTotalElements())
                .as("총 게시물 수")
                .isEqualTo(NUMBER_OF_ARTICLES);

        assertThat(emptyKeywordResult2.getTotalElements())
                .as("총 게시물 수")
                .isEqualTo(NUMBER_OF_ARTICLES);

        assertThat(emptyKeywordResult3.getTotalElements())
                .as("총 게시물 수")
                .isEqualTo(NUMBER_OF_ARTICLES);

        assertThat(nullConditionResult1.getTotalElements())
                .as("총 게시물 수")
                .isEqualTo(NUMBER_OF_ARTICLES);

        assertThat(nullConditionResult2.getTotalElements())
                .as("총 게시물 수")
                .isEqualTo(NUMBER_OF_ARTICLES);

        assertThat(nullConditionResult3.getTotalElements())
                .as("총 게시물 수")
                .isEqualTo(NUMBER_OF_ARTICLES);

        //title of Article
        assertThat(emptyKeywordResult1.getContent())
                .extracting("article")
                .extracting("title")
                .as("게시물 제목")
                .containsExactly(getTitles(NUMBER_OF_ARTICLES, PAGE_1, SIZE_1));

        assertThat(emptyKeywordResult2.getContent())
                .extracting("article")
                .extracting("title")
                .as("게시물 제목")
                .containsExactly(getTitles(NUMBER_OF_ARTICLES, PAGE_2, SIZE_2));

        assertThat(emptyKeywordResult3.getContent())
                .extracting("article")
                .extracting("title")
                .as("게시물 제목")
                .containsExactly(getTitles(NUMBER_OF_ARTICLES, PAGE_3, SIZE_3));

        assertThat(nullConditionResult1.getContent())
                .extracting("article")
                .extracting("title")
                .as("게시물 제목")
                .containsExactly(getTitles(NUMBER_OF_ARTICLES, PAGE_1, SIZE_1));

        assertThat(nullConditionResult2.getContent())
                .extracting("article")
                .extracting("title")
                .as("게시물 제목")
                .containsExactly(getTitles(NUMBER_OF_ARTICLES, PAGE_2, SIZE_2));

        assertThat(nullConditionResult3.getContent())
                .extracting("article")
                .extracting("title")
                .as("게시물 제목")
                .containsExactly(getTitles(NUMBER_OF_ARTICLES, PAGE_3, SIZE_3));

        //fetch join with Author
        assertThat(emptyKeywordResult1.getContent())
                .extracting("article")
                .extracting("author")
                .as("작성자 페치 조인 성공")
                .allMatch(isNotProxy());

        assertThat(emptyKeywordResult2.getContent())
                .extracting("article")
                .extracting("author")
                .as("작성자 페치 조인 성공")
                .allMatch(isNotProxy());

        assertThat(emptyKeywordResult3.getContent())
                .extracting("article")
                .extracting("author")
                .as("작성자 페치 조인 성공")
                .allMatch(isNotProxy());

        assertThat(nullConditionResult1.getContent())
                .extracting("article")
                .extracting("author")
                .as("작성자 페치 조인 성공")
                .allMatch(isNotProxy());

        assertThat(nullConditionResult2.getContent())
                .extracting("article")
                .extracting("author")
                .as("작성자 페치 조인 성공")
                .allMatch(isNotProxy());

        assertThat(nullConditionResult3.getContent())
                .extracting("article")
                .extracting("author")
                .as("작성자 페치 조인 성공")
                .allMatch(isNotProxy());

        //numbers of comments
        assertThat(emptyKeywordResult1.getContent())
                .extracting("numComments")
                .as("댓글 수")
                .containsExactly(getNumsOfComments(NUMBER_OF_ARTICLES, PAGE_1, SIZE_1));

        assertThat(emptyKeywordResult2.getContent())
                .extracting("numComments")
                .as("댓글 수")
                .containsExactly(getNumsOfComments(NUMBER_OF_ARTICLES, PAGE_2, SIZE_2));

        assertThat(emptyKeywordResult3.getContent())
                .extracting("numComments")
                .as("댓글 수")
                .containsExactly(getNumsOfComments(NUMBER_OF_ARTICLES, PAGE_3, SIZE_3));

        assertThat(nullConditionResult1.getContent())
                .extracting("numComments")
                .as("댓글 수")
                .containsExactly(getNumsOfComments(NUMBER_OF_ARTICLES, PAGE_1, SIZE_1));

        assertThat(nullConditionResult2.getContent())
                .extracting("numComments")
                .as("댓글 수")
                .containsExactly(getNumsOfComments(NUMBER_OF_ARTICLES, PAGE_2, SIZE_2));

        assertThat(nullConditionResult3.getContent())
                .extracting("numComments")
                .as("댓글 수")
                .containsExactly(getNumsOfComments(NUMBER_OF_ARTICLES, PAGE_3, SIZE_3));

    }

    @Test
    @DisplayName("검색 조건 존재하는 검색 실패")
    void searchWithCondition_fail() {
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

        final ArticleSearchCond cond = ArticleSearchCond.create("ser2", ArticleSearchType.AUTHOR);

        em.flush();
        em.clear();

        //when
        assertThatThrownBy(() -> articleQueryService.search(cond, PAGE_1, SIZE_1))
                .as("페이지 크기가 1보다 작음")
                .isInstanceOf(WrongPageRequestException.class);

        assertThatThrownBy(() -> articleQueryService.search(cond, PAGE_2, SIZE_2))
                .as("페이지 번호가 0보다 작음")
                .isInstanceOf(WrongPageRequestException.class);
    }
}