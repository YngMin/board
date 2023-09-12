package hello.board.repository;

import hello.board.domain.Article;
import hello.board.domain.Comment;
import hello.board.domain.User;
import hello.board.dto.service.ArticleCommentFlatDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ArticleRepositoryTest {

    @Autowired
    ArticleRepository articleRepository;

    @Autowired
    EntityManager em;

    PersistenceUnitUtil persistence;

    @BeforeEach
    void beforeEach() {
        persistence = em.getEntityManagerFactory().getPersistenceUnitUtil();
    }
    @AfterEach
    void afterEach() {
        em.clear();
    }

    private User createAndSaveUser(String name, String email, String password) {
        User user = User.create(name, email, password);
        em.persist(user);
        return user;
    }


    @Test
    @DisplayName("findById 성공")
    void findById() {
        //given
        User author = createAndSaveUser("author", "author@board.com", "");

        Article article = Article.create("title", "content", author);
        em.persist(article);

        final Long id = article.getId();

        em.flush();
        em.clear();

        //when
        Article findArticle = articleRepository.findById(id).orElseThrow();

        //then
        //article
        assertThat(findArticle)
                .as("게시글")
                .isEqualTo(article);

        assertThat(findArticle.getView())
                .as("조회수")
                .isEqualTo(0L);

        //article.author
        User findArticleAuthor = findArticle.getAuthor();
        assertThat(persistence.isLoaded(findArticleAuthor))
                .as("작성자 페치 조인 성공")
                .isTrue();

        assertThat(findArticleAuthor)
                .as("작성자")
                .isEqualTo(author);

        assertThat(persistence.isLoaded(findArticle.getComments()))
                .as("댓글 로딩 지연")
                .isFalse();
    }

    @Test
    @DisplayName("findById 실패")
    void findById_fail() {
        //given
        User author = createAndSaveUser("author", "author@board.com", "");

        Article article = Article.create("title", "content", author);
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
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");

        Article article = Article.create("title", "content", author1);
        em.persist(article);
        final Long id = article.getId();

        Comment comment1 = Comment.create("comment1", article, author1);
        Comment comment2 = Comment.create("comment2", article, author2);

        em.persist(comment1);
        em.persist(comment2);

        em.flush();
        em.clear();

        //when
        Article findArticle = articleRepository.findWithComments(id).orElseThrow();

        //then
        //article
        assertThat(findArticle)
                .as("게시글")
                .isEqualTo(article);

        assertThat(findArticle.getView())
                .as("조회수")
                .isEqualTo(0L);

        //article.author
        User findArticleAuthor = article.getAuthor();
        assertThat(persistence.isLoaded(findArticleAuthor))
                .as("작성자 페치 조인 성공")
                .isTrue();

        assertThat(findArticleAuthor)
                .as("작성자")
                .isEqualTo(author1);

        //article.comments
        List<Comment> findComments = findArticle.getComments();

        assertThat(persistence.isLoaded(findComments))
                .as("댓글 페치 조인 성공")
                .isTrue();

        assertThat(findComments)
                .as("댓글")
                .containsExactly(comment1, comment2);

        //article.comments[*].author
        assertThat(findComments)
                .extracting("author")
                .as("댓글 작성자 페치 조인 성공")
                .allMatch(persistence::isLoaded);

        assertThat(findComments)
                .extracting("author")
                .as("댓글 작성자")
                .containsExactly(author1, author2);
    }

    @Test
    @DisplayName("댓글과 함께 조회 실패")
    void findWithComments_noPaging_fail() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");

        Article article = Article.create("title", "content", author1);
        em.persist(article);

        Comment comment1 = Comment.create("comment1", article, author1);
        Comment comment2 = Comment.create("comment2", article, author2);

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
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");
        User author3 = createAndSaveUser("author3", "author3@board.com", "");

        Article article = Article.create("title", "content", author1);
        em.persist(article);
        final Long id = article.getId();

        final int NUMBER_OF_COMMENTS = 20;
        final int PAGE = 1, SIZE = 5;
        final Pageable pageable = PageRequest.of(PAGE, SIZE);

        List<Comment> comments = generateComments(article, NUMBER_OF_COMMENTS, author1, author2, author3);
        comments.forEach(em::persist);

        em.flush();
        em.clear();

        //when
        Page<ArticleCommentFlatDto> flatDtos = articleRepository.findWithComments(id, pageable);

        // then
        final List<ArticleCommentFlatDto> content = flatDtos.getContent();

        //article
        assertThat(content)
                .extracting("article")
                .as("동일한 게시글")
                .containsOnly(article);

        Article findArticle = content.stream()
                .map(ArticleCommentFlatDto::getArticle)
                .findAny()
                .orElseThrow();

        assertThat(findArticle.getView())
                .as("조회수")
                .isEqualTo(0L);

        //article.author
        User findArticleAuthor = findArticle.getAuthor();
        assertThat(persistence.isLoaded(findArticleAuthor))
                .as("게시글 작성자 페치 조인")
                .isTrue();

        assertThat(findArticleAuthor)
                .as("게시글 작성자")
                .isEqualTo(author1);

        //article.comments
        assertThat(content)
                .extracting("comment")
                .as("댓글 작성자")
                .containsExactlyElementsOf(comments.subList(5, 10));

        //article.comments[*].author
        assertThat(content)
                .extracting("comment")
                .extracting("author")
                .as("댓글 작성자 페치 조인")
                .allMatch(persistence::isLoaded);

        assertThat(content)
                .extracting("comment")
                .extracting("author")
                .as("댓글 작성자")
                .containsExactly(author3, author1, author2, author3, author1);
    }

    private static List<Comment> generateComments(Article article, int numOfComments, User... authors) {
        List<Comment> comments = new ArrayList<>();
        for (int i = 0; i < numOfComments; i++) {
            Comment comment = Comment.create(
                    "content" + i,
                    article,
                    authors[i % authors.length]
            );
            comments.add(comment);
        }
        return comments;
    }

    @Test
    @DisplayName("페이징된 댓글과 함께 조회 실패")
    void findWithComments_paging_fail() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");
        User author3 = createAndSaveUser("author3", "author3@board.com", "");

        Article article = Article.create("title", "content", author1);
        em.persist(article);

        final Long WRONG_ID = 666L;
        final int NUMBER_OF_COMMENTS = 20;
        final int PAGE = 2, SIZE = 5;
        final Pageable pageable = PageRequest.of(PAGE, SIZE);

        generateComments(article, NUMBER_OF_COMMENTS, author1, author2, author3);

        em.flush();
        em.clear();

        //when
        Page<ArticleCommentFlatDto> flatDtos = articleRepository.findWithComments(WRONG_ID, pageable);

        // then
        assertThat(flatDtos.isEmpty())
                .as("존재하지 않는 게시글")
                .isTrue();
    }

}