package hello.board.web.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import hello.board.domain.Article;
import hello.board.domain.User;
import hello.board.dto.api.ArticleApiDto.SaveRequest;
import hello.board.dto.api.ArticleApiDto.UpdateRequest;
import hello.board.dto.service.search.ArticleSearchCond;
import hello.board.dto.service.search.ArticleSearchDto;
import hello.board.dto.service.search.ArticleSearchType;
import hello.board.exception.FailToFindEntityException;
import hello.board.service.command.ArticleService;
import hello.board.service.query.ArticleQueryService;
import hello.board.web.annotation.Login;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@Slf4j
@WebMvcTest(ArticleApiController.class)
@AutoConfigureMockMvc
@MockBean(JpaMetamodelMappingContext.class)
class ArticleApiControllerTest {

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

    @TestConfiguration
    static class WebConfig implements WebMvcConfigurer {

        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(new MockLoginArgumentResolver());
        }
    }

    static class MockLoginArgumentResolver implements HandlerMethodArgumentResolver {

        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            boolean hasLoginAnnotation = parameter.hasParameterAnnotation(Login.class);
            boolean hasUserType = User.class.isAssignableFrom(parameter.getParameterType());

            return hasLoginAnnotation && hasUserType;
        }

        @Override
        public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
            return User.create("test", "", "");
        }
    }

    @BeforeEach
    void mockMvcSetUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .build();
    }

    private static SaveRequest getSaveRequest(String title, String content) {
        SaveRequest saveRequest = new SaveRequest();
        saveRequest.setTitle(title);
        saveRequest.setContent(content);
        return saveRequest;
    }

    private static UpdateRequest getUpdateRequest(String title, String content) {
        UpdateRequest updateRequest = new UpdateRequest();
        updateRequest.setTitle(title);
        updateRequest.setContent(content);
        return updateRequest;
    }

    @Test
    @DisplayName("POST | /api/articles | 성공")
    void postArticle() throws Exception {
        //given
        final Long id = 1L;
        final SaveRequest saveRequest = getSaveRequest("title", "content");
        final String requestBody = objectMapper.writeValueAsString(saveRequest);

        given(articleService.save(any(), any()))
                .willReturn(id);

        //when
        ResultActions result = mockMvc.perform(
                post("/api/articles")
                .contentType(APPLICATION_JSON)
                .content(requestBody)
        );

        //then
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    @DisplayName("POST | /api/articles | 실패: title & content empty")
    void postArticle_fail_both_empty() throws Exception {
        //given
        final SaveRequest saveRequest = getSaveRequest("", "");
        final String requestBody = objectMapper.writeValueAsString(saveRequest);


        //when
        ResultActions result = mockMvc.perform(post("/api/articles")
                .contentType(APPLICATION_JSON)
                .content(requestBody));


        //then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD"));

        final String responseBody = result.andReturn()
                .getResponse()
                .getContentAsString();

        final List<String> fields = JsonPath.parse(responseBody).read("$.fieldErrors[*].field");

        assertThat(fields)
                .as("Biding Error Fields")
                .containsExactlyInAnyOrder("title", "content");
    }

    @Test
    @DisplayName("POST | /api/articles | 실패: title & content blank")
    void postArticle_fail_both_blank() throws Exception {
        //given
        final SaveRequest saveRequest = getSaveRequest("  ", "  ");
        final String requestBody = objectMapper.writeValueAsString(saveRequest);

        //when
        ResultActions result = mockMvc.perform(post("/api/articles")
                .contentType(APPLICATION_JSON)
                .content(requestBody)
        );

        //then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD"));

        final String responseBody = result.andReturn()
                .getResponse()
                .getContentAsString();

        final List<String> fields = JsonPath.parse(responseBody).read("$.fieldErrors[*].field");

        assertThat(fields)
                .as("Biding Error Fields")
                .containsExactlyInAnyOrder("title", "content");
    }

    @Test
    @DisplayName("POST | /api/articles | 실패: title empty")
    void postArticle_fail_title_empty() throws Exception {
        //given
        final SaveRequest saveRequest = getSaveRequest("", "content");
        final String requestBody = objectMapper.writeValueAsString(saveRequest);

        //when
        ResultActions result = mockMvc.perform(post("/api/articles")
                .contentType(APPLICATION_JSON)
                .content(requestBody)
        );

        //then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD"));

        final String responseBody = result.andReturn()
                .getResponse()
                .getContentAsString();

        final List<String> fields = JsonPath.parse(responseBody).read("$.fieldErrors[*].field");

        assertThat(fields)
                .as("Biding Error Fields")
                .containsExactlyInAnyOrder("title");
    }

    @Test
    @DisplayName("POST | /api/articles | 실패: title blank")
    void postArticle_fail_title_blank() throws Exception {
        //given
        final SaveRequest saveRequest = getSaveRequest(" ", "content");
        final String requestBody = objectMapper.writeValueAsString(saveRequest);

        //when
        ResultActions result = mockMvc.perform(post("/api/articles")
                .contentType(APPLICATION_JSON)
                .content(requestBody)
        );

        //then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD"));

        final String responseBody = result.andReturn()
                .getResponse()
                .getContentAsString();

        final List<String> fields = JsonPath.parse(responseBody).read("$.fieldErrors[*].field");

        assertThat(fields)
                .as("Biding Error Fields")
                .containsExactlyInAnyOrder("title");
    }

    @Test
    @DisplayName("POST | /api/articles | 실패: content empty")
    void postArticle_fail_content_empty() throws Exception {
        //given
        final SaveRequest saveRequest = getSaveRequest("title", "");
        final String requestBody = objectMapper.writeValueAsString(saveRequest);

        //when
        ResultActions result = mockMvc.perform(post("/api/articles")
                .contentType(APPLICATION_JSON)
                .content(requestBody)
        );

        //then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD"));

        final String responseBody = result.andReturn()
                .getResponse()
                .getContentAsString();

        final List<String> fields = JsonPath.parse(responseBody).read("$.fieldErrors[*].field");

        assertThat(fields)
                .as("Biding Error Fields")
                .containsExactlyInAnyOrder("content");
    }

    @Test
    @DisplayName("POST | /api/articles | 실패: content blank")
    void postArticle_fail_content_blank() throws Exception {
        //given
        final SaveRequest saveRequest = getSaveRequest("title", "   ");
        final String requestBody = objectMapper.writeValueAsString(saveRequest);

        //when
        ResultActions result = mockMvc.perform(post("/api/articles")
                .contentType(APPLICATION_JSON)
                .content(requestBody)
        );

        //then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD"));

        final String responseBody = result.andReturn()
                .getResponse()
                .getContentAsString();

        final List<String> fields = JsonPath.parse(responseBody).read("$.fieldErrors[*].field");

        assertThat(fields)
                .as("Biding Error Fields")
                .containsExactlyInAnyOrder("content");
    }

    @Test
    @DisplayName("GET | /api/articles | 성공: default")
    void getArticles_default() throws Exception {
        //given
        final PageRequest pageable = PageRequest.of(0, 10);
        final Page<ArticleSearchDto> page = Page.empty(pageable);

        given(articleQueryService.search(any(ArticleSearchCond.class), any(Pageable.class)))
                .willReturn(page);

        //when
        ResultActions result = mockMvc.perform(
                get("/api/articles")
                .contentType(APPLICATION_JSON)
        );

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.pageable.offset").value(0))
                .andExpect(jsonPath("$.pageable.pageSize").value(10));
    }

    @Test
    @DisplayName("GET | /api/articles | 성공: parameter")
    void getArticles_parameter() throws Exception {
        //given
        final PageRequest pageable = PageRequest.of(2, 20);
        final Page<ArticleSearchDto> page = Page.empty(pageable);

        given(articleQueryService.search(any(ArticleSearchCond.class), any(Pageable.class)))
                .willReturn(page);

        //when
        ResultActions result = mockMvc.perform(
                get("/api/articles")
                        .contentType(APPLICATION_JSON)
                        .param("page", "2")
                        .param("size", "20")
        );

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.pageable.offset").value(2 * 20))
                .andExpect(jsonPath("$.pageable.pageSize").value(20));
    }

    @Test
    @DisplayName("GET | /api/articles | 실패: parameter size less than 1")
    void getArticles_parameter_size_lessThanOne() throws Exception {
        //given

        //when
        ResultActions result = mockMvc.perform(
                get("/api/articles")
                        .contentType(APPLICATION_JSON)
                        .param("page", "2")
                        .param("size", "0")
        );

        //then
        result.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET | /api/articles | 성공: condition")
    void getArticles_condition() throws Exception {
        //given
        final PageRequest pageable = PageRequest.of(0, 10);
        final Page<ArticleSearchDto> page = Page.empty(pageable);
        final ArticleSearchCond cond = new ArticleSearchCond();
        cond.setType(ArticleSearchType.TITLE);
        cond.setKeyword("title");

        given(articleQueryService.search(eq(cond), any(Pageable.class)))
                .willReturn(page);

        //when
        ResultActions result = mockMvc.perform(
                get("/api/articles")
                        .contentType(APPLICATION_JSON)
                        .param("keyword", "title")
                        .param("type", "TITLE")

        );

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.pageable.offset").value(0))
                .andExpect(jsonPath("$.pageable.pageSize").value(10));
    }

    @Test
    @DisplayName("GET | /api/articles | 실패: condition type is wrong")
    void getArticles_condition_wrongType() throws Exception {
        //given

        //when
        ResultActions result = mockMvc.perform(
                get("/api/articles")
                        .contentType(APPLICATION_JSON)
                        .param("keyword", "title")
                        .param("type", "WRONG")

        );

        //then
        result.andExpect(status().isBadRequest());

        final String responseBody = result.andReturn()
                .getResponse()
                .getContentAsString();

        final List<String> fields = JsonPath.parse(responseBody).read("$.fieldErrors[*].field");

        assertThat(fields)
                .as("Biding Error Fields")
                .containsExactlyInAnyOrder("type");
    }

    @Test
    @DisplayName("GET | /api/articles/{id} | 성공")
    void getArticle() throws Exception {
        //given
        final Long id = 1L;
        final Article findArticle = Article.create("title", "content", User.create("author", "", ""))
                        .increaseView();

        given(articleService.lookUp(eq(id)))
                .willReturn(findArticle);

        //when
        ResultActions result = mockMvc.perform(
                get("/api/articles/" + id)
                .contentType(APPLICATION_JSON)
        );

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("title"))
                .andExpect(jsonPath("$.content").value("content"))
                .andExpect(jsonPath("$.author").value("author"))
                .andExpect(jsonPath("$.view").value(1L));
    }

    @Test
    @DisplayName("GET | /api/articles/{id} | 실패: wrong id")
    void getArticle_fail() throws Exception {
        //given
        final Long id = 666L;

        given(articleService.lookUp(eq(id)))
                .willThrow(FailToFindEntityException.class);

        //when
        ResultActions result = mockMvc.perform(
                get("/api/articles/" + id)
                .contentType(APPLICATION_JSON)
        );

        //then
        result.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("BAD"));
    }

    @Test
    @DisplayName("PUT | /api/articles/{id} | 성공 : title & content")
    void updateArticle_both() throws Exception {
        //given
        final long id = 1L;
        final UpdateRequest updateRequest = getUpdateRequest("titleUpdate", "contentUpdate");
        final Article updatedArticle = Article.create("titleUpdate", "contentUpdate", User.create("", "", ""));
        final String requestBody = objectMapper.writeValueAsString(updateRequest);

        given(articleQueryService.findById(eq(id)))
                .willReturn(updatedArticle);

        //when
        ResultActions result = mockMvc.perform(
                put("/api/articles/" + id)
                .contentType(APPLICATION_JSON)
                .content(requestBody)
        );

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("titleUpdate"))
                .andExpect(jsonPath("$.content").value("contentUpdate"));
    }


    @Test
    @DisplayName("PUT | /api/articles/{id} | 실패 : wrong id")
    void updateArticle_fail() throws Exception {
        //given
        final Long id = 666L;
        final UpdateRequest updateRequest = getUpdateRequest("titleUpdate", "contentUpdate");
        final String requestBody = objectMapper.writeValueAsString(updateRequest);

        given(articleQueryService.findById(eq(id)))
                .willThrow(FailToFindEntityException.class);

        //when
        ResultActions result = mockMvc.perform(
                put("/api/articles/" + id)
                .contentType(APPLICATION_JSON)
                .content(requestBody)
        );

        //then
        result.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("BAD"));
    }

    @Test
    @DisplayName("DELETE | /api/articles/{id} | 성공")
    void deleteArticle() throws Exception {
        //given
        final long id = 1L;

        //when
        ResultActions result = mockMvc.perform(
                delete("/api/articles/" + id)
                        .contentType(APPLICATION_JSON)
        );

        //then
        result.andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE | /api/articles/{id} | 실패: wrong id")
    void deleteArticle_fail() throws Exception {
        //given
        final long id = 666L;
        doThrow(FailToFindEntityException.class)
                .when(articleService)
                .delete(eq(id), any());

        //when
        ResultActions result = mockMvc.perform(
                delete("/api/articles/" + id)
                        .contentType(APPLICATION_JSON)
        );

        //then
        result.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("BAD"));
    }
}