package hello.board.web.controller.view;

import com.fasterxml.jackson.databind.ObjectMapper;
import hello.board.domain.Article;
import hello.board.domain.User;
import hello.board.dto.service.search.ArticleSearchCond;
import hello.board.dto.service.search.ArticleSearchDto;
import hello.board.dto.view.ArticleResponse.ListView;
import hello.board.service.command.ArticleService;
import hello.board.service.query.ArticleQueryService;
import hello.board.service.query.CommentQueryService;
import hello.board.web.aspect.PageRequestValidationAspect;
import hello.board.web.controller.mock.MockLoginArgumentResolver;
import hello.board.web.dtoresolver.ArticleServiceDtoResolver;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
@Import(PageRequestValidationAspect.class)
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
    void getArticles_default() throws Exception {
        //given
        final Pageable pageable = PageRequest.of(0, 10);
        final ArticleSearchCond cond = ArticleSearchCond.empty();
        Page<ArticleSearchDto> articles = Page.empty(pageable);

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
    void getArticles_page() throws Exception {
        //given
        final Pageable pageable = PageRequest.of(1, 10);
        final ArticleSearchCond cond = ArticleSearchCond.empty();
        final User author = User.create("author", "author@board.com", "");
        final var content = getArticleSearchDtos(author, 20);
        final var articles = new PageImpl<>(content.subList(10, 20), pageable, 20);

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

    private static List<ArticleSearchDto> getArticleSearchDtos(User author, int numArticles) {
        return IntStream.range(0, numArticles)
                .mapToObj(i -> Article.create("title" + i, "content" + i, author))
                .map(article -> new ArticleSearchDto(article, 0L))
                .toList();
    }

    @Test
    void getArticles_malicious() throws Exception {
        //given
        final Pageable pageable = PageRequest.of(4444, 10);
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
    void getArticle() {
    }

    @Test
    void newArticle() {
    }

    @Test
    void modifyComment() {
    }
}