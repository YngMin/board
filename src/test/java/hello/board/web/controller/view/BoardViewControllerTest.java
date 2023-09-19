package hello.board.web.controller.view;

import com.fasterxml.jackson.databind.ObjectMapper;
import hello.board.domain.Article;
import hello.board.domain.Comment;
import hello.board.domain.EntityReflectionUtils;
import hello.board.domain.User;
import hello.board.dto.service.ArticleServiceDto.LookUp;
import hello.board.dto.service.search.ArticleSearchCond;
import hello.board.dto.service.search.ArticleSearchDto;
import hello.board.dto.view.ArticleResponse.ListView;
import hello.board.dto.view.ArticleResponse.View;
import hello.board.dto.view.ArticleResponse.Write;
import hello.board.dto.view.CommentViewResponse;
import hello.board.exception.FailToFindEntityException;
import hello.board.service.command.ArticleService;
import hello.board.service.query.ArticleQueryService;
import hello.board.service.query.CommentQueryService;
import hello.board.web.aspect.BindingErrorsHandlingAspect;
import hello.board.web.aspect.PageRequestValidationAspect;
import hello.board.web.controller.mock.MockLoginArgumentResolver;
import hello.board.web.dtoresolver.ArticleServiceDtoResolver;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@EnableAspectJAutoProxy
@Import({PageRequestValidationAspect.class, BindingErrorsHandlingAspect.class})
@WebMvcTest(BoardViewController.class)
@AutoConfigureMockMvc
@MockBean(JpaMetamodelMappingContext.class)
class BoardViewControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    WebApplicationContext context;

    @MockBean
    ArticleService articleService;

    @MockBean
    ArticleQueryService articleQueryService;

    @MockBean
    CommentQueryService commentQueryService;

    @Value("${view.board.article-page-size}")
    private int ARTICLE_PAGE_SIZE;

    @Value("${view.board.comment-page-size}")
    private int COMMENT_PAGE_SIZE;


    @TestConfiguration
    static class WebConfig implements WebMvcConfigurer {

        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(new MockLoginArgumentResolver());
        }

        @Bean
        public ArticleServiceDtoResolver articleServiceDtoResolver() {
            return new ArticleServiceDtoResolver();
        }
    }

    @BeforeEach
    void mockMvcSetUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .build();
    }

    @Test
    @DisplayName("GET | /board | 게시글 목록 조회 성공: default")
    void getArticles_default() throws Exception {
        //given
        final Pageable pageable = PageRequest.of(0, ARTICLE_PAGE_SIZE);
        final ArticleSearchCond cond = ArticleSearchCond.empty();
        final Page<ArticleSearchDto> articles = Page.empty(pageable);

        given(articleQueryService.search(eq(cond), eq(pageable)))
                .willReturn(articles);

        //when
        ResultActions result = mockMvc.perform(
                get("/board")
        );

        //then
        result.andExpect(status().isOk())
                .andExpect(view().name("articleList"))
                .andExpect(model().attribute("articles", articles.map(ListView::from)))
                .andExpect(model().attribute("cond", cond))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("prevNumber"))
                .andExpect(model().attributeExists("pageNumbers"))
                .andExpect(model().attributeExists("nextNumber"));
    }

    @Test
    @DisplayName("GET | /board | 게시글 목록 조회 성공: page")
    void getArticles_page() throws Exception {
        //given
        final Pageable pageable = PageRequest.of(1, ARTICLE_PAGE_SIZE);
        final ArticleSearchCond cond = ArticleSearchCond.empty();
        final var content = getArticleSearchDtos(getSimpleAuthor(), 20).subList(10, 20);
        final var articles = new PageImpl<>(content, pageable, 20);

        given(articleQueryService.search(eq(cond), eq(pageable)))
                .willReturn(articles);

        //when
        ResultActions result = mockMvc.perform(
                get("/board")
                .param("page", "2")
        );

        //then
        result.andExpect(status().isOk())
                .andExpect(view().name("articleList"))
                .andExpect(model().attribute("articles", articles.map(ListView::from)))
                .andExpect(model().attribute("cond", cond))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("prevNumber"))
                .andExpect(model().attributeExists("pageNumbers"))
                .andExpect(model().attributeExists("nextNumber"));
    }

    private static User getSimpleAuthor() {
        return User.create("author", "author@board.com", "");
    }

    private static List<ArticleSearchDto> getArticleSearchDtos(User author, int numArticles) {
        return IntStream.range(0, numArticles)
                .mapToObj(i -> Article.create("title" + i, "content" + i, author))
                .map(article -> new ArticleSearchDto(article, 0L))
                .toList();
    }

    @Test
    @DisplayName("GET | /board | 게시글 목록 조회 실패: page - malicious request")
    void getArticles_malicious_page() throws Exception {
        //given
        final Pageable pageable = PageRequest.of(4444, ARTICLE_PAGE_SIZE);
        final ArticleSearchCond cond = ArticleSearchCond.empty();
        final Page<ArticleSearchDto> page = new PageImpl<>(Collections.emptyList(), pageable, 10);

        given(articleQueryService.search(eq(cond), eq(pageable)))
                .willReturn(page);

        //when
        ResultActions result = mockMvc.perform(
                get("/board")
                        .param("page", "4445")
        );

        //then
        result.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET | /board | 게시글 목록 조회 실패: search condition - malicious request")
    void getArticles_malicious_search() throws Exception {
        //given
        final Pageable pageable = PageRequest.of(0, ARTICLE_PAGE_SIZE);
        final ArticleSearchCond cond = ArticleSearchCond.empty();
        final Page<ArticleSearchDto> articles = Page.empty(pageable);

        given(articleQueryService.search(eq(cond), eq(pageable)))
                .willReturn(articles);

        //when
        ResultActions result = mockMvc.perform(
                get("/board")
                        .param("type", "WRONG_TYPE")
        );

        //then
        result.andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/board"));
    }

    @Test
    @DisplayName("GET | /board | 게시글 목록 조회 실패: page - binding fail")
    void getArticles_bindingFail_page() throws Exception {
        //given

        //when
        ResultActions result = mockMvc.perform(
                get("/board")
                        .param("page", "0")
        );

        //then
        result.andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/board"));
    }

    @Test
    @DisplayName("GET | /board/{id} | 게시글 조회 성공: default")
    void getArticle_default() throws Exception {
        //given
        final Long id = 1L;
        final Article article = Article.create("title", "content", getSimpleAuthor());
        final Pageable pageable = PageRequest.of(0, COMMENT_PAGE_SIZE);

        final LookUp lookUp = LookUp.of(article, Page.empty(pageable));

        given(articleService.lookUp(eq(id), eq(pageable)))
                .willReturn(lookUp);

        //when
        ResultActions result = mockMvc.perform(
                get("/board/" + id)
        );

        result.andExpect(status().isOk())
                .andExpect(view().name("article"))
                .andExpect(model().attribute("article", View.of(lookUp)))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("prevNumber"))
                .andExpect(model().attributeExists("pageNumbers"))
                .andExpect(model().attributeExists("nextNumber"));
    }

    @Test
    @DisplayName("GET | /board/{id} | 게시글 조회 성공: page")
    void getArticle_page() throws Exception {
        //given
        final Long id = 1L;
        User author = getSimpleAuthor();
        final Article article = Article.create("title", "content", author);
        final Pageable pageable = PageRequest.of(1, COMMENT_PAGE_SIZE);
        final List<Comment> content = getComments(author, article, 30);
        final Page<Comment> page = new PageImpl<>(content, pageable, 30);

        final LookUp lookUp = LookUp.of(article, page);

        given(articleService.lookUp(eq(id), eq(pageable)))
                .willReturn(lookUp);

        //when
        ResultActions result = mockMvc.perform(
                get("/board/" + id)
                .param("page", "2")
        );

        result.andExpect(status().isOk())
                .andExpect(view().name("article"))
                .andExpect(model().attribute("article", View.of(lookUp)))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("prevNumber"))
                .andExpect(model().attributeExists("pageNumbers"))
                .andExpect(model().attributeExists("nextNumber"));
    }

    private static List<Comment> getComments(User author, Article article, int numComments) {
        return IntStream.range(0, numComments)
                .mapToObj(i -> Comment.create("content" + i, article, author))
                .toList()
                .subList(20, numComments);
    }

    @Test
    @DisplayName("GET | /board/{id} | 게시글 조회 실패: page - binding fail")
    void getArticle_bindingFail_page() throws Exception {
        //given
        final long id = 1L;

        //when
        ResultActions result = mockMvc.perform(
                get("/board/" + id)
                        .param("page", "0")
        );

        //then
        result.andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/board"));
    }

    @Test
    @DisplayName("GET | /board/{id} | 게시글 조회 실패: page - malicious request")
    void getArticle_malicious() throws Exception {
        //given
        final Long id = 1L;
        final Article article = Article.create("title", "content", getSimpleAuthor());
        final Pageable pageable = PageRequest.of(4444, COMMENT_PAGE_SIZE);
        final Page<Comment> comments = new PageImpl<>(Collections.emptyList(), pageable, 10);

        given(articleService.lookUp(eq(id), eq(pageable)))
                .willReturn(LookUp.of(article, comments));

        //when
        ResultActions result = mockMvc.perform(
                get("/board/" + id)
                        .param("page", "4445")
        );

        //then
        result.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET | /board/new-article | 게시글 작성 페이지")
    void newArticle_create() throws Exception {
        //given

        //when
        ResultActions result = mockMvc.perform(
                get("/board/new-article")
        );

        //then
        result.andExpect(status().isOk())
                .andExpect(view().name("newArticle"))
                .andExpect(model().attribute("article", Write.empty()));
    }

    @Test
    @DisplayName("GET | /board/new-article | 게시글 수정 페이지")
    void newArticle_modify() throws Exception {
        //given
        final Long id = 1L;
        final Article article = Article.create("title", "content", getSimpleAuthor());
        EntityReflectionUtils.setIdOfArticle(article, id);

        given(articleQueryService.findById(id))
                .willReturn(article);

        //when
        ResultActions result = mockMvc.perform(
                get("/board/new-article")
                        .param("id", "1")
        );

        //then
        result.andExpect(status().isOk())
                .andExpect(view().name("newArticle"))
                .andExpect(model().attribute("article", Write.from(article)));
    }

    @Test
    @DisplayName("GET | /board/new-article | 게시글 수정 페이지 실패")
    void newArticle_modify_fail() throws Exception {
        //given
        final Long WRONG_ID = 666L;

        given(articleQueryService.findById(WRONG_ID))
                .willThrow(FailToFindEntityException.class);

        //when
        ResultActions result = mockMvc.perform(
                get("/board/new-article")
                        .param("id", "666")
        );

        //then
        result.andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET | /board/{articleId}/modify-comment | 댓글 수정 페이지")
    void modifyComment() throws Exception {
        //given
        final Long articleId = 1L;
        final Long id = 1L;
        User author = getSimpleAuthor();
        Article article = Article.create("title", "content", author);
        Comment comment = Comment.create("content", article, author);

        given(commentQueryService.findWithArticle(id, articleId))
                .willReturn(comment);

        //when
        ResultActions result = mockMvc.perform(
                get("/board/" + articleId + "/modify-comment")
                        .param("id", "1")
        );

        //then
        result.andExpect(status().isOk())
                .andExpect(view().name("modifyComment"))
                .andExpect(model().attribute("comment", CommentViewResponse.of(comment)));

    }

    @Test
    @DisplayName("GET | /board/{articleId}/modify-comment | 댓글 수정 페이지 실패 - 잘못된 게시글 ID")
    void modifyComment_fail_wrongArticleId() throws Exception {
        //given
        final Long WRONG_ARTICLE_ID = 4444L;
        final Long id = 1L;

        given(commentQueryService.findWithArticle(id, WRONG_ARTICLE_ID))
                .willThrow(IllegalArgumentException.class);

        //when
        ResultActions result = mockMvc.perform(
                get("/board/" + WRONG_ARTICLE_ID + "/modify-comment")
                        .param("id", "1")
        );

        //then
        result.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET | /board/{articleId}/modify-comment | 댓글 수정 페이지 실패 - 잘못된 게시글 ID")
    void modifyComment_fail_wrongId() throws Exception {
        //given
        final Long articleId = 1L;
        final Long WRONG_ID = 4444L;

        given(commentQueryService.findWithArticle(WRONG_ID, articleId))
                .willThrow(FailToFindEntityException.class);

        //when
        ResultActions result = mockMvc.perform(
                get("/board/" + articleId + "/modify-comment")
                        .param("id", "4444")
        );

        //then
        result.andExpect(status().isNotFound());
    }
}