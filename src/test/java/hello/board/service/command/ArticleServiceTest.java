package hello.board.service.command;

import hello.board.domain.Article;
import hello.board.domain.Comment;
import hello.board.domain.User;
import hello.board.dto.service.ArticleServiceDto.LookUp;
import hello.board.dto.service.ArticleServiceDto.Save;
import hello.board.exception.FailToFindEntityException;
import hello.board.exception.NoAuthorityException;
import hello.board.exception.WrongPageRequestException;
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
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static hello.board.dto.service.ArticleServiceDto.Update;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class ArticleServiceTest {

    @Autowired
    ArticleService articleService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ArticleRepository articleRepository;

    @Autowired
    CommentRepository commentRepository;

    @TestConfiguration
    static class Config {

        @Bean
        ArticleService articleService(ArticleRepository articleRepository, UserRepository userRepository) {
            return new ArticleService(articleRepository, userRepository);
        }
    }


    private User createAndSaveUser(String name, String email, String password) {
        User user = User.create(name, email, password);
        userRepository.save(user);
        return user;
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
    @DisplayName("저장 성공")
    void save() {
        //given
        User author = createAndSaveUser("author", "author@board.com", "");

        //when
        final Long id = articleService.save(author.getId(), Save.create("title", "content"));

        //then
        Article findArticle = articleRepository.findById(id).orElseThrow();

        assertThat(findArticle.getTitle())
                .as("제목")
                .isEqualTo("title");

        assertThat(findArticle.getContent())
                .as("내용")
                .isEqualTo("content");

        assertThat(findArticle.getView())
                .as("조회수")
                .isEqualTo(0L);
    }

    @Test
    @DisplayName("저장 실패")
    void save_fail() {
        //given
        createAndSaveUser("author", "author@board.com", "");
        final Long WRONG_ID = 4444L;

        //when & then
        assertThatThrownBy(() -> articleService.save(WRONG_ID, Save.create("title", "content")))
                .as("존재하지 않는 사용자 ID")
                .isInstanceOf(FailToFindEntityException.class);
    }

    @Test
    @DisplayName("모든 댓글과 함께 조회 성공")
    void lookUp() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");

        Article article = Article.create("title", "content", author1);
        articleRepository.save(article);

        final Long id = article.getId();

        List<Comment> comments = generateComments(article, 10, author1, author2);
        commentRepository.saveAll(comments);

        //when
        Article findArticle = articleService.lookUp(id);

        List<Comment> findComments = findArticle.getComments();

        //then
        //article
        assertThat(findArticle)
                .as("게시글")
                .isEqualTo(article);

        assertThat(findArticle.getView())
                .as("조회수 증가")
                .isEqualTo(1L);

        //article.author
        User findArticleAuthor = findArticle.getAuthor();
        assertThat(findArticleAuthor)
                .as("작성자")
                .isEqualTo(author1);

        //article.comments
        assertThat(findComments.size())
                .as("댓글 수")
                .isEqualTo(10);

        assertThat(findComments)
                .as("댓글")
                .containsExactlyElementsOf(comments);

        //comment.article
        assertThat(findComments)
                .extracting("article")
                .as("연관 관계 세팅 확인")
                .containsOnly(article);
    }

    @Test
    @DisplayName("모든 댓글과 함께 조회 실패")
    void lookUp_fail() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");

        Article article = Article.create("title", "content", author1);
        articleRepository.save(article);

        final Long WRONG_ID = 4444L;

        //when & then
        assertThatThrownBy(() -> articleService.lookUp(WRONG_ID))
                .as("게시글 조회 실패 시 예외")
                .isInstanceOf(FailToFindEntityException.class);
    }

    @Test
    @DisplayName("페이징된 댓글과 함께 조회 성공 - 첫 페이지")
    void lookUpPaging_firstPage() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");
        User author3 = createAndSaveUser("author3", "author3@board.com", "");

        Article article = Article.create("title", "content", author1);
        articleRepository.save(article);

        final Long id = article.getId();

        final int NUMBER_OF_COMMENTS = 20;
        final int PAGE = 0, SIZE = 5;

        final Pageable pageable = PageRequest.of(PAGE, SIZE);

        List<Comment> comments = generateComments(article, NUMBER_OF_COMMENTS, author1, author2, author3);
        commentRepository.saveAll(comments);

        //when
        LookUp lookUp = articleService.lookUp(id, pageable);

        // then
        //article
        Article findArticle = lookUp.getArticle();

        assertThat(findArticle)
                .as("게시글")
                .isEqualTo(article);

        assertThat(findArticle.getView())
                .as("조회수 증가")
                .isEqualTo(1L);

        //article.author
        User findArticleAuthor = findArticle.getAuthor();

        assertThat(findArticleAuthor)
                .as("게시글 작성자")
                .isEqualTo(author1);

        //article.comments
        Page<Comment> findComments = lookUp.getComments();
        List<Comment> content = findComments.getContent();

        assertThat(content)
                .as("페이지 내 댓글")
                .containsExactlyElementsOf(comments.subList(0, 5));

        //article.comments[*].author
        assertThat(content)
                .extracting("author")
                .as("댓글 작성자")
                .containsExactly(author1, author2, author3, author1, author2);

    }

    @Test
    @DisplayName("페이징된 댓글과 함께 조회 성공 - 중간 페이지")
    void lookUpPaging_middlePage() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");
        User author3 = createAndSaveUser("author3", "author3@board.com", "");

        Article article = Article.create("title", "content", author1);
        articleRepository.save(article);

        final Long id = article.getId();

        final int NUMBER_OF_COMMENTS = 20;
        final int PAGE = 1, SIZE = 7;

        final Pageable pageable = PageRequest.of(PAGE, SIZE);

        List<Comment> comments = generateComments(article, NUMBER_OF_COMMENTS, author1, author2, author3);
        commentRepository.saveAll(comments);

        //when
        LookUp lookUp = articleService.lookUp(id, pageable);

        // then
        //article
        Article findArticle = lookUp.getArticle();

        assertThat(findArticle)
                .as("게시글")
                .isEqualTo(article);

        assertThat(findArticle.getView())
                .as("조회수 증가")
                .isEqualTo(1L);

        //article.author
        User findArticleAuthor = findArticle.getAuthor();

        assertThat(findArticleAuthor)
                .as("게시글 작성자")
                .isEqualTo(author1);

        //article.comments
        Page<Comment> findComments = lookUp.getComments();
        List<Comment> content = findComments.getContent();

        assertThat(content)
                .as("페이지 내 댓글")
                .containsExactlyElementsOf(comments.subList(7, 14));

        //article.comments[*].author
        assertThat(content)
                .extracting("author")
                .as("댓글 작성자")
                .containsExactly(author2, author3, author1, author2, author3, author1, author2);

    }

    @Test
    @DisplayName("페이징된 댓글과 함께 조회 성공 - 마지막 페이지")
    void lookUpPaging_lastPage() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");
        User author3 = createAndSaveUser("author3", "author3@board.com", "");

        Article article = Article.create("title", "content", author1);
        articleRepository.save(article);

        final Long id = article.getId();

        final int NUMBER_OF_COMMENTS = 20;
        final int PAGE = 2, SIZE = 8;

        final Pageable pageable = PageRequest.of(PAGE, SIZE);

        List<Comment> comments = generateComments(article, NUMBER_OF_COMMENTS, author1, author2, author3);
        commentRepository.saveAll(comments);

        //when
        LookUp lookUp = articleService.lookUp(id, pageable);

        // then
        //article
        Article findArticle = lookUp.getArticle();

        assertThat(findArticle)
                .as("게시글")
                .isEqualTo(article);

        assertThat(findArticle.getView())
                .as("조회수 증가")
                .isEqualTo(1L);

        //article.author
        User findArticleAuthor = findArticle.getAuthor();

        assertThat(findArticleAuthor)
                .as("게시글 작성자")
                .isEqualTo(author1);

        //article.comments
        Page<Comment> findComments = lookUp.getComments();
        List<Comment> content = findComments.getContent();

        assertThat(content)
                .as("페이지 내 댓글")
                .containsExactlyElementsOf(comments.subList(16, 20));

        //article.comments[*].author
        assertThat(content)
                .extracting("author")
                .as("댓글 작성자")
                .containsExactly(author2, author3, author1, author2);

    }

    @Test
    @DisplayName("페이징된 댓글과 함께 조회 실패 - wrong article id")
    void lookUpPaging_fail_articleId() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");
        User author3 = createAndSaveUser("author3", "author3@board.com", "");

        Article article = Article.create("title", "content", author1);
        articleRepository.save(article);

        final Long WRONG_ID = 666L;

        final int NUMBER_OF_COMMENTS = 20;
        final int PAGE = 0, SIZE = 5;

        final Pageable pageable = PageRequest.of(PAGE, SIZE);

        generateComments(article, NUMBER_OF_COMMENTS, author1, author2, author3);

        //when & then
        assertThatThrownBy(() -> articleService.lookUp(WRONG_ID, pageable))
                .as("존재하지 않는 게시글")
                .isInstanceOf(FailToFindEntityException.class);
    }

    @Test
    @DisplayName("페이징된 댓글과 함께 조회 실패 - wrong page number")
    void lookUpPaging_fail_pageRequest() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");
        User author3 = createAndSaveUser("author3", "author3@board.com", "");

        Article article = Article.create("title", "content", author1);
        articleRepository.save(article);

        final Long id = article.getId();

        final int NUMBER_OF_COMMENTS = 20;
        final int PAGE = 100, SIZE = 10;

        final Pageable pageable = PageRequest.of(PAGE, SIZE);

        generateComments(article, NUMBER_OF_COMMENTS, author1, author2, author3);

        //when & then
        assertThatThrownBy(() -> articleService.lookUp(id, pageable))
                .as("존재하지 않는 페이지")
                .isInstanceOf(WrongPageRequestException.class);
    }

    @Test
    @DisplayName("수정 성공 - 제목 및 내용 수정")
    void update_both() {
        //given
        User author = createAndSaveUser("author", "author@board.com", "");
        final Long userId = author.getId();

        Article article = Article.create("title", "content", author);
        articleRepository.save(article);

        final Long id = article.getId();

        //when
        articleService.update(id, userId, Update.create("titleUpdate", "contentUpdate"));

        //then
        Article updatedArticle = articleRepository.findById(id).orElseThrow();

        assertThat(updatedArticle.getTitle())
                .as("제목 수정")
                .isEqualTo("titleUpdate");

        assertThat(updatedArticle.getContent())
                .as("내용 수정")
                .isEqualTo("contentUpdate");

        assertThat(updatedArticle.getView())
                .as("조회수 변화 없음")
                .isEqualTo(0L);
    }

    @Test
    @DisplayName("수정 성공 - 제목 수정")
    void update_title() {
        //given
        User author = createAndSaveUser("author", "author@board.com", "");
        final Long userId = author.getId();

        Article article = Article.create("title", "content", author);
        articleRepository.save(article);

        final Long id = article.getId();

        //when
        articleService.update(id, userId, Update.create("titleUpdate", null));

        //then
        Article updatedArticle = articleRepository.findById(id).orElseThrow();

        assertThat(updatedArticle.getTitle())
                .as("제목 수정")
                .isEqualTo("titleUpdate");

        assertThat(updatedArticle.getContent())
                .as("내용 변화 없음")
                .isEqualTo("content");

        assertThat(updatedArticle.getView())
                .as("조회수 변화 없음")
                .isEqualTo(0L);
    }

    @Test
    @DisplayName("수정 성공 - 내용 수정")
    void update_content() {
        //given
        User author = createAndSaveUser("author", "author@board.com", "");
        final Long userId = author.getId();

        Article article = Article.create("title", "content", author);
        articleRepository.save(article);

        final Long id = article.getId();

        //when
        articleService.update(id, userId, Update.create(null, "contentUpdate"));

        //then
        Article updatedArticle = articleRepository.findById(id).orElseThrow();

        assertThat(updatedArticle.getTitle())
                .as("제목 변화 없음")
                .isEqualTo("title");

        assertThat(updatedArticle.getContent())
                .as("내용 수정")
                .isEqualTo("contentUpdate");

        assertThat(updatedArticle.getView())
                .as("조회수 변화 없음")
                .isEqualTo(0L);
    }

    @Test
    @DisplayName("수정 성공 - updateDto null")
    void update_null() {
        //given
        User author = createAndSaveUser("author", "author@board.com", "");
        final Long userId = author.getId();

        Article article = Article.create("title", "content", author);
        articleRepository.save(article);

        final Long id = article.getId();

        //when
        articleService.update(id, userId, null);

        //then
        Article updatedArticle = articleRepository.findById(id).orElseThrow();

        assertThat(updatedArticle.getTitle())
                .as("제목 변화 없음")
                .isEqualTo("title");

        assertThat(updatedArticle.getContent())
                .as("내용 변화 없음")
                .isEqualTo("content");

        assertThat(updatedArticle.getView())
                .as("조회수 변화 없음")
                .isEqualTo(0L);
    }

    @Test
    @DisplayName("수정 실패 - 작성자가 아닌 사용자가 수정 시도")
    void update_fail_noAuthority() {
        //given
        User realAuthor = createAndSaveUser("author1", "author1@board.com", "");
        User otherUser = createAndSaveUser("author2", "author2@board.com", "");

        final Long otherUserId = otherUser.getId();

        Article article = Article.create("title", "content", realAuthor);
        articleRepository.save(article);

        final Long id = article.getId();

        //when & then
        assertThatThrownBy(() -> articleService.update(id, otherUserId, Update.create("", "")))
                .as("작성자가 아닌 사용자가 수정")
                .isInstanceOf(NoAuthorityException.class);
    }

    @Test
    @DisplayName("수정 실패 - 존재하지 않는 사용자 ID로 수정 시도")
    void update_fail_wrongUserId() {
        //given
        User author = createAndSaveUser("author", "author@board.com", "");

        final Long WRONG_USER_ID = 666L;

        Article article = Article.create("title", "content", author);
        articleRepository.save(article);

        final Long id = article.getId();


        //when & then
        assertThatThrownBy(() -> articleService.update(id, WRONG_USER_ID, Update.create("", "")))
                .as("존재하지 않는 사용자 ID로 수정")
                .isInstanceOf(NoAuthorityException.class);
    }


    @Test
    @DisplayName("수정 실패 - 존재하지 않는 게시글 수정 시도")
    void update_fail_noArticle() {
        //given
        User author = createAndSaveUser("author", "author@board.com", "");

        final Long authorId = author.getId();

        Article article = Article.create("title", "content", author);
        articleRepository.save(article);

        final Long WRONG_ID = 4444L;

        //when & then
        assertThatThrownBy(() -> articleService.update(WRONG_ID, authorId, Update.create("", "")))
                .as("존재하지 않는 게시글 수정")
                .isInstanceOf(FailToFindEntityException.class);
    }

    @Test
    @DisplayName("수정 실패 - 존재하지 않는 게시글을 존재하지 않는 사용자 ID로 수정 시도")
    void update_fail_noArticle_wrongUserId() {
        //given
        User author = createAndSaveUser("author", "author@board.com", "");

        final Long WRONG_AUTHOR_ID = 666L;

        Article article = Article.create("title", "content", author);
        articleRepository.save(article);

        final Long WRONG_ID = 4444L;

        //when & then
        assertThatThrownBy(() -> articleService.update(WRONG_ID, WRONG_AUTHOR_ID, Update.create("", "")))
                .as("존재하지 않는 게시글 수정 + 존재하지 않는 사용자 ID")
                .isInstanceOf(FailToFindEntityException.class);
    }

    @Test
    @DisplayName("삭제 성공")
    void delete() {
        //given
        User author = createAndSaveUser("author", "author@board.com", "");
        final Long userId = author.getId();

        Article article = Article.create("title", "content", author);
        articleRepository.save(article);

        final Long id = article.getId();

        List<Comment> comments = generateComments(article, 10, author);
        commentRepository.saveAll(comments);

        List<Long> commentIds = comments.stream()
                .map(Comment::getId)
                .toList();

        //when
        articleService.delete(id, userId);

        //then
        Optional<Article> articleOptional = articleRepository.findById(id);
        assertThat(articleOptional.isEmpty())
                .as("삭제된 게시글")
                .isTrue();

        List<Optional<Comment>> commentOptionals = commentIds.stream()
                .map(commentId -> commentRepository.findById(commentId))
                .toList();

        assertThat(commentOptionals)
                .as("게시글 삭제 시 댓글도 삭제")
                .allMatch(Optional::isEmpty);
    }


    @Test
    @DisplayName("삭제 실패 - 작성자가 아닌 사용자가 삭제 시도")
    void delete_fail_noAuthority() {
        //given
        User realAuthor = createAndSaveUser("author1", "author1@board.com", "");
        User otherUser = createAndSaveUser("author2", "author2@board.com", "");

        final Long otherUserId = otherUser.getId();

        Article article = Article.create("title", "content", realAuthor);

        articleRepository.save(article);

        final Long id = article.getId();

        //when & then
        assertThatThrownBy(() -> articleService.delete(id, otherUserId))
                .as("작성자가 아닌 사용자가 삭제")
                .isInstanceOf(NoAuthorityException.class);
    }

    @Test
    @DisplayName("삭제 실패 - 존재하지 않는 사용자 ID로 삭제 시도")
    void delete_fail_wrongUserId() {
        //given
        User author = createAndSaveUser("author", "author@board.com", "");

        final Long WRONG_USER_ID = 666L;

        Article article = Article.create("title", "content", author);
        articleRepository.save(article);

        final Long id = article.getId();

        //when & then
        assertThatThrownBy(() -> articleService.delete(id, WRONG_USER_ID))
                .as("존재하지 않는 사용자 ID로 삭제")
                .isInstanceOf(NoAuthorityException.class);
    }


    @Test
    @DisplayName("삭제 실패 - 존재하지 않는 게시글 삭제 시도")
    void delete_fail_noArticle() {
        //given
        User author = createAndSaveUser("author", "author@board.com", "");

        final Long authorId = author.getId();

        Article article = Article.create("title", "content", author);
        articleRepository.save(article);

        final Long WRONG_ID = 4444L;

        //when & then
        assertThatThrownBy(() -> articleService.delete(WRONG_ID, authorId))
                .as("존재하지 않는 게시글 삭제")
                .isInstanceOf(FailToFindEntityException.class);
    }

    @Test
    @DisplayName("삭제 실패 - 존재하지 않는 게시글을 존재하지 않는 사용자 ID로 삭제 시도")
    void delete_fail_noArticle_wrongUserId() {
        //given
        User author = createAndSaveUser("author", "author@board.com", "");

        final Long WRONG_AUTHOR_ID = 666L;

        Article article = Article.create("title", "content", author);
        articleRepository.save(article);

        final Long WRONG_ID = 4444L;

        //when & then
        assertThatThrownBy(() -> articleService.delete(WRONG_ID, WRONG_AUTHOR_ID))
                .as("존재하지 않는 게시글 삭제 + 존재하지 않는 사용자 ID")
                .isInstanceOf(FailToFindEntityException.class);
    }
}