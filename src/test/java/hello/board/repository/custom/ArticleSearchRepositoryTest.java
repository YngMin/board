package hello.board.repository.custom;

import hello.board.domain.Article;
import hello.board.domain.Comment;
import hello.board.domain.User;
import hello.board.dto.service.search.ArticleSearchCond;
import hello.board.dto.service.search.ArticleSearchDto;
import hello.board.repository.ArticleRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceUnitUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static hello.board.dto.service.search.ArticleSearchType.*;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ArticleSearchRepositoryTest {

    @Autowired
    ArticleRepository articleRepository;

    @Autowired
    EntityManager em;

    PersistenceUnitUtil persistence;

    @BeforeEach
    void beforeEach() {
        persistence = em.getEntityManagerFactory().getPersistenceUnitUtil();
    }

    private User createAndSaveUser(String name, String email, String password) {
        User user = User.create(name, email, password);
        em.persist(user);
        return user;
    }

    private static List<Article> generateArticles(int numOfArticles, User... authors) {
        List<Article> articles = new ArrayList<>();
        for (int i = 0; i < numOfArticles; i++) {
            Article article = Article.create(
                    "title" + i,
                    "content" + i,
                    authors[i % authors.length]
            );
            articles.add(article);
        }
        return articles;
    }

    private static List<Comment> generateComments(List<Article> articles, User author, int... numOfCommentsSeq) {
        List<Comment> comments = new ArrayList<>();
        int cnt = 0, idx = 0;
        for (Article article : articles) {
            for (int i = 0; i < numOfCommentsSeq[idx]; i++) {
                Comment comment = Comment.create(
                        "content" + cnt++,
                        article,
                        author
                );
                comments.add(comment);
            }
            idx = (idx+1) % numOfCommentsSeq.length;
        }
        return comments;
    }

    @Test
    @DisplayName("검색 조건 없음 - 첫 페이지")
    void search_firstPage() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");
        User author3 = createAndSaveUser("author3", "author3@board.com", "");

        List<Article> articles = generateArticles(20, author1, author2, author3);
        articles.forEach(em::persist);

        List<Comment> comments = generateComments(articles, author1, 0, 3, 11, 2);
        comments.forEach(em::persist);

        final PageRequest pageable = PageRequest.of(0, 5);

        em.flush();
        em.clear();

        //when
        Page<ArticleSearchDto> articleSearchDtos = articleRepository.search(pageable);

        //then
        assertThat(articleSearchDtos.getTotalPages())
                .as("전체 페이지 수")
                .isEqualTo(4);

        assertThat(articleSearchDtos.getTotalElements())
                .as("전체 게시글 수")
                .isEqualTo(20L);

        List<ArticleSearchDto> content = articleSearchDtos.getContent();

        List<Article> inPage = articles.subList(15, 20);
        Collections.reverse(inPage);

        assertThat(content)
                .extracting("article")
                .as("페이지 내 게시글")
                .containsExactlyElementsOf(inPage);

        assertThat(content)
                .extracting("article")
                .extracting("author")
                .as("게시글 작성자 페치 조인")
                .allMatch(persistence::isLoaded);

        assertThat(content)
                .extracting("article")
                .extracting("author")
                .as("게시글 작성자")
                .containsExactly(author2, author1, author3, author2, author1);

        assertThat(content)
                .extracting("article")
                .extracting("comments")
                .as("댓글 로딩 지연")
                .noneMatch(persistence::isLoaded);

        assertThat(content)
                .extracting("numComments")
                .as("댓글 수")
                .containsExactly(2L, 11L, 3L, 0L, 2L);
    }

    @Test
    @DisplayName("검색 조건 없음 - 중간 페이지")
    void search_middlePage() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");
        User author3 = createAndSaveUser("author3", "author3@board.com", "");

        List<Article> articles = generateArticles(20, author1, author2, author3);
        articles.forEach(em::persist);

        List<Comment> comments = generateComments(articles, author1, 7, 0, 11, 3, 8, 2);
        comments.forEach(em::persist);

        PageRequest pageable = PageRequest.of(1, 7);

        em.flush();
        em.clear();

        //when
        final Page<ArticleSearchDto> articleSearchDtos = articleRepository.search(pageable);

        //then
        assertThat(articleSearchDtos.getTotalPages())
                .as("전체 페이지 수")
                .isEqualTo(3);

        assertThat(articleSearchDtos.getTotalElements())
                .as("전체 게시글 수")
                .isEqualTo(20L);

        List<ArticleSearchDto> content = articleSearchDtos.getContent();

        List<Article> inPage = articles.subList(6, 13);
        Collections.reverse(inPage);

        assertThat(content)
                .extracting("article")
                .as("페이지 내 게시글")
                .containsExactlyElementsOf(inPage);

        assertThat(content)
                .extracting("article")
                .extracting("author")
                .as("게시글 작성자 페치 조인")
                .allMatch(persistence::isLoaded);

        assertThat(content)
                .extracting("article")
                .extracting("author")
                .as("게시글 작성자")
                .containsExactly(author1, author3, author2, author1, author3, author2, author1);

        assertThat(content)
                .extracting("article")
                .extracting("comments")
                .as("댓글 로딩 지연")
                .noneMatch(persistence::isLoaded);

        assertThat(content)
                .extracting("numComments")
                .as("댓글 수")
                .containsExactly(7L, 2L, 8L, 3L, 11L, 0L, 7L);
    }

    @Test
    @DisplayName("검색 조건 없음 - 마지막 페이지")
    void search_lastPage() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");
        User author3 = createAndSaveUser("author3", "author3@board.com", "");

        List<Article> articles = generateArticles(20, author1, author2, author3);
        articles.forEach(em::persist);

        List<Comment> comments = generateComments(articles, author1, 3, 8, 5, 2, 0, 2, 6, 3, 8);
        comments.forEach(em::persist);

        PageRequest pageable = PageRequest.of(3, 6);

        em.flush();
        em.clear();

        //when
        final Page<ArticleSearchDto> articleSearchDtos = articleRepository.search(pageable);

        //then
        assertThat(articleSearchDtos.getTotalPages())
                .as("전체 페이지 수")
                .isEqualTo(4);

        assertThat(articleSearchDtos.getTotalElements())
                .as("전체 게시글 수")
                .isEqualTo(20L);

        List<ArticleSearchDto> content = articleSearchDtos.getContent();

        List<Article> inPage = articles.subList(0, 2);
        Collections.reverse(inPage);

        assertThat(content)
                .extracting("article")
                .as("페이지 내 게시글")
                .containsExactlyElementsOf(inPage);

        assertThat(content)
                .extracting("article")
                .extracting("author")
                .as("게시글 작성자 페치 조인")
                .allMatch(persistence::isLoaded);

        assertThat(content)
                .extracting("article")
                .extracting("author")
                .as("게시글 작성자")
                .containsExactly(author2, author1);

        assertThat(content)
                .extracting("article")
                .extracting("comments")
                .as("댓글 로딩 지연")
                .noneMatch(persistence::isLoaded);

        assertThat(content)
                .extracting("numComments")
                .as("댓글 수")
                .containsExactly(8L, 3L);
    }

    @Test
    @DisplayName("검색 조건 없음 - 결과 없음")
    void search_resultEmpty() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");
        User author3 = createAndSaveUser("author3", "author3@board.com", "");

        List<Article> articles = generateArticles(20, author1, author2, author3);
        articles.forEach(em::persist);

        List<Comment> comments = generateComments(articles, author1, 1, 2, 3, 4);
        comments.forEach(em::persist);

        PageRequest pageable = PageRequest.of(666, 6);

        em.flush();
        em.clear();

        //when
        final Page<ArticleSearchDto> articleSearchDtos = articleRepository.search(pageable);

        //then
        assertThat(articleSearchDtos.getTotalPages())
                .as("전체 페이지 수")
                .isEqualTo(4);

        assertThat(articleSearchDtos.getTotalElements())
                .as("전체 게시글 수")
                .isEqualTo(20L);

        assertThat(articleSearchDtos.isEmpty())
                .as("검색 결과 없음")
                .isTrue();
    }

    @Test
    @DisplayName("검색 조건 없음 - 결과가 한 페이지")
    void search_onePage() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");
        User author3 = createAndSaveUser("author3", "author3@board.com", "");

        List<Article> articles = generateArticles(5, author1, author2, author3);
        articles.forEach(em::persist);

        List<Comment> comments = generateComments(articles, author1, 7, 0, 3);
        comments.forEach(em::persist);

        final PageRequest pageable = PageRequest.of(0, 20);

        em.flush();
        em.clear();

        //when
        Page<ArticleSearchDto> articleSearchDtos = articleRepository.search(pageable);

        //then
        assertThat(articleSearchDtos.getTotalPages())
                .as("전체 페이지 수")
                .isEqualTo(1);

        assertThat(articleSearchDtos.getTotalElements())
                .as("전체 게시글 수")
                .isEqualTo(5L);

        List<ArticleSearchDto> content = articleSearchDtos.getContent();

        List<Article> inPage = new ArrayList<>(articles);
        Collections.reverse(inPage);

        assertThat(content)
                .extracting("article")
                .as("페이지 내 게시글")
                .containsExactlyElementsOf(inPage);

        assertThat(content)
                .extracting("article")
                .extracting("author")
                .as("게시글 작성자 페치 조인")
                .allMatch(persistence::isLoaded);

        assertThat(content)
                .extracting("article")
                .extracting("author")
                .as("게시글 작성자")
                .containsExactly(author2, author1, author3, author2, author1);

        assertThat(content)
                .extracting("article")
                .extracting("comments")
                .as("댓글 로딩 지연")
                .noneMatch(persistence::isLoaded);

        assertThat(content)
                .extracting("numComments")
                .as("댓글 수")
                .containsExactly(0L, 7L, 3L, 0L, 7L);
    }

    @Test
    @DisplayName("검색 조건 없음 - 게시글 없음")
    void search_noArticle() {
        //given
        final PageRequest pageable = PageRequest.of(0, 20);

        //when
        Page<ArticleSearchDto> articleSearchDtos = articleRepository.search(pageable);

        //then
        assertThat(articleSearchDtos.getTotalPages())
                .as("전체 페이지 수")
                .isEqualTo(0);

        assertThat(articleSearchDtos.getTotalElements())
                .as("전체 게시글 수")
                .isEqualTo(0L);

        assertThat(articleSearchDtos.isEmpty())
                .as("페이지 내 게시글 없음")
                .isTrue();
    }

    @Test
    @DisplayName("검색 조건: 제목 및 내용 - 빈 조건 1")
    void search_cond_titleAndContent_empty_1() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");
        User author3 = createAndSaveUser("author3", "author3@board.com", "");

        Article article1 = Article.create("    aaaa aa  aa aa   ", "bbBbbBBbbb", author1);
        Article article2 = Article.create(" CccCccCccc   ", "aAaAaAaaAa", author2);
        Article article3 = Article.create("bbb bbb bbbb  ", "ccc ccc cccc ", author3);
        Article article4 = Article.create("ababab abab", "cacaca caca", author1);
        Article article5 = Article.create("  bcbcBCbcbc", "ababa babab", author2);
        Article article6 = Article.create("abc abcaBca", "bc abcabcab", author3);
        Article article7 = Article.create("aa bcca bbca", "abcc abbcaa", author1);
        Article article8 = Article.create(" cbacbacbac", "baba bAbaba", author2);
        Article article9 = Article.create("  a aaab cabbc   ", "  baacaabaca  ", author3);
        Article article10 = Article.create("aAabbbccca", "aaAabbBbcc", author1);


        List<Article> articles = List.of(
                article1, article2, article3, article4, article5,
                article6, article7, article8, article9, article10
        );

        articles.forEach(em::persist);

        List<Comment> comments = generateComments(articles, author1, 0, 1, 2, 3, 4);
        comments.forEach(em::persist);

        PageRequest pageable = PageRequest.of(0, 3);

        final ArticleSearchCond cond = ArticleSearchCond.create("", TITLE_AND_CONTENT);

        em.flush();
        em.clear();

        //when
        final Page<ArticleSearchDto> articleSearchDtos = articleRepository.search(cond, pageable);

        //then
        assertThat(articleSearchDtos.getTotalPages())
                .as("전체 페이지 수")
                .isEqualTo(4);

        assertThat(articleSearchDtos.getTotalElements())
                .as("전체 게시글 수")
                .isEqualTo(10L);

        List<ArticleSearchDto> content = articleSearchDtos.getContent();

        assertThat(content)
                .extracting("article")
                .as("페이지 내 게시글")
                .containsExactly(article10, article9, article8);

        assertThat(content)
                .extracting("article")
                .extracting("author")
                .as("게시글 작성자 페치 조인")
                .allMatch(persistence::isLoaded);

        assertThat(content)
                .extracting("article")
                .extracting("author")
                .as("게시글 작성자")
                .containsExactly(author1, author3, author2);

        assertThat(content)
                .extracting("article")
                .extracting("comments")
                .as("댓글 로딩 지연")
                .noneMatch(persistence::isLoaded);

        assertThat(content)
                .extracting("numComments")
                .as("댓글 수")
                .containsExactly(4L, 3L, 2L);
    }

    @Test
    @DisplayName("검색 조건: 제목 및 내용 - 빈 조건 2")
    void search_cond_titleAndContent_empty_2() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");
        User author3 = createAndSaveUser("author3", "author3@board.com", "");

        Article article1 = Article.create("    aaaa aa  aa aa   ", "bbBbbBBbbb", author1);
        Article article2 = Article.create(" CccCccCccc   ", "aAaAaAaaAa", author2);
        Article article3 = Article.create("bbb bbb bbbb  ", "ccc ccc cccc ", author3);
        Article article4 = Article.create("ababab abab", "cacaca caca", author1);
        Article article5 = Article.create("  bcbcBCbcbc", "ababa babab", author2);
        Article article6 = Article.create("abc abcaBca", "bc abcabcab", author3);
        Article article7 = Article.create("aa bcca bbca", "abcc abbcaa", author1);
        Article article8 = Article.create(" cbacbacbac", "baba bAbaba", author2);
        Article article9 = Article.create("  a aaab cabbc   ", "  baacaabaca  ", author3);
        Article article10 = Article.create("aAabbbccca", "aaAabbBbcc", author1);


        List<Article> articles = List.of(
                article1, article2, article3, article4, article5,
                article6, article7, article8, article9, article10
        );

        articles.forEach(em::persist);

        List<Comment> comments = generateComments(articles, author1, 0, 1, 2, 3, 4);
        comments.forEach(em::persist);

        PageRequest pageable = PageRequest.of(0, 3);

        final ArticleSearchCond cond = ArticleSearchCond.create(" ", TITLE_AND_CONTENT);

        em.flush();
        em.clear();

        //when
        final Page<ArticleSearchDto> articleSearchDtos = articleRepository.search(cond, pageable);

        //then
        assertThat(articleSearchDtos.getTotalPages())
                .as("전체 페이지 수")
                .isEqualTo(4);

        assertThat(articleSearchDtos.getTotalElements())
                .as("전체 게시글 수")
                .isEqualTo(10L);

        List<ArticleSearchDto> content = articleSearchDtos.getContent();

        assertThat(content)
                .extracting("article")
                .as("페이지 내 게시글")
                .containsExactly(article10, article9, article8);

        assertThat(content)
                .extracting("article")
                .extracting("author")
                .as("게시글 작성자 페치 조인")
                .allMatch(persistence::isLoaded);

        assertThat(content)
                .extracting("article")
                .extracting("author")
                .as("게시글 작성자")
                .containsExactly(author1, author3, author2);

        assertThat(content)
                .extracting("article")
                .extracting("comments")
                .as("댓글 로딩 지연")
                .noneMatch(persistence::isLoaded);

        assertThat(content)
                .extracting("numComments")
                .as("댓글 수")
                .containsExactly(4L, 3L, 2L);
    }

    @Test
    @DisplayName("검색 조건: 제목 및 내용 1")
    void search_cond_titleAndContent_1() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");
        User author3 = createAndSaveUser("author3", "author3@board.com", "");

        Article article1 = Article.create("    aaaa aa  aa aa   ", "bbBbbBBbbb", author1);
        Article article2 = Article.create(" CccCccCccc   ", "aAaAaAaaAa", author2);
        Article article3 = Article.create("bbb bbb bbbb  ", "ccc ccc cccc ", author3);
        Article article4 = Article.create("ababab abab", "cacaca caca", author1);
        Article article5 = Article.create("  bcbcBCbcbc", "ababa babab", author2);
        Article article6 = Article.create("abc abcaBca", "bc abcabcab", author3);
        Article article7 = Article.create("aa bcca bbca", "abcc abbcaa", author1);
        Article article8 = Article.create(" cbacbacbac", "baba bAbaba", author2);
        Article article9 = Article.create("  a aaab cabbc   ", "  baacaabaca  ", author3);
        Article article10 = Article.create("aAabbbccca", "aaAabbBbcc", author1);

        List<Article> articles = List.of(
                article1, article2, article3, article4, article5,
                article6, article7, article8, article9, article10
        );

        articles.forEach(em::persist);

        List<Comment> comments = generateComments(articles, author1, 0, 1, 2, 3, 4);
        comments.forEach(em::persist);

        PageRequest pageable = PageRequest.of(0, 3);

        final ArticleSearchCond cond = ArticleSearchCond.create("aaa", TITLE_AND_CONTENT);

        em.flush();
        em.clear();

        //when
        final Page<ArticleSearchDto> articleSearchDtos = articleRepository.search(cond, pageable);

        //then
        assertThat(articleSearchDtos.getTotalPages())
                .as("전체 페이지 수")
                .isEqualTo(2);

        assertThat(articleSearchDtos.getTotalElements())
                .as("전체 게시글 수")
                .isEqualTo(4L);

        List<ArticleSearchDto> content = articleSearchDtos.getContent();

        assertThat(content)
                .extracting("article")
                .as("페이지 내 게시글")
                .containsExactly(article10, article9, article2);

        assertThat(content)
                .extracting("article")
                .extracting("author")
                .as("게시글 작성자 페치 조인")
                .allMatch(persistence::isLoaded);

        assertThat(content)
                .extracting("article")
                .extracting("author")
                .as("게시글 작성자")
                .containsExactly(author1, author3, author2);

        assertThat(content)
                .extracting("article")
                .extracting("comments")
                .as("댓글 로딩 지연")
                .noneMatch(persistence::isLoaded);

        assertThat(content)
                .extracting("numComments")
                .as("댓글 수")
                .containsExactly(4L, 3L, 1L);
    }

    @Test
    @DisplayName("검색 조건: 제목 및 내용 2")
    void search_cond_titleAndContent_2() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");
        User author3 = createAndSaveUser("author3", "author3@board.com", "");

        Article article1 = Article.create("    aaaa aa  aa aa   ", "bbBbbBBbbb", author1);
        Article article2 = Article.create(" CccCccCccc   ", "aAaAaAaaAa", author2);
        Article article3 = Article.create("bbb bbb bbbb  ", "ccc ccc cccc ", author3);
        Article article4 = Article.create("ababab abab", "cacaca caca", author1);
        Article article5 = Article.create("  bcbcBCbcbc", "ababa babab", author2);
        Article article6 = Article.create("abc abcaBca", "bc abcabcab", author3);
        Article article7 = Article.create("aa bcca bbca", "abcc abbcaa", author1);
        Article article8 = Article.create(" cbacbacbac", "baba bAbaba", author2);
        Article article9 = Article.create("  a aaab cabbc   ", "  baacaabaca  ", author3);
        Article article10 = Article.create("aAabbbccca", "aaAabbBbcc", author1);

        List<Article> articles = List.of(
                article1, article2, article3, article4, article5,
                article6, article7, article8, article9, article10
        );

        articles.forEach(em::persist);

        List<Comment> comments = generateComments(articles, author1, 0, 1, 2, 3, 4);
        comments.forEach(em::persist);

        PageRequest pageable = PageRequest.of(0, 3);

        final ArticleSearchCond cond = ArticleSearchCond.create("ABc", TITLE_AND_CONTENT);

        em.flush();
        em.clear();

        //when
        final Page<ArticleSearchDto> articleSearchDtos = articleRepository.search(cond, pageable);

        //then
        assertThat(articleSearchDtos.getTotalPages())
                .as("전체 페이지 수")
                .isEqualTo(1);

        assertThat(articleSearchDtos.getTotalElements())
                .as("전체 게시글 수")
                .isEqualTo(2L);

        List<ArticleSearchDto> content = articleSearchDtos.getContent();

        assertThat(content)
                .extracting("article")
                .as("페이지 내 게시글")
                .containsExactly(article7, article6);

        assertThat(content)
                .extracting("article")
                .extracting("author")
                .as("게시글 작성자 페치 조인")
                .allMatch(persistence::isLoaded);

        assertThat(content)
                .extracting("article")
                .extracting("author")
                .as("게시글 작성자")
                .containsExactly(author1, author3);

        assertThat(content)
                .extracting("article")
                .extracting("comments")
                .as("댓글 로딩 지연")
                .noneMatch(persistence::isLoaded);

        assertThat(content)
                .extracting("numComments")
                .as("댓글 수")
                .containsExactly(1L, 0L);
    }

    @Test
    @DisplayName("검색 조건: 제목 및 내용 3")
    void search_cond_titleAndContent_3() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");
        User author3 = createAndSaveUser("author3", "author3@board.com", "");

        Article article1 = Article.create("    aaaa aa  aa aa   ", "bbBbbBBbbb", author1);
        Article article2 = Article.create(" CccCccCccc   ", "aAaAaAaaAa", author2);
        Article article3 = Article.create("bbb bbb bbbb  ", "ccc ccc cccc ", author3);
        Article article4 = Article.create("ababab abab", "cacaca caca", author1);
        Article article5 = Article.create("  bcbcBCbcbc", "ababa babab", author2);
        Article article6 = Article.create("abc abcaBca", "bc abcabcab", author3);
        Article article7 = Article.create("aa bcca bbca", "abcc abbcaa", author1);
        Article article8 = Article.create(" cbacbacbac", "baba bAbaba", author2);
        Article article9 = Article.create("  a aaab cabbc   ", "  baacaabaca  ", author3);
        Article article10 = Article.create("aAabbbccca", "aaAabbBbcc", author1);

        List<Article> articles = List.of(
                article1, article2, article3, article4, article5,
                article6, article7, article8, article9, article10
        );

        articles.forEach(em::persist);

        List<Comment> comments = generateComments(articles, author1, 0, 1, 2, 3, 4);
        comments.forEach(em::persist);

        PageRequest pageable = PageRequest.of(0, 3);

        final ArticleSearchCond cond = ArticleSearchCond.create(" AA  ", TITLE_AND_CONTENT);

        em.flush();
        em.clear();

        //when
        final Page<ArticleSearchDto> articleSearchDtos = articleRepository.search(cond, pageable);

        //then
        assertThat(articleSearchDtos.getTotalPages())
                .as("전체 페이지 수")
                .isEqualTo(2);

        assertThat(articleSearchDtos.getTotalElements())
                .as("전체 게시글 수")
                .isEqualTo(5L);

        List<ArticleSearchDto> content = articleSearchDtos.getContent();

        assertThat(content)
                .extracting("article")
                .as("페이지 내 게시글")
                .containsExactly(article10, article9, article7);

        assertThat(content)
                .extracting("article")
                .extracting("author")
                .as("게시글 작성자 페치 조인")
                .allMatch(persistence::isLoaded);

        assertThat(content)
                .extracting("article")
                .extracting("author")
                .as("게시글 작성자")
                .containsExactly(author1, author3, author1);

        assertThat(content)
                .extracting("article")
                .extracting("comments")
                .as("댓글 로딩 지연")
                .noneMatch(persistence::isLoaded);

        assertThat(content)
                .extracting("numComments")
                .as("댓글 수")
                .containsExactly(4L, 3L, 1L);
    }

    @Test
    @DisplayName("검색 조건: 제목 및 내용 4")
    void search_cond_titleAndContent_4() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");
        User author3 = createAndSaveUser("author3", "author3@board.com", "");

        Article article1 = Article.create("    aaaa aa  aa aa   ", "bbBbbBBbbb", author1);
        Article article2 = Article.create(" CccCccCccc   ", "aAaAaAaaAa", author2);
        Article article3 = Article.create("bbb bbb bbbb  ", "ccc ccc cccc ", author3);
        Article article4 = Article.create("ababab abab", "cacaca caca", author1);
        Article article5 = Article.create("  bcbcBCbcbc", "ababa babab", author2);
        Article article6 = Article.create("abc abcaBca", "bc abcabcab", author3);
        Article article7 = Article.create("aa bcca bbca", "abcc abbcaa", author1);
        Article article8 = Article.create(" cbacbacbac", "baba bAbaba", author2);
        Article article9 = Article.create("  a aaab cabbc   ", "  baacaabaca  ", author3);
        Article article10 = Article.create("aAabbbccca", "aaAabbBbcc", author1);

        List<Article> articles = List.of(
                article1, article2, article3, article4, article5,
                article6, article7, article8, article9, article10
        );

        articles.forEach(em::persist);

        List<Comment> comments = generateComments(articles, author1, 0, 1, 2, 3, 4);
        comments.forEach(em::persist);

        PageRequest pageable = PageRequest.of(0, 3);

        final ArticleSearchCond cond = ArticleSearchCond.create("a b", TITLE_AND_CONTENT);

        em.flush();
        em.clear();

        //when
        final Page<ArticleSearchDto> articleSearchDtos = articleRepository.search(cond, pageable);

        //then
        assertThat(articleSearchDtos.getTotalPages())
                .as("전체 페이지 수")
                .isEqualTo(1);

        assertThat(articleSearchDtos.getTotalElements())
                .as("전체 게시글 수")
                .isEqualTo(3L);

        List<ArticleSearchDto> content = articleSearchDtos.getContent();

        assertThat(content)
                .extracting("article")
                .as("페이지 내 게시글")
                .containsExactly(article8, article7, article5);

        assertThat(content)
                .extracting("article")
                .extracting("author")
                .as("게시글 작성자 페치 조인")
                .allMatch(persistence::isLoaded);

        assertThat(content)
                .extracting("article")
                .extracting("author")
                .as("게시글 작성자")
                .containsExactly(author2, author1, author2);

        assertThat(content)
                .extracting("article")
                .extracting("comments")
                .as("댓글 로딩 지연")
                .noneMatch(persistence::isLoaded);

        assertThat(content)
                .extracting("numComments")
                .as("댓글 수")
                .containsExactly(2L, 1L, 4L);
    }

    @Test
    @DisplayName("검색 조건: 제목 및 내용 5")
    void search_cond_titleAndContent_5() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");
        User author3 = createAndSaveUser("author3", "author3@board.com", "");

        Article article1 = Article.create("    a검색aaa 정색 aa aa   ", "bbBb검색bBBbbb", author1);
        Article article2 = Article.create(" 거북이   ", "aAaAa검사aAa", author2);
        Article article3 = Article.create("bbb bbb bbbb  ", "ccc 검색 cccc ", author3);
        Article article4 = Article.create("검색 abab", "ca탈색ca caca", author1);
        Article article5 = Article.create("  bcbcBCbcbc", "ababa babab", author2);
        Article article6 = Article.create("abc ab검색Bca", "bc abcab형색cab", author3);
        Article article7 = Article.create("aa bcca bbca", "abcc abbcaa", author1);
        Article article8 = Article.create(" 영어", "baba 두더지", author2);
        Article article9 = Article.create("  a aaa검색b cabbc   ", "  한글  ", author3);
        Article article10 = Article.create("aAabbbccca", "aaAabbBb검색cc", author1);

        List<Article> articles = List.of(
                article1, article2, article3, article4, article5,
                article6, article7, article8, article9, article10
        );

        articles.forEach(em::persist);

        List<Comment> comments = generateComments(articles, author1, 0, 1, 2, 3, 4);
        comments.forEach(em::persist);

        PageRequest pageable = PageRequest.of(0, 3);

        final ArticleSearchCond cond = ArticleSearchCond.create("검색", TITLE_AND_CONTENT);

        em.flush();
        em.clear();

        //when
        final Page<ArticleSearchDto> articleSearchDtos = articleRepository.search(cond, pageable);

        //then
        assertThat(articleSearchDtos.getTotalPages())
                .as("전체 페이지 수")
                .isEqualTo(2);

        assertThat(articleSearchDtos.getTotalElements())
                .as("전체 게시글 수")
                .isEqualTo(6L);

        List<ArticleSearchDto> content = articleSearchDtos.getContent();

        assertThat(content)
                .extracting("article")
                .as("페이지 내 게시글")
                .containsExactly(article10, article9, article6);

        assertThat(content)
                .extracting("article")
                .extracting("author")
                .as("게시글 작성자 페치 조인")
                .allMatch(persistence::isLoaded);

        assertThat(content)
                .extracting("article")
                .extracting("author")
                .as("게시글 작성자")
                .containsExactly(author1, author3, author3);

        assertThat(content)
                .extracting("article")
                .extracting("comments")
                .as("댓글 로딩 지연")
                .noneMatch(persistence::isLoaded);

        assertThat(content)
                .extracting("numComments")
                .as("댓글 수")
                .containsExactly(4L, 3L, 0L);
    }

    @Test
    @DisplayName("검색 조건: 제목 및 내용 6")
    void search_cond_titleAndContent_6() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");
        User author3 = createAndSaveUser("author3", "author3@board.com", "");

        Article article1 = Article.create("    aaaa aa  aa aa   ", "bbBbbBBbbb", author1);
        Article article2 = Article.create(" CccCccCccc   ", "aAaAaAaaAa", author2);
        Article article3 = Article.create("bbb bbb bbbb  ", "ccc ccc cccc ", author3);
        Article article4 = Article.create("ababab abab", "cacaca caca", author1);
        Article article5 = Article.create("  bcbcBCbcbc", "ababa babab", author2);
        Article article6 = Article.create("abc abcaBca", "bc abcabcab", author3);
        Article article7 = Article.create("aa bcca bbca", "abcc abbcaa", author1);
        Article article8 = Article.create(" cbacbacbac", "baba bAbaba", author2);
        Article article9 = Article.create("  a aaab cabbc   ", "  baacaabaca  ", author3);
        Article article10 = Article.create("aAabbbccca", "aaAabbBbcc", author1);

        List<Article> articles = List.of(
                article1, article2, article3, article4, article5,
                article6, article7, article8, article9, article10
        );

        articles.forEach(em::persist);

        List<Comment> comments = generateComments(articles, author1, 0, 1, 2, 3, 4);
        comments.forEach(em::persist);

        PageRequest pageable = PageRequest.of(0, 3);

        final ArticleSearchCond cond = ArticleSearchCond.create("XXXXXXXXXX", TITLE_AND_CONTENT);

        em.flush();
        em.clear();

        //when
        final Page<ArticleSearchDto> articleSearchDtos = articleRepository.search(cond, pageable);

        //then
        assertThat(articleSearchDtos.getTotalPages())
                .as("전체 페이지 수")
                .isEqualTo(0);

        assertThat(articleSearchDtos.getTotalElements())
                .as("전체 게시글 수")
                .isEqualTo(0L);

        assertThat(articleSearchDtos.isEmpty())
                .as("결과 없음")
                .isTrue();
    }

    @Test
    @DisplayName("검색 조건: 제목 - 빈 조건 1")
    void search_cond_title_empty_1() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");
        User author3 = createAndSaveUser("author3", "author3@board.com", "");

        Article article1 = Article.create("    aaaa aa  aa aa   ", "bbBbbBBbbb", author1);
        Article article2 = Article.create(" CccCccCccc   ", "aAaAaAaaAa", author2);
        Article article3 = Article.create("bbb bbb bbbb  ", "ccc ccc cccc ", author3);
        Article article4 = Article.create("ababab abab", "cacaca caca", author1);
        Article article5 = Article.create("  bcbcBCbcbc", "ababa babab", author2);
        Article article6 = Article.create("abc abcaBca", "bc abcabcab", author3);
        Article article7 = Article.create("aa bcca bbca", "abcc abbcaa", author1);
        Article article8 = Article.create(" cbacbacbac", "baba bAbaba", author2);
        Article article9 = Article.create("  a aaab cabbc   ", "  baacaabaca  ", author3);
        Article article10 = Article.create("aAabbbccca", "aaAabbBbcc", author1);


        List<Article> articles = List.of(
                article1, article2, article3, article4, article5,
                article6, article7, article8, article9, article10
        );

        articles.forEach(em::persist);

        List<Comment> comments = generateComments(articles, author1, 0, 1, 2, 3, 4);
        comments.forEach(em::persist);

        PageRequest pageable = PageRequest.of(0, 3);

        final ArticleSearchCond cond = ArticleSearchCond.create("", TITLE);

        em.flush();
        em.clear();

        //when
        final Page<ArticleSearchDto> articleSearchDtos = articleRepository.search(cond, pageable);

        //then
        assertThat(articleSearchDtos.getTotalPages())
                .as("전체 페이지 수")
                .isEqualTo(4);

        assertThat(articleSearchDtos.getTotalElements())
                .as("전체 게시글 수")
                .isEqualTo(10L);

        List<ArticleSearchDto> content = articleSearchDtos.getContent();

        assertThat(content)
                .extracting("article")
                .as("페이지 내 게시글")
                .containsExactly(article10, article9, article8);

        assertThat(content)
                .extracting("article")
                .extracting("author")
                .as("게시글 작성자 페치 조인")
                .allMatch(persistence::isLoaded);

        assertThat(content)
                .extracting("article")
                .extracting("author")
                .as("게시글 작성자")
                .containsExactly(author1, author3, author2);

        assertThat(content)
                .extracting("article")
                .extracting("comments")
                .as("댓글 로딩 지연")
                .noneMatch(persistence::isLoaded);

        assertThat(content)
                .extracting("numComments")
                .as("댓글 수")
                .containsExactly(4L, 3L, 2L);
    }

    @Test
    @DisplayName("검색 조건: 제목 - 빈 조건 2")
    void search_cond_title_empty_2() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");
        User author3 = createAndSaveUser("author3", "author3@board.com", "");

        Article article1 = Article.create("    aaaa aa  aa aa   ", "bbBbbBBbbb", author1);
        Article article2 = Article.create(" CccCccCccc   ", "aAaAaAaaAa", author2);
        Article article3 = Article.create("bbb bbb bbbb  ", "ccc ccc cccc ", author3);
        Article article4 = Article.create("ababab abab", "cacaca caca", author1);
        Article article5 = Article.create("  bcbcBCbcbc", "ababa babab", author2);
        Article article6 = Article.create("abc abcaBca", "bc abcabcab", author3);
        Article article7 = Article.create("aa bcca bbca", "abcc abbcaa", author1);
        Article article8 = Article.create(" cbacbacbac", "baba bAbaba", author2);
        Article article9 = Article.create("  a aaab cabbc   ", "  baacaabaca  ", author3);
        Article article10 = Article.create("aAabbbccca", "aaAabbBbcc", author1);


        List<Article> articles = List.of(
                article1, article2, article3, article4, article5,
                article6, article7, article8, article9, article10
        );

        articles.forEach(em::persist);

        List<Comment> comments = generateComments(articles, author1, 0, 1, 2, 3, 4);
        comments.forEach(em::persist);

        PageRequest pageable = PageRequest.of(0, 3);

        final ArticleSearchCond cond = ArticleSearchCond.create(" ", TITLE);

        em.flush();
        em.clear();

        //when
        final Page<ArticleSearchDto> articleSearchDtos = articleRepository.search(cond, pageable);

        //then
        assertThat(articleSearchDtos.getTotalPages())
                .as("전체 페이지 수")
                .isEqualTo(4);

        assertThat(articleSearchDtos.getTotalElements())
                .as("전체 게시글 수")
                .isEqualTo(10L);

        List<ArticleSearchDto> content = articleSearchDtos.getContent();

        assertThat(content)
                .extracting("article")
                .as("페이지 내 게시글")
                .containsExactly(article10, article9, article8);

        assertThat(content)
                .extracting("article")
                .extracting("author")
                .as("게시글 작성자 페치 조인")
                .allMatch(persistence::isLoaded);

        assertThat(content)
                .extracting("article")
                .extracting("author")
                .as("게시글 작성자")
                .containsExactly(author1, author3, author2);

        assertThat(content)
                .extracting("article")
                .extracting("comments")
                .as("댓글 로딩 지연")
                .noneMatch(persistence::isLoaded);

        assertThat(content)
                .extracting("numComments")
                .as("댓글 수")
                .containsExactly(4L, 3L, 2L);
    }

    @Test
    @DisplayName("검색 조건: 제목 1")
    void search_cond_title_1() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");
        User author3 = createAndSaveUser("author3", "author3@board.com", "");

        Article article1 = Article.create("    aaaa aa  aa aa   ", "bbBbbBBbbb", author1);
        Article article2 = Article.create(" CccCccCccc   ", "aAaAaAaaAa", author2);
        Article article3 = Article.create("bbb bbb bbbb  ", "ccc ccc cccc ", author3);
        Article article4 = Article.create("ababab abab", "cacaca caca", author1);
        Article article5 = Article.create("  bcbcBCbcbc", "ababa babab", author2);
        Article article6 = Article.create("abc abcaBca", "bc abcabcab", author3);
        Article article7 = Article.create("aa bcca bbca", "abcc abbcaa", author1);
        Article article8 = Article.create(" cbacbacbac", "baba bAbaba", author2);
        Article article9 = Article.create("  a aaab cabbc   ", "  baacaabaca  ", author3);
        Article article10 = Article.create("aAabbbccca", "aaAabbBbcc", author1);

        List<Article> articles = List.of(
                article1, article2, article3, article4, article5,
                article6, article7, article8, article9, article10
        );

        articles.forEach(em::persist);

        List<Comment> comments = generateComments(articles, author1, 0, 1, 2, 3, 4);
        comments.forEach(em::persist);

        PageRequest pageable = PageRequest.of(0, 3);

        final ArticleSearchCond cond = ArticleSearchCond.create("aaa", TITLE);

        em.flush();
        em.clear();

        //when
        final Page<ArticleSearchDto> articleSearchDtos = articleRepository.search(cond, pageable);

        //then
        assertThat(articleSearchDtos.getTotalPages())
                .as("전체 페이지 수")
                .isEqualTo(1);

        assertThat(articleSearchDtos.getTotalElements())
                .as("전체 게시글 수")
                .isEqualTo(3L);

        List<ArticleSearchDto> content = articleSearchDtos.getContent();

        assertThat(content)
                .extracting("article")
                .as("페이지 내 게시글")
                .containsExactly(article10, article9, article1);

        assertThat(content)
                .extracting("article")
                .extracting("author")
                .as("게시글 작성자 페치 조인")
                .allMatch(persistence::isLoaded);

        assertThat(content)
                .extracting("article")
                .extracting("author")
                .as("게시글 작성자")
                .containsExactly(author1, author3, author1);

        assertThat(content)
                .extracting("article")
                .extracting("comments")
                .as("댓글 로딩 지연")
                .noneMatch(persistence::isLoaded);

        assertThat(content)
                .extracting("numComments")
                .as("댓글 수")
                .containsExactly(4L, 3L, 0L);
    }

    @Test
    @DisplayName("검색 조건: 제목 2")
    void search_cond_title_2() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");
        User author3 = createAndSaveUser("author3", "author3@board.com", "");

        Article article1 = Article.create("    aaaa aa  aa aa   ", "bbBbbBBbbb", author1);
        Article article2 = Article.create(" CccCccCccc   ", "aAaAaAaaAa", author2);
        Article article3 = Article.create("bbb bbb bbbb  ", "ccc ccc cccc ", author3);
        Article article4 = Article.create("ababab abab", "cacaca caca", author1);
        Article article5 = Article.create("  bcbcBCbcbc", "ababa babab", author2);
        Article article6 = Article.create("abc abcaBca", "bc abcabcab", author3);
        Article article7 = Article.create("aa bcca bbca", "abcc abbcaa", author1);
        Article article8 = Article.create(" cbacbacbac", "baba bAbaba", author2);
        Article article9 = Article.create("  a aaab cabbc   ", "  baacaabaca  ", author3);
        Article article10 = Article.create("aAabbbccca", "aaAabbBbcc", author1);

        List<Article> articles = List.of(
                article1, article2, article3, article4, article5,
                article6, article7, article8, article9, article10
        );

        articles.forEach(em::persist);

        List<Comment> comments = generateComments(articles, author1, 0, 1, 2, 3, 4);
        comments.forEach(em::persist);

        PageRequest pageable = PageRequest.of(0, 3);

        final ArticleSearchCond cond = ArticleSearchCond.create("ABc", TITLE);

        em.flush();
        em.clear();

        //when
        final Page<ArticleSearchDto> articleSearchDtos = articleRepository.search(cond, pageable);

        //then
        assertThat(articleSearchDtos.getTotalPages())
                .as("전체 페이지 수")
                .isEqualTo(1);

        assertThat(articleSearchDtos.getTotalElements())
                .as("전체 게시글 수")
                .isEqualTo(1L);

        List<ArticleSearchDto> content = articleSearchDtos.getContent();

        assertThat(content)
                .extracting("article")
                .as("페이지 내 게시글")
                .containsExactly(article6);

        assertThat(content)
                .extracting("article")
                .extracting("author")
                .as("게시글 작성자 페치 조인")
                .allMatch(persistence::isLoaded);

        assertThat(content)
                .extracting("article")
                .extracting("author")
                .as("게시글 작성자")
                .containsExactly(author3);

        assertThat(content)
                .extracting("article")
                .extracting("comments")
                .as("댓글 로딩 지연")
                .noneMatch(persistence::isLoaded);

        assertThat(content)
                .extracting("numComments")
                .as("댓글 수")
                .containsExactly(0L);
    }

    @Test
    @DisplayName("검색 조건: 제목 및 내용 3")
    void search_cond_title_3() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");
        User author3 = createAndSaveUser("author3", "author3@board.com", "");

        Article article1 = Article.create("    aaaa aa  aa aa   ", "bbBbbBBbbb", author1);
        Article article2 = Article.create(" CccCccCccc   ", "aAaAaAaaAa", author2);
        Article article3 = Article.create("bbb bbb bbbb  ", "ccc ccc cccc ", author3);
        Article article4 = Article.create("ababab abab", "cacaca caca", author1);
        Article article5 = Article.create("  bcbcBCbcbc", "ababa babab", author2);
        Article article6 = Article.create("abc abcaBca", "bc abcabcab", author3);
        Article article7 = Article.create("aa bcca bbca", "abcc abbcaa", author1);
        Article article8 = Article.create(" cbacbacbac", "baba bAbaba", author2);
        Article article9 = Article.create("  a aaab cabbc   ", "  baacaabaca  ", author3);
        Article article10 = Article.create("aAabbbccca", "aaAabbBbcc", author1);

        List<Article> articles = List.of(
                article1, article2, article3, article4, article5,
                article6, article7, article8, article9, article10
        );

        articles.forEach(em::persist);

        List<Comment> comments = generateComments(articles, author1, 0, 1, 2, 3, 4);
        comments.forEach(em::persist);

        PageRequest pageable = PageRequest.of(0, 3);

        final ArticleSearchCond cond = ArticleSearchCond.create(" AA  ", TITLE);

        em.flush();
        em.clear();

        //when
        final Page<ArticleSearchDto> articleSearchDtos = articleRepository.search(cond, pageable);

        //then
        assertThat(articleSearchDtos.getTotalPages())
                .as("전체 페이지 수")
                .isEqualTo(2);

        assertThat(articleSearchDtos.getTotalElements())
                .as("전체 게시글 수")
                .isEqualTo(4L);

        List<ArticleSearchDto> content = articleSearchDtos.getContent();

        assertThat(content)
                .extracting("article")
                .as("페이지 내 게시글")
                .containsExactly(article10, article9, article7);

        assertThat(content)
                .extracting("article")
                .extracting("author")
                .as("게시글 작성자 페치 조인")
                .allMatch(persistence::isLoaded);

        assertThat(content)
                .extracting("article")
                .extracting("author")
                .as("게시글 작성자")
                .containsExactly(author1, author3, author1);

        assertThat(content)
                .extracting("article")
                .extracting("comments")
                .as("댓글 로딩 지연")
                .noneMatch(persistence::isLoaded);

        assertThat(content)
                .extracting("numComments")
                .as("댓글 수")
                .containsExactly(4L, 3L, 1L);
    }

    @Test
    @DisplayName("검색 조건: 제목 및 내용 4")
    void search_cond_title_4() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");
        User author3 = createAndSaveUser("author3", "author3@board.com", "");

        Article article1 = Article.create("    aaaa aa  aa aa   ", "bbBbbBBbbb", author1);
        Article article2 = Article.create(" CccCccCccc   ", "aAaAaAaaAa", author2);
        Article article3 = Article.create("bbb bbb bbbb  ", "ccc ccc cccc ", author3);
        Article article4 = Article.create("ababab abab", "cacaca caca", author1);
        Article article5 = Article.create("  bcbcBCbcbc", "ababa babab", author2);
        Article article6 = Article.create("abc abcaBca", "bc abcabcab", author3);
        Article article7 = Article.create("aa bcca bbca", "abcc abbcaa", author1);
        Article article8 = Article.create(" cbacbacbac", "baba bAbaba", author2);
        Article article9 = Article.create("  a aaab cabbc   ", "  baacaabaca  ", author3);
        Article article10 = Article.create("aAabbbccca", "aaAabbBbcc", author1);

        List<Article> articles = List.of(
                article1, article2, article3, article4, article5,
                article6, article7, article8, article9, article10
        );

        articles.forEach(em::persist);

        List<Comment> comments = generateComments(articles, author1, 0, 1, 2, 3, 4);
        comments.forEach(em::persist);

        PageRequest pageable = PageRequest.of(0, 3);

        final ArticleSearchCond cond = ArticleSearchCond.create("a b", TITLE);

        em.flush();
        em.clear();

        //when
        final Page<ArticleSearchDto> articleSearchDtos = articleRepository.search(cond, pageable);

        //then
        assertThat(articleSearchDtos.getTotalPages())
                .as("전체 페이지 수")
                .isEqualTo(1);

        assertThat(articleSearchDtos.getTotalElements())
                .as("전체 게시글 수")
                .isEqualTo(1L);

        List<ArticleSearchDto> content = articleSearchDtos.getContent();

        assertThat(content)
                .extracting("article")
                .as("페이지 내 게시글")
                .containsExactly(article7);

        assertThat(content)
                .extracting("article")
                .extracting("author")
                .as("게시글 작성자 페치 조인")
                .allMatch(persistence::isLoaded);

        assertThat(content)
                .extracting("article")
                .extracting("author")
                .as("게시글 작성자")
                .containsExactly(author1);

        assertThat(content)
                .extracting("article")
                .extracting("comments")
                .as("댓글 로딩 지연")
                .noneMatch(persistence::isLoaded);

        assertThat(content)
                .extracting("numComments")
                .as("댓글 수")
                .containsExactly(1L);
    }

    @Test
    @DisplayName("검색 조건: 제목 및 내용 5")
    void search_cond_title_5() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");
        User author3 = createAndSaveUser("author3", "author3@board.com", "");

        Article article1 = Article.create("    a검색aaa 정색 aa aa   ", "bbBb검색bBBbbb", author1);
        Article article2 = Article.create(" 거북이   ", "aAaAa검사aAa", author2);
        Article article3 = Article.create("bbb bbb bbbb  ", "ccc 검색 cccc ", author3);
        Article article4 = Article.create("검색 abab", "ca탈색ca caca", author1);
        Article article5 = Article.create("  bcbcBCbcbc", "ababa babab", author2);
        Article article6 = Article.create("abc ab검색Bca", "bc abcab형색cab", author3);
        Article article7 = Article.create("aa bcca bbca", "abcc abbcaa", author1);
        Article article8 = Article.create(" 영어", "baba 두더지", author2);
        Article article9 = Article.create("  a aaa검색b cabbc   ", "  한글  ", author3);
        Article article10 = Article.create("aAabbbccca", "aaAabbBb검색cc", author1);

        List<Article> articles = List.of(
                article1, article2, article3, article4, article5,
                article6, article7, article8, article9, article10
        );

        articles.forEach(em::persist);

        List<Comment> comments = generateComments(articles, author1, 0, 1, 2, 3, 4);
        comments.forEach(em::persist);

        PageRequest pageable = PageRequest.of(0, 3);

        final ArticleSearchCond cond = ArticleSearchCond.create("검색", TITLE);

        em.flush();
        em.clear();

        //when
        final Page<ArticleSearchDto> articleSearchDtos = articleRepository.search(cond, pageable);

        //then
        assertThat(articleSearchDtos.getTotalPages())
                .as("전체 페이지 수")
                .isEqualTo(2);

        assertThat(articleSearchDtos.getTotalElements())
                .as("전체 게시글 수")
                .isEqualTo(4L);

        List<ArticleSearchDto> content = articleSearchDtos.getContent();

        assertThat(content)
                .extracting("article")
                .as("페이지 내 게시글")
                .containsExactly(article9, article6, article4);

        assertThat(content)
                .extracting("article")
                .extracting("author")
                .as("게시글 작성자 페치 조인")
                .allMatch(persistence::isLoaded);

        assertThat(content)
                .extracting("article")
                .extracting("author")
                .as("게시글 작성자")
                .containsExactly(author3, author3, author1);

        assertThat(content)
                .extracting("article")
                .extracting("comments")
                .as("댓글 로딩 지연")
                .noneMatch(persistence::isLoaded);

        assertThat(content)
                .extracting("numComments")
                .as("댓글 수")
                .containsExactly(3L, 0L, 3L);
    }

    @Test
    @DisplayName("검색 조건: 제목 6")
    void search_cond_title_6() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");
        User author3 = createAndSaveUser("author3", "author3@board.com", "");

        Article article1 = Article.create("    aaaa aa  aa aa   ", "bbBbbBBbbb", author1);
        Article article2 = Article.create(" CccCccCccc   ", "aAaAaAaaAa", author2);
        Article article3 = Article.create("bbb bbb bbbb  ", "ccc ccc cccc ", author3);
        Article article4 = Article.create("ababab abab", "cacaca caca", author1);
        Article article5 = Article.create("  bcbcBCbcbc", "ababa babab", author2);
        Article article6 = Article.create("abc abcaBca", "bc abcabcab", author3);
        Article article7 = Article.create("aa bcca bbca", "abcc abbcaa", author1);
        Article article8 = Article.create(" cbacbacbac", "baba bAbaba", author2);
        Article article9 = Article.create("  a aaab cabbc   ", "  baacaabaca  ", author3);
        Article article10 = Article.create("aAabbbccca", "aaAabbBbcc", author1);

        List<Article> articles = List.of(
                article1, article2, article3, article4, article5,
                article6, article7, article8, article9, article10
        );

        articles.forEach(em::persist);

        List<Comment> comments = generateComments(articles, author1, 0, 1, 2, 3, 4);
        comments.forEach(em::persist);

        PageRequest pageable = PageRequest.of(0, 3);

        final ArticleSearchCond cond = ArticleSearchCond.create("XXXXXXXXXX", TITLE);

        em.flush();
        em.clear();

        //when
        final Page<ArticleSearchDto> articleSearchDtos = articleRepository.search(cond, pageable);

        //then
        assertThat(articleSearchDtos.getTotalPages())
                .as("전체 페이지 수")
                .isEqualTo(0);

        assertThat(articleSearchDtos.getTotalElements())
                .as("전체 게시글 수")
                .isEqualTo(0L);

        assertThat(articleSearchDtos.isEmpty())
                .as("결과 없음")
                .isTrue();
    }

    @Test
    @DisplayName("검색 조건: 내용 - 빈 조건 1")
    void search_cond_content_empty_1() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");
        User author3 = createAndSaveUser("author3", "author3@board.com", "");

        Article article1 = Article.create("    aaaa aa  aa aa   ", "bbBbbBBbbb", author1);
        Article article2 = Article.create(" CccCccCccc   ", "aAaAaAaaAa", author2);
        Article article3 = Article.create("bbb bbb bbbb  ", "ccc ccc cccc ", author3);
        Article article4 = Article.create("ababab abab", "cacaca caca", author1);
        Article article5 = Article.create("  bcbcBCbcbc", "ababa babab", author2);
        Article article6 = Article.create("abc abcaBca", "bc abcabcab", author3);
        Article article7 = Article.create("aa bcca bbca", "abcc abbcaa", author1);
        Article article8 = Article.create(" cbacbacbac", "baba bAbaba", author2);
        Article article9 = Article.create("  a aaab cabbc   ", "  baacaabaca  ", author3);
        Article article10 = Article.create("aAabbbccca", "aaAabbBbcc", author1);


        List<Article> articles = List.of(
                article1, article2, article3, article4, article5,
                article6, article7, article8, article9, article10
        );

        articles.forEach(em::persist);

        List<Comment> comments = generateComments(articles, author1, 0, 1, 2, 3, 4);
        comments.forEach(em::persist);

        PageRequest pageable = PageRequest.of(0, 3);

        final ArticleSearchCond cond = ArticleSearchCond.create("", CONTENT);

        em.flush();
        em.clear();

        //when
        final Page<ArticleSearchDto> articleSearchDtos = articleRepository.search(cond, pageable);

        //then
        assertThat(articleSearchDtos.getTotalPages())
                .as("전체 페이지 수")
                .isEqualTo(4);

        assertThat(articleSearchDtos.getTotalElements())
                .as("전체 게시글 수")
                .isEqualTo(10L);

        List<ArticleSearchDto> content = articleSearchDtos.getContent();

        assertThat(content)
                .extracting("article")
                .as("페이지 내 게시글")
                .containsExactly(article10, article9, article8);

        assertThat(content)
                .extracting("article")
                .extracting("author")
                .as("게시글 작성자 페치 조인")
                .allMatch(persistence::isLoaded);

        assertThat(content)
                .extracting("article")
                .extracting("author")
                .as("게시글 작성자")
                .containsExactly(author1, author3, author2);

        assertThat(content)
                .extracting("article")
                .extracting("comments")
                .as("댓글 로딩 지연")
                .noneMatch(persistence::isLoaded);

        assertThat(content)
                .extracting("numComments")
                .as("댓글 수")
                .containsExactly(4L, 3L, 2L);
    }

    @Test
    @DisplayName("검색 조건: 내용 - 빈 조건 2")
    void search_cond_content_empty_2() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");
        User author3 = createAndSaveUser("author3", "author3@board.com", "");

        Article article1 = Article.create("    aaaa aa  aa aa   ", "bbBbbBBbbb", author1);
        Article article2 = Article.create(" CccCccCccc   ", "aAaAaAaaAa", author2);
        Article article3 = Article.create("bbb bbb bbbb  ", "ccc ccc cccc ", author3);
        Article article4 = Article.create("ababab abab", "cacaca caca", author1);
        Article article5 = Article.create("  bcbcBCbcbc", "ababa babab", author2);
        Article article6 = Article.create("abc abcaBca", "bc abcabcab", author3);
        Article article7 = Article.create("aa bcca bbca", "abcc abbcaa", author1);
        Article article8 = Article.create(" cbacbacbac", "baba bAbaba", author2);
        Article article9 = Article.create("  a aaab cabbc   ", "  baacaabaca  ", author3);
        Article article10 = Article.create("aAabbbccca", "aaAabbBbcc", author1);


        List<Article> articles = List.of(
                article1, article2, article3, article4, article5,
                article6, article7, article8, article9, article10
        );

        articles.forEach(em::persist);

        List<Comment> comments = generateComments(articles, author1, 0, 1, 2, 3, 4);
        comments.forEach(em::persist);

        PageRequest pageable = PageRequest.of(0, 3);

        final ArticleSearchCond cond = ArticleSearchCond.create(" ", CONTENT);

        em.flush();
        em.clear();

        //when
        final Page<ArticleSearchDto> articleSearchDtos = articleRepository.search(cond, pageable);

        //then
        assertThat(articleSearchDtos.getTotalPages())
                .as("전체 페이지 수")
                .isEqualTo(4);

        assertThat(articleSearchDtos.getTotalElements())
                .as("전체 게시글 수")
                .isEqualTo(10L);

        List<ArticleSearchDto> content = articleSearchDtos.getContent();

        assertThat(content)
                .extracting("article")
                .as("페이지 내 게시글")
                .containsExactly(article10, article9, article8);

        assertThat(content)
                .extracting("article")
                .extracting("author")
                .as("게시글 작성자 페치 조인")
                .allMatch(persistence::isLoaded);

        assertThat(content)
                .extracting("article")
                .extracting("author")
                .as("게시글 작성자")
                .containsExactly(author1, author3, author2);

        assertThat(content)
                .extracting("article")
                .extracting("comments")
                .as("댓글 로딩 지연")
                .noneMatch(persistence::isLoaded);

        assertThat(content)
                .extracting("numComments")
                .as("댓글 수")
                .containsExactly(4L, 3L, 2L);
    }

    @Test
    @DisplayName("검색 조건: 내용 1")
    void search_cond_content_1() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");
        User author3 = createAndSaveUser("author3", "author3@board.com", "");

        Article article1 = Article.create("    aaaa aa  aa aa   ", "bbBbbBBbbb", author1);
        Article article2 = Article.create(" CccCccCccc   ", "aAaAaAaaAa", author2);
        Article article3 = Article.create("bbb bbb bbbb  ", "ccc ccc cccc ", author3);
        Article article4 = Article.create("ababab abab", "cacaca caca", author1);
        Article article5 = Article.create("  bcbcBCbcbc", "ababa babab", author2);
        Article article6 = Article.create("abc abcaBca", "bc abcabcab", author3);
        Article article7 = Article.create("aa bcca bbca", "abcc abbcaa", author1);
        Article article8 = Article.create(" cbacbacbac", "baba bAbaba", author2);
        Article article9 = Article.create("  a aaab cabbc   ", "  baacaabaca  ", author3);
        Article article10 = Article.create("aAabbbccca", "aaAabbBbcc", author1);

        List<Article> articles = List.of(
                article1, article2, article3, article4, article5,
                article6, article7, article8, article9, article10
        );

        articles.forEach(em::persist);

        List<Comment> comments = generateComments(articles, author1, 0, 1, 2, 3, 4);
        comments.forEach(em::persist);

        PageRequest pageable = PageRequest.of(0, 3);

        final ArticleSearchCond cond = ArticleSearchCond.create("aaa", CONTENT);

        em.flush();
        em.clear();

        //when
        final Page<ArticleSearchDto> articleSearchDtos = articleRepository.search(cond, pageable);

        //then
        assertThat(articleSearchDtos.getTotalPages())
                .as("전체 페이지 수")
                .isEqualTo(1);

        assertThat(articleSearchDtos.getTotalElements())
                .as("전체 게시글 수")
                .isEqualTo(2L);

        List<ArticleSearchDto> content = articleSearchDtos.getContent();

        assertThat(content)
                .extracting("article")
                .as("페이지 내 게시글")
                .containsExactly(article10, article2);

        assertThat(content)
                .extracting("article")
                .extracting("author")
                .as("게시글 작성자 페치 조인")
                .allMatch(persistence::isLoaded);

        assertThat(content)
                .extracting("article")
                .extracting("author")
                .as("게시글 작성자")
                .containsExactly(author1, author2);

        assertThat(content)
                .extracting("article")
                .extracting("comments")
                .as("댓글 로딩 지연")
                .noneMatch(persistence::isLoaded);

        assertThat(content)
                .extracting("numComments")
                .as("댓글 수")
                .containsExactly(4L, 1L);
    }

    @Test
    @DisplayName("검색 조건: 내용 2")
    void search_cond_content_2() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");
        User author3 = createAndSaveUser("author3", "author3@board.com", "");

        Article article1 = Article.create("    aaaa aa  aa aa   ", "bbBbbBBbbb", author1);
        Article article2 = Article.create(" CccCccCccc   ", "aAaAaAaaAa", author2);
        Article article3 = Article.create("bbb bbb bbbb  ", "ccc ccc cccc ", author3);
        Article article4 = Article.create("ababab abab", "cacaca caca", author1);
        Article article5 = Article.create("  bcbcBCbcbc", "ababa babab", author2);
        Article article6 = Article.create("abc abcaBca", "bc abcabcab", author3);
        Article article7 = Article.create("aa bcca bbca", "abcc abbcaa", author1);
        Article article8 = Article.create(" cbacbacbac", "baba bAbaba", author2);
        Article article9 = Article.create("  a aaab cabbc   ", "  baacaabaca  ", author3);
        Article article10 = Article.create("aAabbbccca", "aaAabbBbcc", author1);

        List<Article> articles = List.of(
                article1, article2, article3, article4, article5,
                article6, article7, article8, article9, article10
        );

        articles.forEach(em::persist);

        List<Comment> comments = generateComments(articles, author1, 0, 1, 2, 3, 4);
        comments.forEach(em::persist);

        PageRequest pageable = PageRequest.of(0, 3);

        final ArticleSearchCond cond = ArticleSearchCond.create("ABc", CONTENT);

        em.flush();
        em.clear();

        //when
        final Page<ArticleSearchDto> articleSearchDtos = articleRepository.search(cond, pageable);

        //then
        assertThat(articleSearchDtos.getTotalPages())
                .as("전체 페이지 수")
                .isEqualTo(1);

        assertThat(articleSearchDtos.getTotalElements())
                .as("전체 게시글 수")
                .isEqualTo(2L);

        List<ArticleSearchDto> content = articleSearchDtos.getContent();

        assertThat(content)
                .extracting("article")
                .as("페이지 내 게시글")
                .containsExactly(article7, article6);

        assertThat(content)
                .extracting("article")
                .extracting("author")
                .as("게시글 작성자 페치 조인")
                .allMatch(persistence::isLoaded);

        assertThat(content)
                .extracting("article")
                .extracting("author")
                .as("게시글 작성자")
                .containsExactly(author1, author3);

        assertThat(content)
                .extracting("article")
                .extracting("comments")
                .as("댓글 로딩 지연")
                .noneMatch(persistence::isLoaded);

        assertThat(content)
                .extracting("numComments")
                .as("댓글 수")
                .containsExactly(1L, 0L);
    }

    @Test
    @DisplayName("검색 조건: 내용 3")
    void search_cond_content_3() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");
        User author3 = createAndSaveUser("author3", "author3@board.com", "");

        Article article1 = Article.create("    aaaa aa  aa aa   ", "bbBbbBBbbb", author1);
        Article article2 = Article.create(" CccCccCccc   ", "aAaAaAaaAa", author2);
        Article article3 = Article.create("bbb bbb bbbb  ", "ccc ccc cccc ", author3);
        Article article4 = Article.create("ababab abab", "cacaca caca", author1);
        Article article5 = Article.create("  bcbcBCbcbc", "ababa babab", author2);
        Article article6 = Article.create("abc abcaBca", "bc abcabcab", author3);
        Article article7 = Article.create("aa bcca bbca", "abcc abbcaa", author1);
        Article article8 = Article.create(" cbacbacbac", "baba bAbaba", author2);
        Article article9 = Article.create("  a aaab cabbc   ", "  baacaabaca  ", author3);
        Article article10 = Article.create("aAabbbccca", "aaAabbBbcc", author1);

        List<Article> articles = List.of(
                article1, article2, article3, article4, article5,
                article6, article7, article8, article9, article10
        );

        articles.forEach(em::persist);

        List<Comment> comments = generateComments(articles, author1, 0, 1, 2, 3, 4);
        comments.forEach(em::persist);

        PageRequest pageable = PageRequest.of(0, 3);

        final ArticleSearchCond cond = ArticleSearchCond.create(" AA  ", CONTENT);

        em.flush();
        em.clear();

        //when
        final Page<ArticleSearchDto> articleSearchDtos = articleRepository.search(cond, pageable);

        //then
        assertThat(articleSearchDtos.getTotalPages())
                .as("전체 페이지 수")
                .isEqualTo(2);

        assertThat(articleSearchDtos.getTotalElements())
                .as("전체 게시글 수")
                .isEqualTo(4L);

        List<ArticleSearchDto> content = articleSearchDtos.getContent();

        assertThat(content)
                .extracting("article")
                .as("페이지 내 게시글")
                .containsExactly(article10, article9, article7);

        assertThat(content)
                .extracting("article")
                .extracting("author")
                .as("게시글 작성자 페치 조인")
                .allMatch(persistence::isLoaded);

        assertThat(content)
                .extracting("article")
                .extracting("author")
                .as("게시글 작성자")
                .containsExactly(author1, author3, author1);

        assertThat(content)
                .extracting("article")
                .extracting("comments")
                .as("댓글 로딩 지연")
                .noneMatch(persistence::isLoaded);

        assertThat(content)
                .extracting("numComments")
                .as("댓글 수")
                .containsExactly(4L, 3L, 1L);
    }

    @Test
    @DisplayName("검색 조건: 내용 4")
    void search_cond_content_4() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");
        User author3 = createAndSaveUser("author3", "author3@board.com", "");

        Article article1 = Article.create("    aaaa aa  aa aa   ", "bbBbbBBbbb", author1);
        Article article2 = Article.create(" CccCccCccc   ", "aAaAaAaaAa", author2);
        Article article3 = Article.create("bbb bbb bbbb  ", "ccc ccc cccc ", author3);
        Article article4 = Article.create("ababab abab", "cacaca caca", author1);
        Article article5 = Article.create("  bcbcBCbcbc", "ababa babab", author2);
        Article article6 = Article.create("abc abcaBca", "bc abcabcab", author3);
        Article article7 = Article.create("aa bcca bbca", "abcc abbcaa", author1);
        Article article8 = Article.create(" cbacbacbac", "baba bAbaba", author2);
        Article article9 = Article.create("  a aaab cabbc   ", "  baacaabaca  ", author3);
        Article article10 = Article.create("aAabbbccca", "aaAabbBbcc", author1);

        List<Article> articles = List.of(
                article1, article2, article3, article4, article5,
                article6, article7, article8, article9, article10
        );

        articles.forEach(em::persist);

        List<Comment> comments = generateComments(articles, author1, 0, 1, 2, 3, 4);
        comments.forEach(em::persist);

        PageRequest pageable = PageRequest.of(0, 3);

        final ArticleSearchCond cond = ArticleSearchCond.create("a b", CONTENT);

        em.flush();
        em.clear();

        //when
        final Page<ArticleSearchDto> articleSearchDtos = articleRepository.search(cond, pageable);

        //then
        assertThat(articleSearchDtos.getTotalPages())
                .as("전체 페이지 수")
                .isEqualTo(1);

        assertThat(articleSearchDtos.getTotalElements())
                .as("전체 게시글 수")
                .isEqualTo(2L);

        List<ArticleSearchDto> content = articleSearchDtos.getContent();

        assertThat(content)
                .extracting("article")
                .as("페이지 내 게시글")
                .containsExactly(article8, article5);

        assertThat(content)
                .extracting("article")
                .extracting("author")
                .as("게시글 작성자 페치 조인")
                .allMatch(persistence::isLoaded);

        assertThat(content)
                .extracting("article")
                .extracting("author")
                .as("게시글 작성자")
                .containsExactly(author2, author2);

        assertThat(content)
                .extracting("article")
                .extracting("comments")
                .as("댓글 로딩 지연")
                .noneMatch(persistence::isLoaded);

        assertThat(content)
                .extracting("numComments")
                .as("댓글 수")
                .containsExactly(2L, 4L);
    }

    @Test
    @DisplayName("검색 조건: 내용 5")
    void search_cond_content_5() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");
        User author3 = createAndSaveUser("author3", "author3@board.com", "");

        Article article1 = Article.create("    a검색aaa 정색 aa aa   ", "bbBb검색bBBbbb", author1);
        Article article2 = Article.create(" 거북이   ", "aAaAa검사aAa", author2);
        Article article3 = Article.create("bbb bbb bbbb  ", "ccc 검색 cccc ", author3);
        Article article4 = Article.create("검색 abab", "ca탈색ca caca", author1);
        Article article5 = Article.create("  bcbcBCbcbc", "ababa babab", author2);
        Article article6 = Article.create("abc ab검색Bca", "bc abcab형색cab", author3);
        Article article7 = Article.create("aa bcca bbca", "abcc abbcaa", author1);
        Article article8 = Article.create(" 영어", "baba 두더지", author2);
        Article article9 = Article.create("  a aaa검색b cabbc   ", "  한글  ", author3);
        Article article10 = Article.create("aAabbbccca", "aaAabbBb검색cc", author1);

        List<Article> articles = List.of(
                article1, article2, article3, article4, article5,
                article6, article7, article8, article9, article10
        );

        articles.forEach(em::persist);

        List<Comment> comments = generateComments(articles, author1, 0, 1, 2, 3, 4);
        comments.forEach(em::persist);

        PageRequest pageable = PageRequest.of(0, 3);

        final ArticleSearchCond cond = ArticleSearchCond.create("검색", CONTENT);

        em.flush();
        em.clear();

        //when
        final Page<ArticleSearchDto> articleSearchDtos = articleRepository.search(cond, pageable);

        //then
        assertThat(articleSearchDtos.getTotalPages())
                .as("전체 페이지 수")
                .isEqualTo(1);

        assertThat(articleSearchDtos.getTotalElements())
                .as("전체 게시글 수")
                .isEqualTo(3L);

        List<ArticleSearchDto> content = articleSearchDtos.getContent();

        assertThat(content)
                .extracting("article")
                .as("페이지 내 게시글")
                .containsExactly(article10, article3, article1);

        assertThat(content)
                .extracting("article")
                .extracting("author")
                .as("게시글 작성자 페치 조인")
                .allMatch(persistence::isLoaded);

        assertThat(content)
                .extracting("article")
                .extracting("author")
                .as("게시글 작성자")
                .containsExactly(author1, author3, author1);

        assertThat(content)
                .extracting("article")
                .extracting("comments")
                .as("댓글 로딩 지연")
                .noneMatch(persistence::isLoaded);

        assertThat(content)
                .extracting("numComments")
                .as("댓글 수")
                .containsExactly(4L, 2L, 0L);
    }

    @Test
    @DisplayName("검색 조건: 내용 6")
    void search_cond_content_6() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");
        User author3 = createAndSaveUser("author3", "author3@board.com", "");

        Article article1 = Article.create("    aaaa aa  aa aa   ", "bbBbbBBbbb", author1);
        Article article2 = Article.create(" CccCccCccc   ", "aAaAaAaaAa", author2);
        Article article3 = Article.create("bbb bbb bbbb  ", "ccc ccc cccc ", author3);
        Article article4 = Article.create("ababab abab", "cacaca caca", author1);
        Article article5 = Article.create("  bcbcBCbcbc", "ababa babab", author2);
        Article article6 = Article.create("abc abcaBca", "bc abcabcab", author3);
        Article article7 = Article.create("aa bcca bbca", "abcc abbcaa", author1);
        Article article8 = Article.create(" cbacbacbac", "baba bAbaba", author2);
        Article article9 = Article.create("  a aaab cabbc   ", "  baacaabaca  ", author3);
        Article article10 = Article.create("aAabbbccca", "aaAabbBbcc", author1);

        List<Article> articles = List.of(
                article1, article2, article3, article4, article5,
                article6, article7, article8, article9, article10
        );

        articles.forEach(em::persist);

        List<Comment> comments = generateComments(articles, author1, 0, 1, 2, 3, 4);
        comments.forEach(em::persist);

        PageRequest pageable = PageRequest.of(0, 3);

        final ArticleSearchCond cond = ArticleSearchCond.create("XXXXXXXXXX", CONTENT);

        em.flush();
        em.clear();

        //when
        final Page<ArticleSearchDto> articleSearchDtos = articleRepository.search(cond, pageable);

        //then
        assertThat(articleSearchDtos.getTotalPages())
                .as("전체 페이지 수")
                .isEqualTo(0);

        assertThat(articleSearchDtos.getTotalElements())
                .as("전체 게시글 수")
                .isEqualTo(0L);

        assertThat(articleSearchDtos.isEmpty())
                .as("결과 없음")
                .isTrue();
    }

    @Test
    @DisplayName("검색 조건: 작성자 - 빈 조건 1")
    void search_cond_author_empty_1() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");
        User author3 = createAndSaveUser("author3", "author3@board.com", "");

        Article article1 = Article.create("    aaaa aa  aa aa   ", "bbBbbBBbbb", author1);
        Article article2 = Article.create(" CccCccCccc   ", "aAaAaAaaAa", author2);
        Article article3 = Article.create("bbb bbb bbbb  ", "ccc ccc cccc ", author3);
        Article article4 = Article.create("ababab abab", "cacaca caca", author1);
        Article article5 = Article.create("  bcbcBCbcbc", "ababa babab", author2);
        Article article6 = Article.create("abc abcaBca", "bc abcabcab", author3);
        Article article7 = Article.create("aa bcca bbca", "abcc abbcaa", author1);
        Article article8 = Article.create(" cbacbacbac", "baba bAbaba", author2);
        Article article9 = Article.create("  a aaab cabbc   ", "  baacaabaca  ", author3);
        Article article10 = Article.create("aAabbbccca", "aaAabbBbcc", author1);


        List<Article> articles = List.of(
                article1, article2, article3, article4, article5,
                article6, article7, article8, article9, article10
        );

        articles.forEach(em::persist);

        List<Comment> comments = generateComments(articles, author1, 0, 1, 2, 3, 4);
        comments.forEach(em::persist);

        PageRequest pageable = PageRequest.of(0, 3);

        final ArticleSearchCond cond = ArticleSearchCond.create("", AUTHOR);

        em.flush();
        em.clear();

        //when
        final Page<ArticleSearchDto> articleSearchDtos = articleRepository.search(cond, pageable);

        //then
        assertThat(articleSearchDtos.getTotalPages())
                .as("전체 페이지 수")
                .isEqualTo(4);

        assertThat(articleSearchDtos.getTotalElements())
                .as("전체 게시글 수")
                .isEqualTo(10L);

        List<ArticleSearchDto> content = articleSearchDtos.getContent();

        assertThat(content)
                .extracting("article")
                .as("페이지 내 게시글")
                .containsExactly(article10, article9, article8);

        assertThat(content)
                .extracting("article")
                .extracting("author")
                .as("게시글 작성자 페치 조인")
                .allMatch(persistence::isLoaded);

        assertThat(content)
                .extracting("article")
                .extracting("author")
                .as("게시글 작성자")
                .containsExactly(author1, author3, author2);

        assertThat(content)
                .extracting("article")
                .extracting("comments")
                .as("댓글 로딩 지연")
                .noneMatch(persistence::isLoaded);

        assertThat(content)
                .extracting("numComments")
                .as("댓글 수")
                .containsExactly(4L, 3L, 2L);
    }

    @Test
    @DisplayName("검색 조건: 작성자 - 빈 조건 2")
    void search_cond_author_empty_2() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");
        User author3 = createAndSaveUser("author3", "author3@board.com", "");

        Article article1 = Article.create("    aaaa aa  aa aa   ", "bbBbbBBbbb", author1);
        Article article2 = Article.create(" CccCccCccc   ", "aAaAaAaaAa", author2);
        Article article3 = Article.create("bbb bbb bbbb  ", "ccc ccc cccc ", author3);
        Article article4 = Article.create("ababab abab", "cacaca caca", author1);
        Article article5 = Article.create("  bcbcBCbcbc", "ababa babab", author2);
        Article article6 = Article.create("abc abcaBca", "bc abcabcab", author3);
        Article article7 = Article.create("aa bcca bbca", "abcc abbcaa", author1);
        Article article8 = Article.create(" cbacbacbac", "baba bAbaba", author2);
        Article article9 = Article.create("  a aaab cabbc   ", "  baacaabaca  ", author3);
        Article article10 = Article.create("aAabbbccca", "aaAabbBbcc", author1);


        List<Article> articles = List.of(
                article1, article2, article3, article4, article5,
                article6, article7, article8, article9, article10
        );

        articles.forEach(em::persist);

        List<Comment> comments = generateComments(articles, author1, 0, 1, 2, 3, 4);
        comments.forEach(em::persist);

        PageRequest pageable = PageRequest.of(0, 3);

        final ArticleSearchCond cond = ArticleSearchCond.create(" ", AUTHOR);

        em.flush();
        em.clear();

        //when
        final Page<ArticleSearchDto> articleSearchDtos = articleRepository.search(cond, pageable);

        //then
        assertThat(articleSearchDtos.getTotalPages())
                .as("전체 페이지 수")
                .isEqualTo(4);

        assertThat(articleSearchDtos.getTotalElements())
                .as("전체 게시글 수")
                .isEqualTo(10L);

        List<ArticleSearchDto> content = articleSearchDtos.getContent();

        assertThat(content)
                .extracting("article")
                .as("페이지 내 게시글")
                .containsExactly(article10, article9, article8);

        assertThat(content)
                .extracting("article")
                .extracting("author")
                .as("게시글 작성자 페치 조인")
                .allMatch(persistence::isLoaded);

        assertThat(content)
                .extracting("article")
                .extracting("author")
                .as("게시글 작성자")
                .containsExactly(author1, author3, author2);

        assertThat(content)
                .extracting("article")
                .extracting("comments")
                .as("댓글 로딩 지연")
                .noneMatch(persistence::isLoaded);

        assertThat(content)
                .extracting("numComments")
                .as("댓글 수")
                .containsExactly(4L, 3L, 2L);
    }

    @Test
    @DisplayName("검색 조건: 작성자 1")
    void search_cond_author_1() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");
        User author3 = createAndSaveUser("author3", "author3@board.com", "");

        Article article1 = Article.create("    aaaa aa  aa aa   ", "bbBbbBBbbb", author1);
        Article article2 = Article.create(" CccCccCccc   ", "aAaAaAaaAa", author2);
        Article article3 = Article.create("bbb bbb bbbb  ", "ccc ccc cccc ", author3);
        Article article4 = Article.create("ababab abab", "cacaca caca", author1);
        Article article5 = Article.create("  bcbcBCbcbc", "ababa babab", author2);
        Article article6 = Article.create("abc abcaBca", "bc abcabcab", author3);
        Article article7 = Article.create("aa bcca bbca", "abcc abbcaa", author1);
        Article article8 = Article.create(" cbacbacbac", "baba bAbaba", author2);
        Article article9 = Article.create("  a aaab cabbc   ", "  baacaabaca  ", author3);
        Article article10 = Article.create("aAabbbccca", "aaAabbBbcc", author1);


        List<Article> articles = List.of(
                article1, article2, article3, article4, article5,
                article6, article7, article8, article9, article10
        );

        articles.forEach(em::persist);

        List<Comment> comments = generateComments(articles, author1, 0, 1, 2, 3, 4);
        comments.forEach(em::persist);

        PageRequest pageable = PageRequest.of(0, 3);

        final ArticleSearchCond cond = ArticleSearchCond.create("author1", AUTHOR);

        em.flush();
        em.clear();

        //when
        final Page<ArticleSearchDto> articleSearchDtos = articleRepository.search(cond, pageable);

        //then
        assertThat(articleSearchDtos.getTotalPages())
                .as("전체 페이지 수")
                .isEqualTo(2);

        assertThat(articleSearchDtos.getTotalElements())
                .as("전체 게시글 수")
                .isEqualTo(4L);

        List<ArticleSearchDto> content = articleSearchDtos.getContent();

        assertThat(content)
                .extracting("article")
                .as("페이지 내 게시글")
                .containsExactly(article10, article7, article4);

        assertThat(content)
                .extracting("article")
                .extracting("author")
                .as("게시글 작성자 페치 조인")
                .allMatch(persistence::isLoaded);

        assertThat(content)
                .extracting("article")
                .extracting("author")
                .as("게시글 작성자")
                .containsExactly(author1, author1, author1);

        assertThat(content)
                .extracting("article")
                .extracting("comments")
                .as("댓글 로딩 지연")
                .noneMatch(persistence::isLoaded);

        assertThat(content)
                .extracting("numComments")
                .as("댓글 수")
                .containsExactly(4L, 1L, 3L);
    }

    @Test
    @DisplayName("검색 조건: 작성자 2")
    void search_cond_author_2() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");
        User author3 = createAndSaveUser("author3", "author3@board.com", "");

        Article article1 = Article.create("    aaaa aa  aa aa   ", "bbBbbBBbbb", author1);
        Article article2 = Article.create(" CccCccCccc   ", "aAaAaAaaAa", author2);
        Article article3 = Article.create("bbb bbb bbbb  ", "ccc ccc cccc ", author3);
        Article article4 = Article.create("ababab abab", "cacaca caca", author1);
        Article article5 = Article.create("  bcbcBCbcbc", "ababa babab", author2);
        Article article6 = Article.create("abc abcaBca", "bc abcabcab", author3);
        Article article7 = Article.create("aa bcca bbca", "abcc abbcaa", author1);
        Article article8 = Article.create(" cbacbacbac", "baba bAbaba", author2);
        Article article9 = Article.create("  a aaab cabbc   ", "  baacaabaca  ", author3);
        Article article10 = Article.create("aAabbbccca", "aaAabbBbcc", author1);

        List<Article> articles = List.of(
                article1, article2, article3, article4, article5,
                article6, article7, article8, article9, article10
        );

        articles.forEach(em::persist);

        List<Comment> comments = generateComments(articles, author1, 0, 1, 2, 3, 4);
        comments.forEach(em::persist);

        PageRequest pageable = PageRequest.of(0, 3);

        final ArticleSearchCond cond = ArticleSearchCond.create("XXXXXXXXXX", AUTHOR);

        em.flush();
        em.clear();

        //when
        final Page<ArticleSearchDto> articleSearchDtos = articleRepository.search(cond, pageable);

        //then
        assertThat(articleSearchDtos.getTotalPages())
                .as("전체 페이지 수")
                .isEqualTo(0);

        assertThat(articleSearchDtos.getTotalElements())
                .as("전체 게시글 수")
                .isEqualTo(0L);

        assertThat(articleSearchDtos.isEmpty())
                .as("결과 없음")
                .isTrue();
    }
}