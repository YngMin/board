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
import hello.board.repository.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.proxy.HibernateProxy;
import org.junit.jupiter.api.AfterEach;
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
import java.util.function.Predicate;

import static hello.board.dto.service.ArticleServiceDto.Update;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
@DataJpaTest
class ArticleServiceTest {

    @Autowired
    ArticleService articleService;

    @Autowired
    EntityManager em;

    @TestConfiguration
    @RequiredArgsConstructor
    static class Config {

        @Bean
        ArticleService articleService(ArticleRepository articleRepository, UserRepository userRepository) {
            return new ArticleService(articleRepository, userRepository);
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

    private static Predicate<Object> isNotProxy() {
        return a -> !(a instanceof HibernateProxy);
    }

    @Test
    @DisplayName("저장 성공")
    void save() {
        //given
        User user = createUserAndPersist("user", "test@gmail.com", "1234");

        //when
        final Long id = articleService.save(user.getId(), Save.create("title", "content"));

        em.flush();
        em.clear();

        //then
        //article
        Article findArticle = em.find(Article.class, id);

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
        createUserAndPersist("user", "test@gmail.com", "1234");
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
        User user1 = createUserAndPersist("user1", "test1@gmail.com", "12341");
        User user2 = createUserAndPersist("user2", "test2@gmail.com", "12342");

        Article article = Article.create("title", "content", user1);
        em.persist(article);

        final Long id = article.getId();

        final int NUMBER_OF_COMMENTS = 10;
        for (int i = 0; i < NUMBER_OF_COMMENTS; i++) {
            Comment comment = Comment.create("comment " + i, article, (i % 2 == 1) ? user1 : user2);
            em.persist(comment);
        }

        em.flush();
        em.clear();


        //when
        Article findArticle = articleService.lookUp(id);

        List<Comment> comments = findArticle.getComments();

        //then
        //article
        assertThat(findArticle.getTitle())
                .as("제목")
                .isEqualTo("title");

        assertThat(findArticle.getContent())
                .as("내용")
                .isEqualTo("content");

        assertThat(findArticle.getView())
                .as("조회수 증가")
                .isEqualTo(article.getView() + 1L);

        //article.author
        User articleAuthor = findArticle.getAuthor();
        assertThat(articleAuthor)
                .as("게시글 작성자 페치 조인 성공")
                .isNotInstanceOf(HibernateProxy.class);

        assertThat(articleAuthor.getName())
                .as("작성자 이름")
                .isEqualTo("user1");

        //article.comments
        assertThat(comments.size())
                .as("댓글 수")
                .isEqualTo(NUMBER_OF_COMMENTS);

        assertThat(comments)
                .extracting("content")
                .as("댓글 내용")
                .containsExactly(getCommentContents(0, NUMBER_OF_COMMENTS));

        //comment.article
        assertThat(comments)
                .extracting("article")
                .as("Article 확인")
                .containsOnly(findArticle);

        assertThat(comments)
                .extracting("article")
                .as("게시글 - 댓글 페치 조인 성공")
                .allMatch(isNotProxy());

        //comment.author
        assertThat(comments)
                .extracting("author")
                .as("댓글 작성자 페치 조인 성공")
                .allMatch(isNotProxy());

        for (int i = 0; i < comments.size(); i++) {
            User commentAuthor = comments.get(i).getAuthor();

            assertThat(commentAuthor.getName())
                    .as("댓글 작성자 이름")
                    .isEqualTo((i % 2 == 1) ? "user1" : "user2");
        }
    }

    @Test
    @DisplayName("모든 댓글과 함께 조회 실패")
    void lookUp_fail() {
        //given
        User user = createUserAndPersist("user", "test@gmail.com", "1234");

        Article article = Article.create("title", "content", user);
        em.persist(article);

        final Long WRONG_ID = 4444L;

        em.flush();
        em.clear();

        //when & then
        assertThatThrownBy(() -> articleService.lookUp(WRONG_ID))
                .as("게시글 조회 실패 시 예외")
                .isInstanceOf(FailToFindEntityException.class);
    }

    @Test
    @DisplayName("페이징된 댓글과 함께 조회 성공")
    void lookUpPaging() {
        //given
        User user1 = createUserAndPersist("user1", "test1@gmail.com", "12341");
        User user2 = createUserAndPersist("user2", "test2@gmail.com", "12342");

        Article article = Article.create("title", "content", user1);
        em.persist(article);

        final Long id = article.getId();

        final int NUMBER_OF_COMMENTS = 123;

        final int PAGE_1 = 0, SIZE_1 = 10;
        final int PAGE_2 = 2, SIZE_2 = 20;
        final int PAGE_3 = 19, SIZE_3 = 5;

        final Pageable pageable1 = PageRequest.of(PAGE_1, SIZE_1);
        final Pageable pageable2 = PageRequest.of(PAGE_2, SIZE_2);
        final Pageable pageable3 = PageRequest.of(PAGE_3, SIZE_3);

        for (int i = PAGE_1; i < NUMBER_OF_COMMENTS; i++) {
            Comment comment = Comment.create("comment " + i, article, (i % 2 == 1) ? user1 : user2);
            em.persist(comment);
        }

        em.flush();
        em.clear();

        //when
        LookUp lookUp1 = articleService.lookUp(id, pageable1);
        LookUp lookUp2 = articleService.lookUp(id, pageable2);
        LookUp lookUp3 = articleService.lookUp(id, pageable3);

        // then
        //article
        Article findArticle1 = lookUp1.getArticle();
        Article findArticle2 = lookUp2.getArticle();
        Article findArticle3 = lookUp3.getArticle();

        assertThat(findArticle1)
                .as("동일한 객체")
                .isSameAs(findArticle2);

        assertThat(findArticle2)
                .as("동일한 객체")
                .isSameAs(findArticle3);

        assertThat(findArticle3)
                .as("동일한 객체")
                .isSameAs(findArticle1);

        assertThat(findArticle1.getTitle())
                .as("제목")
                .isEqualTo("title");

        assertThat(findArticle1.getContent())
                .as("내용")
                .isEqualTo("content");

        assertThat(findArticle1.getView())
                .as("조회수 증가")
                .isEqualTo(article.getView() + 3L);

        //article.author
        User articleAuthor = findArticle1.getAuthor();

        assertThat(articleAuthor.getClass())
                .as("게시글 작성자 페치 조인 성공")
                .isNotInstanceOf(HibernateProxy.class);

        assertThat(articleAuthor.getName())
                .as("게시글 작성자 이름")
                .isEqualTo("user1");

        //article.comments
        Page<Comment> comments1 = lookUp1.getComments();
        Page<Comment> comments2 = lookUp2.getComments();
        Page<Comment> comments3 = lookUp3.getComments();

        assertThat(comments1.getContent())
                .extracting("content")
                .as("댓글 내용")
                .containsExactly(getCommentContents(PAGE_1, SIZE_1));

        assertThat(comments2.getContent())
                .extracting("content")
                .as("댓글 내용")
                .containsExactly(getCommentContents(PAGE_2, SIZE_2));

        assertThat(comments3.getContent())
                .extracting("content")
                .as("댓글 내용")
                .containsExactly(getCommentContents(PAGE_3, SIZE_3));

        //comment.author
        assertThat(comments1.getContent())
                .extracting("author")
                .as("댓글 작성자 페치 조인 성공")
                .allMatch(isNotProxy());

        assertThat(comments2.getContent())
                .extracting("author")
                .as("댓글 작성자 페치 조인 성공")
                .allMatch(isNotProxy());

        assertThat(comments3.getContent())
                .extracting("author")
                .as("댓글 작성자 페치 조인 성공")
                .allMatch(isNotProxy());
    }

    @Test
    @DisplayName("페이징된 댓글과 함께 조회 실패")
    void lookUpPaging_fail() {
        //given
        User user1 = createUserAndPersist("user1", "test1@gmail.com", "12341");
        User user2 = createUserAndPersist("user2", "test2@gmail.com", "12342");

        Article article = Article.create("title", "content", user1);
        em.persist(article);

        final Long id = article.getId();

        final int NUMBER_OF_COMMENTS = 123;

        final int PAGE_1 = 100, SIZE_1 = 10;
        final int PAGE_2 = 2, SIZE_2 = 0;
        final int PAGE_3 = -1, SIZE_3 = 5;

        final Pageable pageable1 = PageRequest.of(PAGE_1, SIZE_1);
        final Pageable pageable2 = PageRequest.of(PAGE_2, SIZE_2);
        final Pageable pageable3 = PageRequest.of(PAGE_3, SIZE_3);


        for (int i = PAGE_1; i < NUMBER_OF_COMMENTS; i++) {
            Comment comment = Comment.create("comment " + i, article, (i % 2 == 1) ? user1 : user2);
            em.persist(comment);
        }

        em.flush();
        em.clear();

        //when & then
        assertThatThrownBy(() -> articleService.lookUp(id, pageable1))
                .as("존재하지 않는 페이지")
                .isInstanceOf(FailToFindEntityException.class);

        assertThatThrownBy(() -> articleService.lookUp(id, pageable2))
                .as("페이지 크기가 1보다 작음")
                .isInstanceOf(WrongPageRequestException.class);

        assertThatThrownBy(() -> articleService.lookUp(id, pageable3))
                .as("페이지 번호가 0보다 작음")
                .isInstanceOf(WrongPageRequestException.class);

    }

    private static String[] getCommentContents(int page, int size) {
        int startNum = page * size;

        String[] values = new String[size];
        for (int i = 0; i < size; i++) {
            values[i] = "comment " + startNum++;
        }

        return values;
    }

    @Test
    @DisplayName("수정 성공")
    void update() {
        //given
        User user = createUserAndPersist("user", "test@gmail.com", "1234");
        final Long userId = user.getId();

        Article article = Article.create("title", "content", user);
        em.persist(article);

        final Long id = article.getId();

        em.flush();
        em.clear();

        //when 1
        articleService.update(id, userId, Update.create("titleUpdate", "contentUpdate"));
        em.flush();
        em.clear();

        //then 1
        Article test1 = em.find(Article.class, id);

        assertThat(test1.getTitle())
                .as("제목 및 내용 수정")
                .isEqualTo("titleUpdate");

        assertThat(test1.getContent())
                .as("제목 및 내용 수정")
                .isEqualTo("contentUpdate");

        //when 2
        articleService.update(id, userId, Update.create(null, "contentUpdateOnly"));
        em.flush();
        em.clear();

        //then 2
        Article test2 = em.find(Article.class, id);

        assertThat(test2.getTitle())
                .as("내용만 수정")
                .isEqualTo("titleUpdate");

        assertThat(test2.getContent())
                .as("내용만 수정")
                .isEqualTo("contentUpdateOnly");

        //when 3
        articleService.update(id, userId, Update.create("titleUpdateOnly", null));
        em.flush();
        em.clear();

        //then 3
        Article test3 = em.find(Article.class, id);

        assertThat(test3.getTitle())
                .as("제목만 수정")
                .isEqualTo("titleUpdateOnly");

        assertThat(test3.getContent())
                .as("제목만 수정")
                .isEqualTo("contentUpdateOnly");


        //when 4
        articleService.update(id, userId, null);
        em.flush();
        em.clear();

        //then 4
        Article test4 = em.find(Article.class, id);

        assertThat(test4.getTitle())
                .as("UpdateParam null")
                .isEqualTo("titleUpdateOnly");

        assertThat(test4.getContent())
                .as("UpdateParam null")
                .isEqualTo("contentUpdateOnly");

        assertThat(test4.getView()).isEqualTo(0L);
    }

    @Test
    @DisplayName("수정 실패")
    void update_fail() {
        //given
        User user1 = createUserAndPersist("user1", "test1@gmail.com", "12341");
        User user2 = createUserAndPersist("user2", "test2@gmail.com", "12342");

        final Long user1Id = user1.getId();
        final Long user2Id = user2.getId();

        Article article1 = Article.create("title1", "content2", user1);
        Article article2 = Article.create("title2", "content2", user2);

        em.persist(article1);
        em.persist(article2);

        final Long id1 = article1.getId();
        final Long id2 = article2.getId();

        final Long WRONG_ID = 4444L;

        em.flush();
        em.clear();

        //when & then
        assertThatThrownBy(() -> articleService.update(id1, user2Id, Update.create("titleUpdate", "contentUpdate")))
                .as("작성자가 아닌 사용자가 수정")
                .isInstanceOf(NoAuthorityException.class);

        assertThatThrownBy(() -> articleService.update(id2, user1Id, Update.create("titleUpdate", "contentUpdate")))
                .as("작성자 이외의 사용자가 수정")
                .isInstanceOf(NoAuthorityException.class);

        assertThatThrownBy(() -> articleService.update(id1, WRONG_ID, Update.create("titleUpdate", "contentUpdate")))
                .as("작성자 이외의 사용자가 수정 - 존재하지 않는 사용자 ID인 경우")
                .isInstanceOf(NoAuthorityException.class);

        assertThatThrownBy(() -> articleService.update(WRONG_ID, user1Id, Update.create("titleUpdate", "contentUpdate")))
                .as("존재하지 않는 게시글 수정")
                .isInstanceOf(FailToFindEntityException.class);

        assertThatThrownBy(() -> articleService.update(WRONG_ID, WRONG_ID, Update.create("titleUpdate", "contentUpdate")))
                .as("존재하지 않는 게시글 수정 - 존재하지 않는 사용자 ID인 경우")
                .isInstanceOf(FailToFindEntityException.class);

    }

    @Test
    @DisplayName("삭제 성공")
    void delete() {
        //given
        User user = createUserAndPersist("user", "test@gmail.com", "1234");
        Long userId = user.getId();

        Article article = Article.create("title", "content", user);
        em.persist(article);
        Long id = article.getId();

        final int NUMBER_OF_COMMENTS = 10;
        final List<Long> commentIds = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_COMMENTS; i++) {
            Comment comment = Comment.create("comment " + i, article, user);
            em.persist(comment);
            commentIds.add(comment.getId());
        }

        em.flush();
        em.clear();

        //when
        articleService.delete(id, userId);

        //then
        Article findArticle = em.find(Article.class, id);
        assertThat(findArticle)
                .as("삭제된 게시글")
                .isNull();

        List<Comment> findComments = commentIds.stream()
                .map((commentId) -> em.find(Comment.class, commentId))
                .toList();

        assertThat(findComments)
                .as("게시글 삭제 시 댓글도 삭제")
                .containsOnlyNulls();
    }

    @Test
    @DisplayName("삭제 실패")
    void delete_fail() {
        //given
        User user1 = createUserAndPersist("user1", "test1@gmail.com", "12341");
        User user2 = createUserAndPersist("user2", "test2@gmail.com", "12342");

        final Long user1Id = user1.getId();
        final Long user2Id = user2.getId();

        Article article1 = Article.create("title1", "content2", user1);
        Article article2 = Article.create("title2", "content2", user2);

        em.persist(article1);
        em.persist(article2);

        final Long id1 = article1.getId();
        final Long id2 = article2.getId();

        final Long WRONG_ID = 4444L;

        em.flush();
        em.clear();

        //when & then
        assertThatThrownBy(() -> articleService.delete(id1, user2Id))
                .as("작성자가 아닌 사용자가 삭제")
                .isInstanceOf(NoAuthorityException.class);

        assertThatThrownBy(() -> articleService.delete(id2, user1Id))
                .as("작성자가 아닌 사용자가 삭제")
                .isInstanceOf(NoAuthorityException.class);

        assertThatThrownBy(() -> articleService.delete(id1, WRONG_ID))
                .as("작성자가 아닌 사용자가 삭제 - 존재하지 않는 사용자 ID인 경우")
                .isInstanceOf(NoAuthorityException.class);

        assertThatThrownBy(() -> articleService.delete(WRONG_ID, user1Id))
                .as("존재하지 않는 게시글 삭제")
                .isInstanceOf(FailToFindEntityException.class);

        assertThatThrownBy(() -> articleService.delete(WRONG_ID, WRONG_ID))
                .as("존재하지 않는 게시글 삭제 - 존재하지 않는 사용자 ID인 경우")
                .isInstanceOf(FailToFindEntityException.class);

    }
}