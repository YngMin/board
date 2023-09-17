package hello.board.service.query;

import hello.board.domain.Article;
import hello.board.domain.Comment;
import hello.board.domain.User;
import hello.board.exception.FailToFindEntityException;
import hello.board.repository.ArticleRepository;
import hello.board.repository.CommentRepository;
import hello.board.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class CommentQueryServiceTest {

    @Autowired
    CommentQueryService commentQueryService;
    
    @Autowired
    UserRepository userRepository;
    
    @Autowired
    ArticleRepository articleRepository;
    
    @Autowired
    CommentRepository commentRepository;

    @TestConfiguration
    static class Config {

        @Bean
        CommentQueryService commentQueryService(ArticleRepository articleRepository, CommentRepository commentRepository) {
            return new CommentQueryService(articleRepository, commentRepository);
        }
    }
    
    private User createAndSaveUser(String name, String email, String password) {
        User user = User.create(name, email, password);
        userRepository.save(user);
        return user;
    }

    private Article createAndSaveArticle(String title, String content, User user) {
        Article article = Article.create(title, content, user);
        articleRepository.save(article);
        return article;
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
    private static List<Comment> generateComments(List<Article> articles, List<User> authors, int... numOfCommentsSeq) {
        List<Comment> comments = new ArrayList<>();
        int cnt = 0, idx = 0;
        int authorIdx = 0;
        for (Article article : articles) {
            for (int i = 0; i < numOfCommentsSeq[idx]; i++) {
                Comment comment = Comment.create(
                        "content" + cnt++,
                        article,
                        authors.get(authorIdx)
                );
                comments.add(comment);
                authorIdx = (authorIdx+1) % authors.size();
            }
            idx = (idx+1) % numOfCommentsSeq.length;
        }
        return comments;
    }

    @Test
    @DisplayName("findById 성공")
    void findById() {
        //given
        User author = createAndSaveUser("author", "author@board.com", "");
        Article article = createAndSaveArticle("title", "content", author);

        Comment comment = Comment.create("comment", article, author);
        commentRepository.save(comment);
        final Long id = comment.getId();

        //when
        Comment findComment = commentQueryService.findById(id);

        //then
        assertThat(findComment)
                .as("댓글")
                .isEqualTo(comment);

        assertThat(findComment.getAuthor())
                .as("작성자")
                .isEqualTo(author);
    }

    @Test
    @DisplayName("findById 실패")
    void findById_fail() {
        //given
        User author = createAndSaveUser("author", "author@board.com", "");
        Article article = createAndSaveArticle("title", "content", author);

        Comment comment = Comment.create("comment", article, author);
        commentRepository.save(comment);

        final Long WRONG_ID = 4444L;

        //when & then
        assertThatThrownBy(() -> commentQueryService.findById(WRONG_ID))
                .as("존재하지 않는 댓글")
                .isInstanceOf(FailToFindEntityException.class);
    }

    @Test
    @DisplayName("게시글과 함께 조회 성공")
    void findWithArticle() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");

        Article article = createAndSaveArticle("title", "content", author1);
        final Long articleId = article.getId();

        Comment comment = Comment.create("comment", article, author2);
        commentRepository.save(comment);
        final Long id = comment.getId();

        //when
        Comment findComment = commentQueryService.findWithArticle(id, articleId);

        //then
        assertThat(findComment)
                .as("댓글")
                .isEqualTo(comment);

        assertThat(findComment.getAuthor())
                .as("댓글 작성자")
                .isEqualTo(author2);

        assertThat(findComment.getArticle())
                .as("게시글")
                .isEqualTo(article);
    }

    @Test
    @DisplayName("게시글과 함께 조회 실패 - 존재하지 않는 댓글 ID로 조회 시도")
    void findWithArticle_fail_wrongCommentId() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");

        Article article = createAndSaveArticle("title", "content", author1);
        final Long articleId = article.getId();

        Comment comment = Comment.create("comment", article, author2);
        commentRepository.save(comment);

        final Long WRONG_ID = 4444L;

        //when & then
        assertThatThrownBy(() -> commentQueryService.findWithArticle(WRONG_ID, articleId))
                .as("존재하지 않는 댓글 ID")
                .isInstanceOf(FailToFindEntityException.class);
    }

    @Test
    @DisplayName("게시글과 함께 조회 실패 - 다른 게시글 ID로 조회 시도")
    void findWithArticle_fail_otherArticleId() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");

        Article article = createAndSaveArticle("title", "content", author1);
        Article other = createAndSaveArticle("other", "ohter", author2);

        final Long otherId = other.getId();

        Comment comment = Comment.create("comment", article, author2);
        commentRepository.save(comment);

        final Long id = comment.getId();

        //when & then
        assertThatThrownBy(() -> commentQueryService.findWithArticle(id, otherId))
                .as("다른 게시글 ID")
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("게시글과 함께 조회 실패 - 존재하지 않는 게시글 ID로 조회 시도")
    void findWithArticle_fail_wrongArticleId() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");

        Article article = createAndSaveArticle("title", "content", author1);

        Comment comment = Comment.create("comment", article, author2);
        commentRepository.save(comment);

        final Long id = comment.getId();

        final Long WRONG_ARTICLE_ID = 4444L;

        //when & then
        assertThatThrownBy(() -> commentQueryService.findWithArticle(id, WRONG_ARTICLE_ID))
                .as("존재하지 않는 게시글 ID")
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("게시글과 함께 조회 실패")
    void findWithArticle_fail() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");

        Article article = createAndSaveArticle("title", "content", author1);
        Article other = createAndSaveArticle("other", "other", author2);

        final Long otherId = other.getId();

        Comment comment = Comment.create("comment", article, author2);
        commentRepository.save(comment);

        final Long WRONG_ID = 4444L;

        //when & then
        assertThatThrownBy(() -> commentQueryService.findWithArticle(WRONG_ID, otherId))
                .as("존재하지 않는 댓글 ID & 다른 게시글 ID")
                .isInstanceOf(FailToFindEntityException.class);
    }

    @Test
    @DisplayName("게시글 ID로 조회 성공 - 댓글 없음")
    void findByArticleId_NoComment() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");
        User author3 = createAndSaveUser("author3", "author3@board.com", "");

        List<Article> articles = generateArticles(25, author1, author2, author3);
        articleRepository.saveAll(articles);

        List<Comment> comments = generateComments(
                articles,
                List.of(author1, author2, author3),
                4, 3, 2, 1, 0
        );
        commentRepository.saveAll(comments);

        final Long articleId = articles.get(4).getId();

        final PageRequest pageable = PageRequest.of(0, 5);

        //when
        Page<Comment> findComments = commentQueryService.findByArticleId(articleId, pageable);

        //then
        assertThat(findComments.getTotalElements())
                .as("총 댓글 수")
                .isEqualTo(0L);

        assertThat(findComments.getContent().isEmpty())
                .as("댓글 없음")
                .isTrue();
    }

    @Test
    @DisplayName("게시글 ID로 조회 성공 - 첫 페이지")
    void findByArticleId_firstPage() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");
        User author3 = createAndSaveUser("author3", "author3@board.com", "");

        List<Article> articles = generateArticles(20, author1, author2, author3);
        articleRepository.saveAll(articles);

        List<Comment> comments = generateComments(
                articles,
                List.of(author1, author2, author3),
                9, 4, 3, 0, 6, 2
        );
        commentRepository.saveAll(comments);

        final Long articleId = articles.get(0).getId();

        final PageRequest pageable = PageRequest.of(0, 5);

        //when
        Page<Comment> findComments = commentQueryService.findByArticleId(articleId, pageable);

        //then
        assertThat(findComments.getTotalElements())
                .as("총 댓글 수")
                .isEqualTo(9L);

        assertThat(findComments.getContent())
                 .as("댓글")
                .containsExactlyElementsOf(comments.subList(0, 5));

        assertThat(findComments.getContent())
                .extracting("author")
                .as("댓글 작성자")
                .containsExactly(author1, author2, author3, author1, author2);
    }

    @Test
    @DisplayName("게시글 ID로 조회 성공 - 중간 페이지")
    void findByArticleId_middlePage() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");
        User author3 = createAndSaveUser("author3", "author3@board.com", "");

        List<Article> articles = generateArticles(20, author1, author2, author3);
        articleRepository.saveAll(articles);

        List<Comment> comments = generateComments(
                articles,
                List.of(author1, author2, author3),
                12, 4, 11, 0, 3, 2, 6
        );
        commentRepository.saveAll(comments);

        final Long articleId = articles.get(2).getId();

        final PageRequest pageable = PageRequest.of(1, 5);

        //when
        Page<Comment> findComments = commentQueryService.findByArticleId(articleId, pageable);

        //then
        assertThat(findComments.getTotalElements())
                .as("총 댓글 수")
                .isEqualTo(11L);

        assertThat(findComments.getContent())
                .as("댓글")
                .containsExactlyElementsOf(comments.subList(21, 26));

        assertThat(findComments.getContent())
                .extracting("author")
                .as("댓글 작성자")
                .containsExactly(author1, author2, author3, author1, author2);
    }

    @Test
    @DisplayName("게시글 ID로 조회 성공 - 마지막 페이지")
    void findByArticleId_endPage() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");
        User author3 = createAndSaveUser("author3", "author3@board.com", "");

        List<Article> articles = generateArticles(20, author1, author2, author3);
        articleRepository.saveAll(articles);

        List<Comment> comments = generateComments(
                articles,
                List.of(author1, author2, author3),
                3, 11, 6, 23, 0, 4, 6, 1
        );
        commentRepository.saveAll(comments);

        final Long articleId = articles.get(3).getId();

        final PageRequest pageable = PageRequest.of(4, 5);

        //when
        Page<Comment> findComments = commentQueryService.findByArticleId(articleId, pageable);

        //then
        assertThat(findComments.getTotalElements())
                .as("총 댓글 수")
                .isEqualTo(23L);

        assertThat(findComments.getContent())
                .as("댓글")
                .containsExactlyElementsOf(comments.subList(40, 43));

        assertThat(findComments.getContent())
                .extracting("author")
                .as("댓글 작성자")
                .containsExactly(author2, author3, author1);
    }

    @Test
    @DisplayName("게시글 ID로 조회 성공 - 페이지 내 결과 없음")
    void findByArticleId_emptyPage() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");
        User author3 = createAndSaveUser("author3", "author3@board.com", "");

        List<Article> articles = generateArticles(20, author1, author2, author3);
        articleRepository.saveAll(articles);

        List<Comment> comments = generateComments(
                articles,
                List.of(author1, author2, author3),
                4, 3, 2, 1, 0
        );
        commentRepository.saveAll(comments);

        final Long articleId = articles.get(0).getId();

        final PageRequest pageable = PageRequest.of(1, 5);

        //when
        Page<Comment> findComments = commentQueryService.findByArticleId(articleId, pageable);

        //then
        assertThat(findComments.getTotalElements())
                .as("총 댓글 수")
                .isEqualTo(4L);

        assertThat(findComments.getContent().isEmpty())
                .as("페이지 내 댓글 없음")
                .isTrue();
    }

    @Test
    @DisplayName("게시글 ID로 조회 실패")
    void findByArticleId_fail() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");
        User author3 = createAndSaveUser("author3", "author3@board.com", "");

        List<Article> articles = generateArticles(20, author1, author2, author3);
        articleRepository.saveAll(articles);

        List<Comment> comments = generateComments(
                articles,
                List.of(author1, author2, author3),
                1, 2, 3, 4, 5
        );
        commentRepository.saveAll(comments);

        final Long WRONG_ARTICLE_ID = 666L;

        final PageRequest pageable = PageRequest.of(0, 5);

        //when & then
        assertThatThrownBy(() -> commentQueryService.findByArticleId(WRONG_ARTICLE_ID, pageable))
                .as("존재하지 않는 게시글 ID")
                .isInstanceOf(IllegalArgumentException.class);
    }
}