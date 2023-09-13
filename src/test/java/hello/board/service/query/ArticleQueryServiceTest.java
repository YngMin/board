package hello.board.service.query;

import hello.board.domain.Article;
import hello.board.domain.Comment;
import hello.board.domain.User;
import hello.board.dto.service.search.ArticleSearchCond;
import hello.board.dto.service.search.ArticleSearchDto;
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
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static hello.board.dto.service.search.ArticleSearchType.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class ArticleQueryServiceTest {

    @Autowired
    ArticleQueryService articleQueryService;
    
    @Autowired
    UserRepository userRepository;
    
    @Autowired
    ArticleRepository articleRepository;
    
    @Autowired
    CommentRepository commentRepository;
    

    @TestConfiguration
    static class Config {
        @Bean
        ArticleQueryService articleQueryService(ArticleRepository articleRepository) {
            return new ArticleQueryService(articleRepository);
        }
    }

    private User createAndSaveUser(String name, String email, String password) {
        User user = User.create(name, email, password);
        userRepository.save(user);
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
    @DisplayName("findById 성공")
    void findById() {
        //given
        User author = createAndSaveUser("author", "author@board.com", "");

        Article article = Article.create("title", "content", author);
        articleRepository.save(article);
        final Long id = article.getId();

        //when
        Article findArticle = articleQueryService.findById(id);

        //then
        assertThat(findArticle)
                .as("제목")
                .isEqualTo(article);

        assertThat(findArticle.getView())
                .as("조회수")
                .isEqualTo(0L);

        assertThat(article.getAuthor())
                .as("작성자")
                .isEqualTo(author);

    }

    @Test
    @DisplayName("findById 실패")
    void findById_fail() {
        //given
        User author = createAndSaveUser("author", "author@board.com", "");

        Article article = Article.create("title", "content", author);
        articleRepository.save(article);

        final Long WRONG_ID = 4444L;

        //when & then
        assertThatThrownBy(() -> articleQueryService.findById(WRONG_ID))
                .as("존재하지 않는 게시글")
                .isInstanceOf(FailToFindEntityException.class);
    }

    @Test
    @DisplayName("댓글과 함께 조회 성공")
    void findWithComments() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");

        Article article = Article.create("title", "content", author1);
        articleRepository.save(article);
        final Long id = article.getId();

        Comment comment1 = Comment.create("comment1", article, author1);
        Comment comment2 = Comment.create("comment2", article, author2);

        commentRepository.save(comment1);
        commentRepository.save(comment2);

        //when
        Article findArticle = articleQueryService.findWithComments(id);

        //then
        assertThat(findArticle)
                .as("제목")
                .isEqualTo(article);

        assertThat(findArticle.getView())
                .as("조회수")
                .isEqualTo(0L);

        assertThat(article.getAuthor())
                .as("작성자")
                .isEqualTo(author1);

        //article.comments
        List<Comment> comments = findArticle.getComments();

        assertThat(comments)
                .as("댓글")
                .containsExactly(comment1, comment2);

        assertThat(comments)
                .extracting("author")
                .containsExactly(author1, author2);
    }

    @Test
    @DisplayName("댓글과 함께 조회 실패")
    void findWithComments_fail() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");

        Article article = Article.create("title", "content", author1);
        articleRepository.save(article);
        final Long WRONG_ID = 4444L;

        Comment comment1 = Comment.create("comment1", article, author1);
        Comment comment2 = Comment.create("comment2", article, author2);

        commentRepository.save(comment1);
        commentRepository.save(comment2);

        //when & then
        assertThatThrownBy(() -> articleQueryService.findWithComments(WRONG_ID))
                .as("존재하지 않는 게시글")
                .isInstanceOf(FailToFindEntityException.class);
    }

    @Test
    @DisplayName("댓글과 함께 조회 - 댓글 없음")
    void findWithComments_noComment() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");

        Article article = Article.create("title", "content", author1);
        articleRepository.save(article);
        final Long id = article.getId();

        //when
        Article findArticle = articleQueryService.findWithComments(id);

        //then
        assertThat(findArticle)
                .as("제목")
                .isEqualTo(article);

        assertThat(findArticle.getView())
                .as("조회수")
                .isEqualTo(0L);

        assertThat(article.getAuthor())
                .as("작성자")
                .isEqualTo(author1);

        assertThat(findArticle.getComments().isEmpty())
                .as("댓글 없음")
                .isTrue();
    }

    @Test
    @DisplayName("검색 조건 없음 - 게시글 없음")
    void search_noArticle() {
        //given
        final Pageable pageable = PageRequest.of(0, 10);

        //when
        Page<ArticleSearchDto> articleSearchDtos = articleQueryService.search(pageable);


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
    @DisplayName("검색 조건 없음 - 첫 페이지")
    void search_firstPage() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");
        User author3 = createAndSaveUser("author3", "author3@board.com", "");

        List<Article> articles = generateArticles(50, author1, author2, author3);
        articleRepository.saveAll(articles);

        List<Comment> comments = generateComments(articles, author1, 3, 16, 7, 18, 34, 0, 2);
        commentRepository.saveAll(comments);

        final Pageable pageable = PageRequest.of(0, 10);

        //when
        Page<ArticleSearchDto> articleSearchDtos = articleQueryService.search(pageable);


        //then
        assertThat(articleSearchDtos.getTotalPages())
                .as("전체 페이지 수")
                .isEqualTo(5);

        assertThat(articleSearchDtos.getTotalElements())
                .as("전체 게시글 수")
                .isEqualTo(50L);

        final List<Article> inPage = articles.subList(40, 50);
        Collections.reverse(inPage);

        assertThat(articleSearchDtos.getContent())
                .extracting("article")
                .as("게시물")
                .containsExactlyElementsOf(inPage);

        assertThat(articleSearchDtos.getContent())
                .extracting("numComments")
                .as("댓글 수")
                .containsExactly(3L, 2L, 0L, 34L, 18L, 7L, 16L, 3L, 2L, 0L);
    }

    @Test
    @DisplayName("검색 조건 없음 - 중간 페이지")
    void search_middlePage() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");
        User author3 = createAndSaveUser("author3", "author3@board.com", "");

        List<Article> articles = generateArticles(50, author1, author2, author3);
        articleRepository.saveAll(articles);

        List<Comment> comments = generateComments(articles, author1, 3, 16, 7, 18, 34, 0, 2);
        commentRepository.saveAll(comments);

        final Pageable pageable = PageRequest.of(2, 10);

        //when
        Page<ArticleSearchDto> articleSearchDtos = articleQueryService.search(pageable);


        //then
        assertThat(articleSearchDtos.getTotalPages())
                .as("전체 페이지 수")
                .isEqualTo(5);

        assertThat(articleSearchDtos.getTotalElements())
                .as("전체 게시글 수")
                .isEqualTo(50L);

        final List<Article> inPage = articles.subList(20, 30);
        Collections.reverse(inPage);

        assertThat(articleSearchDtos.getContent())
                .extracting("article")
                .as("게시물")
                .containsExactlyElementsOf(inPage);

        assertThat(articleSearchDtos.getContent())
                .extracting("numComments")
                .as("댓글 수")
                .containsExactly(16L, 3L, 2L, 0L, 34L, 18L, 7L, 16L, 3L, 2L);
    }

    @Test
    @DisplayName("검색 조건 없음 - 마지막 페이지")
    void search_endPage() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");
        User author3 = createAndSaveUser("author3", "author3@board.com", "");

        List<Article> articles = generateArticles(57, author1, author2, author3);
        articleRepository.saveAll(articles);

        List<Comment> comments = generateComments(articles, author1, 3, 16, 7, 18, 34, 0, 2);
        commentRepository.saveAll(comments);

        final Pageable pageable = PageRequest.of(5, 10);

        //when
        Page<ArticleSearchDto> articleSearchDtos = articleQueryService.search(pageable);


        //then
        assertThat(articleSearchDtos.getTotalPages())
                .as("전체 페이지 수")
                .isEqualTo(6);

        assertThat(articleSearchDtos.getTotalElements())
                .as("전체 게시글 수")
                .isEqualTo(57L);

        final List<Article> inPage = articles.subList(0, 7);
        Collections.reverse(inPage);

        assertThat(articleSearchDtos.getContent())
                .extracting("article")
                .as("게시물")
                .containsExactlyElementsOf(inPage);

        assertThat(articleSearchDtos.getContent())
                .extracting("numComments")
                .as("댓글 수")
                .containsExactly(2L, 0L, 34L, 18L, 7L, 16L, 3L);
    }

    @Test
    @DisplayName("검색 조건 없음 - 결과 없음")
    void search_resultEmpty() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");
        User author3 = createAndSaveUser("author3", "author3@board.com", "");

        List<Article> articles = generateArticles(50, author1, author2, author3);
        articleRepository.saveAll(articles);

        List<Comment> comments = generateComments(articles, author1, 3, 16, 7, 18, 34, 0, 2);
        commentRepository.saveAll(comments);

        final Pageable pageable = PageRequest.of(666, 10);

        //when
        Page<ArticleSearchDto> articleSearchDtos = articleQueryService.search(pageable);

        //then
        assertThat(articleSearchDtos.getTotalPages())
                .as("전체 페이지 수")
                .isEqualTo(5);

        assertThat(articleSearchDtos.getTotalElements())
                .as("전체 게시글 수")
                .isEqualTo(50L);

        assertThat(articleSearchDtos.isEmpty())
                .as("페이지 내 게시글 없음")
                .isTrue();
    }

    @Test
    @DisplayName("검색 조건 - 제목 및 내용: 빈 조건 1")
    void search_cond_titleAndContent_empty_1() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");
        User author3 = createAndSaveUser("author3", "author3@board.com", "");

        Article article1 = Article.create("asfaXs" + "target" + "aaf", "asfastaasetaag", author1);
        Article article2 = Article.create("asfasfarerjser", "segerh" + "target" + "seh", author2);
        Article article3 = Article.create("ashdsdh" + "TARgeT" + "rqvcbn", "2343thg\thftdag", author3);
        Article article4 = Article.create("asfaXstarge taaf", "gdg365$\n%#$g", author1);
        Article article5 = Article.create("sdfsdg sdsg", "    targeT" + "asfastsdfs112Ka ss", author2);
        Article article6 = Article.create("  asfsgsdgaXs  sdfsfs", "sdgs\ngsdgs", author3);
        Article article7 = Article.create("cmv,xvpxz getaaf", "asfastxnc,.spnxx..a asetaag", author1);
        Article article8 = Article.create("dsffsfgls;nae;'akjfdla's", "sdgfgdssg sdgs\n" + "target", author2);
        Article article9 = Article.create("a'fj'al;sghp]oenk.", "asfasaladogja'[hr", author3);
        Article article10 = Article.create("게시글제목" + "taRget", " 게시글 내용 1ㅔㅓ~ㅓㅔㅓㄷㄴㄹ", author1);
        Article article11 = Article.create("두더지계란후라이", "파절이", author2);
        Article article12 = Article.create("kxlnvnl" + " TARGET  " + "aw[x2-o304354", "힘드렁", author3);
        Article article13 = Article.create("41653166", "as68489512aag", author1);
        Article article14 = Article.create("!gfdfdh#$", "        sdgsg\nssgg", author2);
        Article article15 = Article.create("ahahsgana", "asf\t\nsdgs", author3);
        Article article16 = Article.create("sahashfa", "asfgstaag" + "target", author1);
        Article article17 = Article.create("shasher234$#@45", "asfasta asetaag", author2);
        Article article18 = Article.create("인텔리제이", "미나리", author3);
        Article article19 = Article.create("댓글" + "target", "asgdaskljg;", author1);
        Article article20 = Article.create("poerweqj", "target" + "awpo    tjsg", author2);

        List<Article> articles = List.of(
                article1, article2, article3, article4, article5,
                article6, article7, article8, article9, article10,
                article11, article12, article13, article14, article15,
                article16, article17, article18, article19, article20
        );

        articleRepository.saveAll(articles);

        List<Comment> comments = generateComments(articles, author1, 0, 0, 1, 1, 2, 2);
        commentRepository.saveAll(comments);

        final Pageable pageable = PageRequest.of(1, 4);
        ArticleSearchCond cond = ArticleSearchCond.create("", TITLE_AND_CONTENT);

        //when
        Page<ArticleSearchDto> articleSearchDtos = articleQueryService.search(cond, pageable);


        //then
        assertThat(articleSearchDtos.getTotalPages())
                .as("전체 페이지 수")
                .isEqualTo(5);

        assertThat(articleSearchDtos.getTotalElements())
                .as("전체 게시글 수")
                .isEqualTo(20L);

        assertThat(articleSearchDtos.getContent())
                .extracting("article")
                .as("게시물")
                .containsExactly(article16, article15, article14, article13);

        assertThat(articleSearchDtos.getContent())
                .extracting("numComments")
                .as("댓글 수")
                .containsExactly(1L, 1L, 0L, 0L);
    }

    @Test
    @DisplayName("검색 조건 - 제목 및 내용: 빈 조건 2")
    void search_cond_titleAndContent_empty_2() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");
        User author3 = createAndSaveUser("author3", "author3@board.com", "");

        Article article1 = Article.create("asfaXs" + "target" + "aaf", "asfastaasetaag", author1);
        Article article2 = Article.create("asfasfarerjser", "segerh" + "target" + "seh", author2);
        Article article3 = Article.create("ashdsdh" + "TARgeT" + "rqvcbn", "2343thg\thftdag", author3);
        Article article4 = Article.create("asfaXstarge taaf", "gdg365$\n%#$g", author1);
        Article article5 = Article.create("sdfsdg sdsg", "    targeT" + "asfastsdfs112Ka ss", author2);
        Article article6 = Article.create("  asfsgsdgaXs  sdfsfs", "sdgs\ngsdgs", author3);
        Article article7 = Article.create("cmv,xvpxz getaaf", "asfastxnc,.spnxx..a asetaag", author1);
        Article article8 = Article.create("dsffsfgls;nae;'akjfdla's", "sdgfgdssg sdgs\n" + "target", author2);
        Article article9 = Article.create("a'fj'al;sghp]oenk.", "asfasaladogja'[hr", author3);
        Article article10 = Article.create("게시글제목" + "taRget", " 게시글 내용 1ㅔㅓ~ㅓㅔㅓㄷㄴㄹ", author1);
        Article article11 = Article.create("두더지계란후라이", "파절이", author2);
        Article article12 = Article.create("kxlnvnl" + " TARGET  " + "aw[x2-o304354", "힘드렁", author3);
        Article article13 = Article.create("41653166", "as68489512aag", author1);
        Article article14 = Article.create("!gfdfdh#$", "        sdgsg\nssgg", author2);
        Article article15 = Article.create("ahahsgana", "asf\t\nsdgs", author3);
        Article article16 = Article.create("sahashfa", "asfgstaag" + "target", author1);
        Article article17 = Article.create("shasher234$#@45", "asfasta asetaag", author2);
        Article article18 = Article.create("인텔리제이", "미나리", author3);
        Article article19 = Article.create("댓글" + "target", "asgdaskljg;", author1);
        Article article20 = Article.create("poerweqj", "target" + "awpo    tjsg", author2);

        List<Article> articles = List.of(
                article1, article2, article3, article4, article5,
                article6, article7, article8, article9, article10,
                article11, article12, article13, article14, article15,
                article16, article17, article18, article19, article20
        );

        articleRepository.saveAll(articles);

        List<Comment> comments = generateComments(articles, author1, 0, 0, 1, 1, 2, 2);
        commentRepository.saveAll(comments);

        final Pageable pageable = PageRequest.of(1, 4);
        ArticleSearchCond cond = ArticleSearchCond.create("             ", TITLE_AND_CONTENT);

        //when
        Page<ArticleSearchDto> articleSearchDtos = articleQueryService.search(cond, pageable);


        //then
        assertThat(articleSearchDtos.getTotalPages())
                .as("전체 페이지 수")
                .isEqualTo(5);

        assertThat(articleSearchDtos.getTotalElements())
                .as("전체 게시글 수")
                .isEqualTo(20L);

        assertThat(articleSearchDtos.getContent())
                .extracting("article")
                .as("게시물")
                .containsExactly(article16, article15, article14, article13);

        assertThat(articleSearchDtos.getContent())
                .extracting("numComments")
                .as("댓글 수")
                .containsExactly(1L, 1L, 0L, 0L);
    }


    @Test
    @DisplayName("검색 조건 - 제목 및 내용")
    void search_cond_titleAndContent() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");
        User author3 = createAndSaveUser("author3", "author3@board.com", "");

        Article article1 = Article.create("asfaXs" + "target" + "aaf", "asfastaasetaag", author1);
        Article article2 = Article.create("asfasfarerjser", "segerh" + "target" + "seh", author2);
        Article article3 = Article.create("ashdsdh" + "TARgeT" + "rqvcbn", "2343thg\thftdag", author3);
        Article article4 = Article.create("asfaXstarge taaf", "gdg365$\n%#$g", author1);
        Article article5 = Article.create("sdfsdg sdsg", "    targeT" + "asfastsdfs112Ka ss", author2);
        Article article6 = Article.create("  asfsgsdgaXs  sdfsfs", "sdgs\ngsdgs", author3);
        Article article7 = Article.create("cmv,xvpxz getaaf", "asfastxnc,.spnxx..a asetaag", author1);
        Article article8 = Article.create("dsffsfgls;nae;'akjfdla's", "sdgfgdssg sdgs\n" + "target", author2);
        Article article9 = Article.create("a'fj'al;sghp]oenk.", "asfasaladogja'[hr", author3);
        Article article10 = Article.create("게시글제목" + "taRget", " 게시글 내용 1ㅔㅓ~ㅓㅔㅓㄷㄴㄹ", author1);
        Article article11 = Article.create("두더지계란후라이", "파절이", author2);
        Article article12 = Article.create("kxlnvnl" + " TARGET  " + "aw[x2-o304354", "힘드렁", author3);
        Article article13 = Article.create("41653166", "as68489512aag", author1);
        Article article14 = Article.create("!gfdfdh#$", "        sdgsg\nssgg", author2);
        Article article15 = Article.create("ahahsgana", "asf\t\nsdgs", author3);
        Article article16 = Article.create("sahashfa", "asfgstaag" + "target", author1);
        Article article17 = Article.create("shasher234$#@45", "asfasta asetaag", author2);
        Article article18 = Article.create("인텔리제이", "미나리", author3);
        Article article19 = Article.create("댓글" + "target", "asgdaskljg;", author1);
        Article article20 = Article.create("poerweqj", "target" + "awpo    tjsg", author2);

        List<Article> articles = List.of(
                article1, article2, article3, article4, article5,
                article6, article7, article8, article9, article10,
                article11, article12, article13, article14, article15,
                article16, article17, article18, article19, article20
        );

        articleRepository.saveAll(articles);

        List<Comment> comments = generateComments(articles, author1, 0, 0, 1, 1, 2, 2);
        commentRepository.saveAll(comments);

        final Pageable pageable = PageRequest.of(1, 4);
        ArticleSearchCond cond = ArticleSearchCond.create(" TARGET  ", TITLE_AND_CONTENT);

        //when
        Page<ArticleSearchDto> articleSearchDtos = articleQueryService.search(cond, pageable);


        //then
        assertThat(articleSearchDtos.getTotalPages())
                .as("전체 페이지 수")
                .isEqualTo(3);

        assertThat(articleSearchDtos.getTotalElements())
                .as("전체 게시글 수")
                .isEqualTo(10L);

        assertThat(articleSearchDtos.getContent())
                .extracting("article")
                .as("게시물")
                .containsExactly(article10, article8, article5, article3);

        assertThat(articleSearchDtos.getContent())
                .extracting("numComments")
                .as("댓글 수")
                .containsExactly(1L, 0L, 2L, 1L);
    }

    @Test
    @DisplayName("검색 조건 - 제목: 빈 조건 1")
    void search_cond_title_empty_1() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");
        User author3 = createAndSaveUser("author3", "author3@board.com", "");

        Article article1 = Article.create("asfaXs" + "target" + "aaf", "asfastaasetaag", author1);
        Article article2 = Article.create("asfasfarerjser", "segerh" + "target" + "seh", author2);
        Article article3 = Article.create("ashdsdh" + "TARgeT" + "rqvcbn", "2343thg\thftdag", author3);
        Article article4 = Article.create("asfaXstarge taaf", "gdg365$\n%#$g", author1);
        Article article5 = Article.create("sdfsdg sdsg", "    targeT" + "asfastsdfs112Ka ss", author2);
        Article article6 = Article.create("  asfsgsdgaXs  sdfsfs", "sdgs\ngsdgs", author3);
        Article article7 = Article.create("cmv,xvpxz getaaf", "asfastxnc,.spnxx..a asetaag", author1);
        Article article8 = Article.create("dsffsfgls;nae;'akjfdla's", "sdgfgdssg sdgs\n" + "target", author2);
        Article article9 = Article.create("a'fj'al;sghp]oenk.", "asfasaladogja'[hr", author3);
        Article article10 = Article.create("게시글제목" + "taRget", " 게시글 내용 1ㅔㅓ~ㅓㅔㅓㄷㄴㄹ", author1);
        Article article11 = Article.create("두더지계란후라이", "파절이", author2);
        Article article12 = Article.create("kxlnvnl" + " TARGET  " + "aw[x2-o304354", "힘드렁", author3);
        Article article13 = Article.create("41653166", "as68489512aag", author1);
        Article article14 = Article.create("!gfdfdh#$", "        sdgsg\nssgg", author2);
        Article article15 = Article.create("ahahsgana", "asf\t\nsdgs", author3);
        Article article16 = Article.create("sahashfa", "asfgstaag" + "target", author1);
        Article article17 = Article.create("shasher234$#@45", "asfasta asetaag", author2);
        Article article18 = Article.create("인텔리제이", "미나리", author3);
        Article article19 = Article.create("댓글" + "target", "asgdaskljg;", author1);
        Article article20 = Article.create("poerweqj", "target" + "awpo    tjsg", author2);

        List<Article> articles = List.of(
                article1, article2, article3, article4, article5,
                article6, article7, article8, article9, article10,
                article11, article12, article13, article14, article15,
                article16, article17, article18, article19, article20
        );

        articleRepository.saveAll(articles);

        List<Comment> comments = generateComments(articles, author1, 0, 0, 1, 1, 2, 2);
        commentRepository.saveAll(comments);

        final Pageable pageable = PageRequest.of(1, 4);
        ArticleSearchCond cond = ArticleSearchCond.create("", TITLE);

        //when
        Page<ArticleSearchDto> articleSearchDtos = articleQueryService.search(cond, pageable);


        //then
        assertThat(articleSearchDtos.getTotalPages())
                .as("전체 페이지 수")
                .isEqualTo(5);

        assertThat(articleSearchDtos.getTotalElements())
                .as("전체 게시글 수")
                .isEqualTo(20L);

        assertThat(articleSearchDtos.getContent())
                .extracting("article")
                .as("게시물")
                .containsExactly(article16, article15, article14, article13);

        assertThat(articleSearchDtos.getContent())
                .extracting("numComments")
                .as("댓글 수")
                .containsExactly(1L, 1L, 0L, 0L);
    }

    @Test
    @DisplayName("검색 조건 - 제목: 빈 조건 2")
    void search_cond_title_empty_2() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");
        User author3 = createAndSaveUser("author3", "author3@board.com", "");

        Article article1 = Article.create("asfaXs" + "target" + "aaf", "asfastaasetaag", author1);
        Article article2 = Article.create("asfasfarerjser", "segerh" + "target" + "seh", author2);
        Article article3 = Article.create("ashdsdh" + "TARgeT" + "rqvcbn", "2343thg\thftdag", author3);
        Article article4 = Article.create("asfaXstarge taaf", "gdg365$\n%#$g", author1);
        Article article5 = Article.create("sdfsdg sdsg", "    targeT" + "asfastsdfs112Ka ss", author2);
        Article article6 = Article.create("  asfsgsdgaXs  sdfsfs", "sdgs\ngsdgs", author3);
        Article article7 = Article.create("cmv,xvpxz getaaf", "asfastxnc,.spnxx..a asetaag", author1);
        Article article8 = Article.create("dsffsfgls;nae;'akjfdla's", "sdgfgdssg sdgs\n" + "target", author2);
        Article article9 = Article.create("a'fj'al;sghp]oenk.", "asfasaladogja'[hr", author3);
        Article article10 = Article.create("게시글제목" + "taRget", " 게시글 내용 1ㅔㅓ~ㅓㅔㅓㄷㄴㄹ", author1);
        Article article11 = Article.create("두더지계란후라이", "파절이", author2);
        Article article12 = Article.create("kxlnvnl" + " TARGET  " + "aw[x2-o304354", "힘드렁", author3);
        Article article13 = Article.create("41653166", "as68489512aag", author1);
        Article article14 = Article.create("!gfdfdh#$", "        sdgsg\nssgg", author2);
        Article article15 = Article.create("ahahsgana", "asf\t\nsdgs", author3);
        Article article16 = Article.create("sahashfa", "asfgstaag" + "target", author1);
        Article article17 = Article.create("shasher234$#@45", "asfasta asetaag", author2);
        Article article18 = Article.create("인텔리제이", "미나리", author3);
        Article article19 = Article.create("댓글" + "target", "asgdaskljg;", author1);
        Article article20 = Article.create("poerweqj", "target" + "awpo    tjsg", author2);

        List<Article> articles = List.of(
                article1, article2, article3, article4, article5,
                article6, article7, article8, article9, article10,
                article11, article12, article13, article14, article15,
                article16, article17, article18, article19, article20
        );

        articleRepository.saveAll(articles);

        List<Comment> comments = generateComments(articles, author1, 0, 0, 1, 1, 2, 2);
        commentRepository.saveAll(comments);

        final Pageable pageable = PageRequest.of(1, 4);
        ArticleSearchCond cond = ArticleSearchCond.create("             ", TITLE);

        //when
        Page<ArticleSearchDto> articleSearchDtos = articleQueryService.search(cond, pageable);


        //then
        assertThat(articleSearchDtos.getTotalPages())
                .as("전체 페이지 수")
                .isEqualTo(5);

        assertThat(articleSearchDtos.getTotalElements())
                .as("전체 게시글 수")
                .isEqualTo(20L);

        assertThat(articleSearchDtos.getContent())
                .extracting("article")
                .as("게시물")
                .containsExactly(article16, article15, article14, article13);

        assertThat(articleSearchDtos.getContent())
                .extracting("numComments")
                .as("댓글 수")
                .containsExactly(1L, 1L, 0L, 0L);
    }


    @Test
    @DisplayName("검색 조건 - 제목")
    void search_cond_title() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");
        User author3 = createAndSaveUser("author3", "author3@board.com", "");

        Article article1 = Article.create("asfaXs" + "target" + "aaf", "asfastaasetaag", author1);
        Article article2 = Article.create("asfasfarerjser", "segerh" + "target" + "seh", author2);
        Article article3 = Article.create("ashdsdh" + "TARgeT" + "rqvcbn", "2343thg\thftdag", author3);
        Article article4 = Article.create("asfaXstarge taaf", "gdg365$\n%#$g", author1);
        Article article5 = Article.create("sdfsdg sdsg", "    targeT" + "asfastsdfs112Ka ss", author2);
        Article article6 = Article.create("  asfsgsdgaXs  sdfsfs", "sdgs\ngsdgs", author3);
        Article article7 = Article.create("cmv,xvpxz getaaf", "asfastxnc,.spnxx..a asetaag", author1);
        Article article8 = Article.create("dsffsfgls;nae;'akjfdla's", "sdgfgdssg sdgs\n" + "target", author2);
        Article article9 = Article.create("a'fj'al;sghp]oenk.", "asfasaladogja'[hr", author3);
        Article article10 = Article.create("게시글제목" + "taRget", " 게시글 내용 1ㅔㅓ~ㅓㅔㅓㄷㄴㄹ", author1);
        Article article11 = Article.create("두더지계란후라이", "파절이", author2);
        Article article12 = Article.create("kxlnvnl" + " TARGET  " + "aw[x2-o304354", "힘드렁", author3);
        Article article13 = Article.create("41653166", "as68489512aag", author1);
        Article article14 = Article.create("!gfdfdh#$", "        sdgsg\nssgg", author2);
        Article article15 = Article.create("ahahsgana", "asf\t\nsdgs", author3);
        Article article16 = Article.create("sahashfa", "asfgstaag" + "target", author1);
        Article article17 = Article.create("shasher234$#@45", "asfasta asetaag", author2);
        Article article18 = Article.create("인텔리제이", "미나리", author3);
        Article article19 = Article.create("댓글" + "target", "asgdaskljg;", author1);
        Article article20 = Article.create("poerweqj", "target" + "awpo    tjsg", author2);

        List<Article> articles = List.of(
                article1, article2, article3, article4, article5,
                article6, article7, article8, article9, article10,
                article11, article12, article13, article14, article15,
                article16, article17, article18, article19, article20
        );

        articleRepository.saveAll(articles);

        List<Comment> comments = generateComments(articles, author1, 0, 0, 1, 1, 2, 2);
        commentRepository.saveAll(comments);

        final Pageable pageable = PageRequest.of(1, 4);
        ArticleSearchCond cond = ArticleSearchCond.create("TARGET", TITLE);

        //when
        Page<ArticleSearchDto> articleSearchDtos = articleQueryService.search(cond, pageable);

        //then
        assertThat(articleSearchDtos.getTotalPages())
                .as("전체 페이지 수")
                .isEqualTo(2);

        assertThat(articleSearchDtos.getTotalElements())
                .as("전체 게시글 수")
                .isEqualTo(5L);

        assertThat(articleSearchDtos.getContent())
                .extracting("article")
                .as("게시물")
                .containsExactly(article1);

        assertThat(articleSearchDtos.getContent())
                .extracting("numComments")
                .as("댓글 수")
                .containsExactly(0L);
    }

    @Test
    @DisplayName("검색 조건 - 내용: 빈 조건 1")
    void search_cond_content_empty_1() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");
        User author3 = createAndSaveUser("author3", "author3@board.com", "");

        Article article1 = Article.create("asfaXs" + "target" + "aaf", "asfastaasetaag", author1);
        Article article2 = Article.create("asfasfarerjser", "segerh" + "target" + "seh", author2);
        Article article3 = Article.create("ashdsdh" + "TARgeT" + "rqvcbn", "2343thg\thftdag", author3);
        Article article4 = Article.create("asfaXstarge taaf", "gdg365$\n%#$g", author1);
        Article article5 = Article.create("sdfsdg sdsg", "    targeT" + "asfastsdfs112Ka ss", author2);
        Article article6 = Article.create("  asfsgsdgaXs  sdfsfs", "sdgs\ngsdgs", author3);
        Article article7 = Article.create("cmv,xvpxz getaaf", "asfastxnc,.spnxx..a asetaag", author1);
        Article article8 = Article.create("dsffsfgls;nae;'akjfdla's", "sdgfgdssg sdgs\n" + "target", author2);
        Article article9 = Article.create("a'fj'al;sghp]oenk.", "asfasaladogja'[hr", author3);
        Article article10 = Article.create("게시글제목" + "taRget", " 게시글 내용 1ㅔㅓ~ㅓㅔㅓㄷㄴㄹ", author1);
        Article article11 = Article.create("두더지계란후라이", "파절이", author2);
        Article article12 = Article.create("kxlnvnl" + " TARGET  " + "aw[x2-o304354", "힘드렁", author3);
        Article article13 = Article.create("41653166", "as68489512aag", author1);
        Article article14 = Article.create("!gfdfdh#$", "        sdgsg\nssgg", author2);
        Article article15 = Article.create("ahahsgana", "asf\t\nsdgs", author3);
        Article article16 = Article.create("sahashfa", "asfgstaag" + "target", author1);
        Article article17 = Article.create("shasher234$#@45", "asfasta asetaag", author2);
        Article article18 = Article.create("인텔리제이", "미나리", author3);
        Article article19 = Article.create("댓글" + "target", "asgdaskljg;", author1);
        Article article20 = Article.create("poerweqj", "target" + "awpo    tjsg", author2);

        List<Article> articles = List.of(
                article1, article2, article3, article4, article5,
                article6, article7, article8, article9, article10,
                article11, article12, article13, article14, article15,
                article16, article17, article18, article19, article20
        );

        articleRepository.saveAll(articles);

        List<Comment> comments = generateComments(articles, author1, 0, 0, 1, 1, 2, 2);
        commentRepository.saveAll(comments);

        final Pageable pageable = PageRequest.of(1, 4);
        ArticleSearchCond cond = ArticleSearchCond.create("", CONTENT);

        //when
        Page<ArticleSearchDto> articleSearchDtos = articleQueryService.search(cond, pageable);


        //then
        assertThat(articleSearchDtos.getTotalPages())
                .as("전체 페이지 수")
                .isEqualTo(5);

        assertThat(articleSearchDtos.getTotalElements())
                .as("전체 게시글 수")
                .isEqualTo(20L);

        assertThat(articleSearchDtos.getContent())
                .extracting("article")
                .as("게시물")
                .containsExactly(article16, article15, article14, article13);

        assertThat(articleSearchDtos.getContent())
                .extracting("numComments")
                .as("댓글 수")
                .containsExactly(1L, 1L, 0L, 0L);
    }

    @Test
    @DisplayName("검색 조건 - 내용: 빈 조건 2")
    void search_cond_content_empty_2() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");
        User author3 = createAndSaveUser("author3", "author3@board.com", "");

        Article article1 = Article.create("asfaXs" + "target" + "aaf", "asfastaasetaag", author1);
        Article article2 = Article.create("asfasfarerjser", "segerh" + "target" + "seh", author2);
        Article article3 = Article.create("ashdsdh" + "TARgeT" + "rqvcbn", "2343thg\thftdag", author3);
        Article article4 = Article.create("asfaXstarge taaf", "gdg365$\n%#$g", author1);
        Article article5 = Article.create("sdfsdg sdsg", "    targeT" + "asfastsdfs112Ka ss", author2);
        Article article6 = Article.create("  asfsgsdgaXs  sdfsfs", "sdgs\ngsdgs", author3);
        Article article7 = Article.create("cmv,xvpxz getaaf", "asfastxnc,.spnxx..a asetaag", author1);
        Article article8 = Article.create("dsffsfgls;nae;'akjfdla's", "sdgfgdssg sdgs\n" + "target", author2);
        Article article9 = Article.create("a'fj'al;sghp]oenk.", "asfasaladogja'[hr", author3);
        Article article10 = Article.create("게시글제목" + "taRget", " 게시글 내용 1ㅔㅓ~ㅓㅔㅓㄷㄴㄹ", author1);
        Article article11 = Article.create("두더지계란후라이", "파절이", author2);
        Article article12 = Article.create("kxlnvnl" + " TARGET  " + "aw[x2-o304354", "힘드렁", author3);
        Article article13 = Article.create("41653166", "as68489512aag", author1);
        Article article14 = Article.create("!gfdfdh#$", "        sdgsg\nssgg", author2);
        Article article15 = Article.create("ahahsgana", "asf\t\nsdgs", author3);
        Article article16 = Article.create("sahashfa", "asfgstaag" + "target", author1);
        Article article17 = Article.create("shasher234$#@45", "asfasta asetaag", author2);
        Article article18 = Article.create("인텔리제이", "미나리", author3);
        Article article19 = Article.create("댓글" + "target", "asgdaskljg;", author1);
        Article article20 = Article.create("poerweqj", "target" + "awpo    tjsg", author2);

        List<Article> articles = List.of(
                article1, article2, article3, article4, article5,
                article6, article7, article8, article9, article10,
                article11, article12, article13, article14, article15,
                article16, article17, article18, article19, article20
        );

        articleRepository.saveAll(articles);

        List<Comment> comments = generateComments(articles, author1, 0, 0, 1, 1, 2, 2);
        commentRepository.saveAll(comments);

        final Pageable pageable = PageRequest.of(1, 4);
        ArticleSearchCond cond = ArticleSearchCond.create("             ", CONTENT);

        //when
        Page<ArticleSearchDto> articleSearchDtos = articleQueryService.search(cond, pageable);


        //then
        assertThat(articleSearchDtos.getTotalPages())
                .as("전체 페이지 수")
                .isEqualTo(5);

        assertThat(articleSearchDtos.getTotalElements())
                .as("전체 게시글 수")
                .isEqualTo(20L);

        assertThat(articleSearchDtos.getContent())
                .extracting("article")
                .as("게시물")
                .containsExactly(article16, article15, article14, article13);

        assertThat(articleSearchDtos.getContent())
                .extracting("numComments")
                .as("댓글 수")
                .containsExactly(1L, 1L, 0L, 0L);
    }


    @Test
    @DisplayName("검색 조건 - 내용")
    void search_cond_content() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");
        User author3 = createAndSaveUser("author3", "author3@board.com", "");

        Article article1 = Article.create("asfaXs" + "target" + "aaf", "asfastaasetaag", author1);
        Article article2 = Article.create("asfasfarerjser", "segerh" + "target" + "seh", author2);
        Article article3 = Article.create("ashdsdh" + "TARgeT" + "rqvcbn", "2343thg\thftdag", author3);
        Article article4 = Article.create("asfaXstarge taaf", "gdg365$\n%#$g", author1);
        Article article5 = Article.create("sdfsdg sdsg", "    targeT" + "asfastsdfs112Ka ss", author2);
        Article article6 = Article.create("  asfsgsdgaXs  sdfsfs", "sdgs\ngsdgs", author3);
        Article article7 = Article.create("cmv,xvpxz getaaf", "asfastxnc,.spnxx..a asetaag", author1);
        Article article8 = Article.create("dsffsfgls;nae;'akjfdla's", "sdgfgdssg sdgs\n" + "target", author2);
        Article article9 = Article.create("a'fj'al;sghp]oenk.", "asfasaladogja'[hr", author3);
        Article article10 = Article.create("게시글제목" + "taRget", " 게시글 내용 1ㅔㅓ~ㅓㅔㅓㄷㄴㄹ", author1);
        Article article11 = Article.create("두더지계란후라이", "파절이", author2);
        Article article12 = Article.create("kxlnvnl" + " TARGET  " + "aw[x2-o304354", "힘드렁", author3);
        Article article13 = Article.create("41653166", "as68489512aag", author1);
        Article article14 = Article.create("!gfdfdh#$", "        sdgsg\nssgg", author2);
        Article article15 = Article.create("ahahsgana", "asf\t\nsdgs", author3);
        Article article16 = Article.create("sahashfa", "asfgstaag" + "target", author1);
        Article article17 = Article.create("shasher234$#@45", "asfasta asetaag", author2);
        Article article18 = Article.create("인텔리제이", "미나리", author3);
        Article article19 = Article.create("댓글" + "target", "asgdaskljg;", author1);
        Article article20 = Article.create("poerweqj", "target" + "awpo    tjsg", author2);

        List<Article> articles = List.of(
                article1, article2, article3, article4, article5,
                article6, article7, article8, article9, article10,
                article11, article12, article13, article14, article15,
                article16, article17, article18, article19, article20
        );

        articleRepository.saveAll(articles);

        List<Comment> comments = generateComments(articles, author1, 0, 0, 1, 1, 2, 2);
        commentRepository.saveAll(comments);

        final Pageable pageable = PageRequest.of(1, 4);
        ArticleSearchCond cond = ArticleSearchCond.create(" TARGET  ", CONTENT);

        //when
        Page<ArticleSearchDto> articleSearchDtos = articleQueryService.search(cond, pageable);


        //then
        assertThat(articleSearchDtos.getTotalPages())
                .as("전체 페이지 수")
                .isEqualTo(2);

        assertThat(articleSearchDtos.getTotalElements())
                .as("전체 게시글 수")
                .isEqualTo(5L);

        assertThat(articleSearchDtos.getContent())
                .extracting("article")
                .as("게시물")
                .containsExactly(article2);

        assertThat(articleSearchDtos.getContent())
                .extracting("numComments")
                .as("댓글 수")
                .containsExactly(0L);
    }


    @Test
    @DisplayName("검색 조건 - 작성자: 빈 조건 1")
    void search_cond_author_empty_1() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");
        User author3 = createAndSaveUser("author3", "author3@board.com", "");

        Article article1 = Article.create("asfaXs" + "target" + "aaf", "asfastaasetaag", author1);
        Article article2 = Article.create("asfasfarerjser", "segerh" + "target" + "seh", author2);
        Article article3 = Article.create("ashdsdh" + "TARgeT" + "rqvcbn", "2343thg\thftdag", author3);
        Article article4 = Article.create("asfaXstarge taaf", "gdg365$\n%#$g", author1);
        Article article5 = Article.create("sdfsdg sdsg", "    targeT" + "asfastsdfs112Ka ss", author2);
        Article article6 = Article.create("  asfsgsdgaXs  sdfsfs", "sdgs\ngsdgs", author3);
        Article article7 = Article.create("cmv,xvpxz getaaf", "asfastxnc,.spnxx..a asetaag", author1);
        Article article8 = Article.create("dsffsfgls;nae;'akjfdla's", "sdgfgdssg sdgs\n" + "target", author2);
        Article article9 = Article.create("a'fj'al;sghp]oenk.", "asfasaladogja'[hr", author3);
        Article article10 = Article.create("게시글제목" + "taRget", " 게시글 내용 1ㅔㅓ~ㅓㅔㅓㄷㄴㄹ", author1);
        Article article11 = Article.create("두더지계란후라이", "파절이", author2);
        Article article12 = Article.create("kxlnvnl" + " TARGET  " + "aw[x2-o304354", "힘드렁", author3);
        Article article13 = Article.create("41653166", "as68489512aag", author1);
        Article article14 = Article.create("!gfdfdh#$", "        sdgsg\nssgg", author2);
        Article article15 = Article.create("ahahsgana", "asf\t\nsdgs", author3);
        Article article16 = Article.create("sahashfa", "asfgstaag" + "target", author1);
        Article article17 = Article.create("shasher234$#@45", "asfasta asetaag", author2);
        Article article18 = Article.create("인텔리제이", "미나리", author3);
        Article article19 = Article.create("댓글" + "target", "asgdaskljg;", author1);
        Article article20 = Article.create("poerweqj", "target" + "awpo    tjsg", author2);

        List<Article> articles = List.of(
                article1, article2, article3, article4, article5,
                article6, article7, article8, article9, article10,
                article11, article12, article13, article14, article15,
                article16, article17, article18, article19, article20
        );

        articleRepository.saveAll(articles);

        List<Comment> comments = generateComments(articles, author1, 0, 0, 1, 1, 2, 2);
        commentRepository.saveAll(comments);

        final Pageable pageable = PageRequest.of(1, 4);
        ArticleSearchCond cond = ArticleSearchCond.create("", AUTHOR);

        //when
        Page<ArticleSearchDto> articleSearchDtos = articleQueryService.search(cond, pageable);


        //then
        assertThat(articleSearchDtos.getTotalPages())
                .as("전체 페이지 수")
                .isEqualTo(5);

        assertThat(articleSearchDtos.getTotalElements())
                .as("전체 게시글 수")
                .isEqualTo(20L);

        assertThat(articleSearchDtos.getContent())
                .extracting("article")
                .as("게시물")
                .containsExactly(article16, article15, article14, article13);

        assertThat(articleSearchDtos.getContent())
                .extracting("numComments")
                .as("댓글 수")
                .containsExactly(1L, 1L, 0L, 0L);
    }

    @Test
    @DisplayName("검색 조건 - 작성자: 빈 조건 2")
    void search_cond_author_empty_2() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");
        User author3 = createAndSaveUser("author3", "author3@board.com", "");

        Article article1 = Article.create("asfaXs" + "target" + "aaf", "asfastaasetaag", author1);
        Article article2 = Article.create("asfasfarerjser", "segerh" + "target" + "seh", author2);
        Article article3 = Article.create("ashdsdh" + "TARgeT" + "rqvcbn", "2343thg\thftdag", author3);
        Article article4 = Article.create("asfaXstarge taaf", "gdg365$\n%#$g", author1);
        Article article5 = Article.create("sdfsdg sdsg", "    targeT" + "asfastsdfs112Ka ss", author2);
        Article article6 = Article.create("  asfsgsdgaXs  sdfsfs", "sdgs\ngsdgs", author3);
        Article article7 = Article.create("cmv,xvpxz getaaf", "asfastxnc,.spnxx..a asetaag", author1);
        Article article8 = Article.create("dsffsfgls;nae;'akjfdla's", "sdgfgdssg sdgs\n" + "target", author2);
        Article article9 = Article.create("a'fj'al;sghp]oenk.", "asfasaladogja'[hr", author3);
        Article article10 = Article.create("게시글제목" + "taRget", " 게시글 내용 1ㅔㅓ~ㅓㅔㅓㄷㄴㄹ", author1);
        Article article11 = Article.create("두더지계란후라이", "파절이", author2);
        Article article12 = Article.create("kxlnvnl" + " TARGET  " + "aw[x2-o304354", "힘드렁", author3);
        Article article13 = Article.create("41653166", "as68489512aag", author1);
        Article article14 = Article.create("!gfdfdh#$", "        sdgsg\nssgg", author2);
        Article article15 = Article.create("ahahsgana", "asf\t\nsdgs", author3);
        Article article16 = Article.create("sahashfa", "asfgstaag" + "target", author1);
        Article article17 = Article.create("shasher234$#@45", "asfasta asetaag", author2);
        Article article18 = Article.create("인텔리제이", "미나리", author3);
        Article article19 = Article.create("댓글" + "target", "asgdaskljg;", author1);
        Article article20 = Article.create("poerweqj", "target" + "awpo    tjsg", author2);

        List<Article> articles = List.of(
                article1, article2, article3, article4, article5,
                article6, article7, article8, article9, article10,
                article11, article12, article13, article14, article15,
                article16, article17, article18, article19, article20
        );

        articleRepository.saveAll(articles);

        List<Comment> comments = generateComments(articles, author1, 0, 0, 1, 1, 2, 2);
        commentRepository.saveAll(comments);

        final Pageable pageable = PageRequest.of(1, 4);
        ArticleSearchCond cond = ArticleSearchCond.create("             ", AUTHOR);

        //when
        Page<ArticleSearchDto> articleSearchDtos = articleQueryService.search(cond, pageable);


        //then
        assertThat(articleSearchDtos.getTotalPages())
                .as("전체 페이지 수")
                .isEqualTo(5);

        assertThat(articleSearchDtos.getTotalElements())
                .as("전체 게시글 수")
                .isEqualTo(20L);

        assertThat(articleSearchDtos.getContent())
                .extracting("article")
                .as("게시물")
                .containsExactly(article16, article15, article14, article13);

        assertThat(articleSearchDtos.getContent())
                .extracting("numComments")
                .as("댓글 수")
                .containsExactly(1L, 1L, 0L, 0L);
    }


    @Test
    @DisplayName("검색 조건 - 작성자")
    void search_cond_author() {
        //given
        User author1 = createAndSaveUser("author1", "author1@board.com", "");
        User author2 = createAndSaveUser("author2", "author2@board.com", "");
        User author3 = createAndSaveUser("author3", "author3@board.com", "");

        Article article1 = Article.create("asfaXs" + "target" + "aaf", "asfastaasetaag", author1);
        Article article2 = Article.create("asfasfarerjser", "segerh" + "target" + "seh", author2);
        Article article3 = Article.create("ashdsdh" + "TARgeT" + "rqvcbn", "2343thg\thftdag", author3);
        Article article4 = Article.create("asfaXstarge taaf", "gdg365$\n%#$g", author1);
        Article article5 = Article.create("sdfsdg sdsg", "    targeT" + "asfastsdfs112Ka ss", author2);
        Article article6 = Article.create("  asfsgsdgaXs  sdfsfs", "sdgs\ngsdgs", author3);
        Article article7 = Article.create("cmv,xvpxz getaaf", "asfastxnc,.spnxx..a asetaag", author1);
        Article article8 = Article.create("dsffsfgls;nae;'akjfdla's", "sdgfgdssg sdgs\n" + "target", author2);
        Article article9 = Article.create("a'fj'al;sghp]oenk.", "asfasaladogja'[hr", author3);
        Article article10 = Article.create("게시글제목" + "taRget", " 게시글 내용 1ㅔㅓ~ㅓㅔㅓㄷㄴㄹ", author1);
        Article article11 = Article.create("두더지계란후라이", "파절이", author2);
        Article article12 = Article.create("kxlnvnl" + " TARGET  " + "aw[x2-o304354", "힘드렁", author3);
        Article article13 = Article.create("41653166", "as68489512aag", author1);
        Article article14 = Article.create("!gfdfdh#$", "        sdgsg\nssgg", author2);
        Article article15 = Article.create("ahahsgana", "asf\t\nsdgs", author3);
        Article article16 = Article.create("sahashfa", "asfgstaag" + "target", author1);
        Article article17 = Article.create("shasher234$#@45", "asfasta asetaag", author2);
        Article article18 = Article.create("인텔리제이", "미나리", author3);
        Article article19 = Article.create("댓글" + "target", "asgdaskljg;", author1);
        Article article20 = Article.create("poerweqj", "target" + "awpo    tjsg", author2);

        List<Article> articles = List.of(
                article1, article2, article3, article4, article5,
                article6, article7, article8, article9, article10,
                article11, article12, article13, article14, article15,
                article16, article17, article18, article19, article20
        );

        articleRepository.saveAll(articles);

        List<Comment> comments = generateComments(articles, author1, 0, 0, 1, 1, 2, 2);
        commentRepository.saveAll(comments);

        final Pageable pageable = PageRequest.of(1, 4);
        ArticleSearchCond cond = ArticleSearchCond.create(" thor1  ", AUTHOR);

        //when
        Page<ArticleSearchDto> articleSearchDtos = articleQueryService.search(cond, pageable);


        //then
        assertThat(articleSearchDtos.getTotalPages())
                .as("전체 페이지 수")
                .isEqualTo(2);

        assertThat(articleSearchDtos.getTotalElements())
                .as("전체 게시글 수")
                .isEqualTo(7L);

        assertThat(articleSearchDtos.getContent())
                .extracting("article")
                .as("게시물")
                .containsExactly(article7, article4, article1);

        assertThat(articleSearchDtos.getContent())
                .extracting("numComments")
                .as("댓글 수")
                .containsExactly(0L, 1L, 0L);
    }
}