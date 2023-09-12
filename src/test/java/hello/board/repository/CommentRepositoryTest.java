package hello.board.repository;

import hello.board.domain.Article;
import hello.board.domain.Comment;
import hello.board.domain.User;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CommentRepositoryTest {

    @Autowired
    CommentRepository commentRepository;

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

    private Article createAndSaveArticle(String title, String content, User author) {
        Article article = Article.create(title, content, author);
        em.persist(article);
        return article;
    }

    @Test
    @DisplayName("findById 성공")
    void findById() {
        //given
        User articleAuthor = createAndSaveUser("articleAuthor", "article@board.com", "");
        User commentAuthor = createAndSaveUser("commentAuthor", "comment@board.com", "");
        Article article = createAndSaveArticle("title", "content", articleAuthor);

        Comment comment = Comment.create("content", article, commentAuthor);
        em.persist(comment);

        final Long id = comment.getId();

        em.flush();
        em.clear();

        //when
        Comment findComment = commentRepository.findById(id).orElseThrow();

        //then
        assertThat(findComment)
                .as("댓글")
                .isEqualTo(comment);

        User findCommentAuthor = findComment.getAuthor();
        assertThat(persistence.isLoaded(findCommentAuthor))
                .as("댓글 작성자 페치 조인")
                .isTrue();

        assertThat(findCommentAuthor)
                .isEqualTo(commentAuthor);

        assertThat(persistence.isLoaded(findComment.getArticle()))
                .as("게시글 로딩 지연")
                .isFalse();
    }

    @Test
    @DisplayName("findById 실패")
    void findById_fail() {
        //given
        User articleAuthor = createAndSaveUser("articleAuthor", "article@board.com", "");
        User commentAuthor = createAndSaveUser("commentAuthor", "comment@board.com", "");
        Article article = createAndSaveArticle("title", "content", articleAuthor);

        Comment comment = Comment.create("content", article, commentAuthor);
        em.persist(comment);

        final Long WRONG_ID = 666L;

        em.flush();
        em.clear();

        //when & then
        Optional<Comment> commentOptional = commentRepository.findById(WRONG_ID);
        assertThat(commentOptional.isEmpty())
                .as("존재하지 않는 댓글")
                .isTrue();
    }

    @Test
    @DisplayName("findByArticleId 성공 - 댓글 없음")
    void findByArticleId_noComment() {
        //given
        User author = createAndSaveUser("author", "author@board.com", "");
        Article article = createAndSaveArticle("title", "content", author);

        final Long id = article.getId();

        em.flush();
        em.clear();

        //when
        Page<Comment> comments = commentRepository.findByArticleId(id, PageRequest.of(0, 5));

        //then
        assertThat(comments.isEmpty())
                .as("댓글 없음")
                .isTrue();

    }

    @Test
    @DisplayName("findByArticleId 성공 - 댓글 하나")
    void findByArticleId_oneComment() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");

        Article article = createAndSaveArticle("title2", "content2", author1);
        final Long articleId = article.getId();

        Comment comment = Comment.create("content", article, author2);
        em.persist(comment);

        em.flush();
        em.clear();

        //when
        Page<Comment> comments = commentRepository.findByArticleId(articleId, PageRequest.of(0, 5));

        //then
        assertThat(comments.getTotalElements())
                .isEqualTo(1L);

        Comment findComment = comments.getContent().get(0);

        assertThat(findComment)
                .as("페이지 내 댓글")
                .isEqualTo(comment);

        assertThat(persistence.isLoaded(findComment.getAuthor()))
                .as("댓글 작성자 페치 조인")
                .isTrue();

        assertThat(findComment.getAuthor())
                .as("댓글 작성자")
                .isEqualTo(author2);

        assertThat(persistence.isLoaded(findComment.getArticle()))
                .as("게시글 로딩 지연")
                .isFalse();
    }


    @Test
    @DisplayName("findByArticleId 성공 - 댓글 다수")
    void findByArticleId_lotsOfComments() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");
        User author3 = createAndSaveUser("author3", "author3@board.com", "");

        Article article = createAndSaveArticle("title", "content", author1);
        final Long articleId = article.getId();

        List<Comment> comments = generateComments(article, 20, author1, author2, author3);
        comments.forEach(em::persist);

        em.flush();
        em.clear();

        //when
        Page<Comment> findComments = commentRepository.findByArticleId(articleId, PageRequest.of(1, 5));

        //then
        assertThat(findComments.getTotalElements())
                .as("총 댓글 수")
                .isEqualTo(20);

        List<Comment> content = findComments.getContent();

        assertThat(content)
                .as("페이지 내 댓글")
                .containsExactlyElementsOf(comments.subList(5, 10));

        assertThat(content)
                .extracting("author")
                .as("댓글 작성자 페치 조인")
                .allMatch(persistence::isLoaded);

        assertThat(content)
                .extracting("author")
                .as("댓글 작성자")
                .containsExactly(author3, author1, author2, author3, author1);

        assertThat(content)
                .extracting("article")
                .as("게시글 로딩 지연")
                .noneMatch(persistence::isLoaded);
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
    @DisplayName("게시글과 함께 조회 성공")
    void findWithArticleById() {
        //given
        User articleAuthor = createAndSaveUser("articleAuthor", "article@board.com", "");
        User commentAuthor = createAndSaveUser("commentAuthor", "comment@board.com", "");
        Article article = createAndSaveArticle("title", "content", articleAuthor);

        Comment comment = Comment.create("content", article, commentAuthor);
        em.persist(comment);

        final Long id = comment.getId();

        em.flush();
        em.clear();

        //when
        Comment findComment = commentRepository.findWithArticleById(id).orElseThrow();

        //then
        assertThat(findComment)
                .as("댓글")
                .isEqualTo(comment);

        User findCommentAuthor = findComment.getAuthor();
        assertThat(persistence.isLoaded(findCommentAuthor))
                .as("댓글 작성자 페치 조인")
                .isTrue();

        assertThat(findCommentAuthor)
                .as("댓글 작성자")
                .isEqualTo(commentAuthor);

        Article findArticle = findComment.getArticle();
        assertThat(persistence.isLoaded(findArticle))
                .as("게시글 페치 조인")
                .isTrue();

        assertThat(findArticle)
                .as("게시글")
                .isEqualTo(article);

        assertThat(persistence.isLoaded(findArticle.getAuthor()))
                .as("게시글 작성자 로딩 지연")
                .isFalse();
    }

    @Test
    @DisplayName("게시글과 함께 조회 실패")
    void findWithArticleById_fail() {
        //given
        User articleAuthor = createAndSaveUser("articleAuthor", "article@board.com", "");
        User commentAuthor = createAndSaveUser("commentAuthor", "comment@board.com", "");
        Article article = createAndSaveArticle("title", "content", articleAuthor);

        Comment comment = Comment.create("content", article, commentAuthor);
        em.persist(comment);

        final Long WRONG_ID = 666L;

        em.flush();
        em.clear();

        //when
        Optional<Comment> commentOptional = commentRepository.findWithArticleById(WRONG_ID);

        //then
        assertThat(commentOptional.isEmpty())
                .as("존재하지 않는 댓글")
                .isTrue();
    }
}