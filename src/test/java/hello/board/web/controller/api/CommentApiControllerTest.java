package hello.board.web.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import hello.board.domain.Article;
import hello.board.domain.Comment;
import hello.board.domain.User;
import hello.board.dto.api.CommentApiDto.SaveRequest;
import hello.board.dto.api.CommentApiDto.UpdateRequest;
import hello.board.dto.service.CommentServiceDto.Save;
import hello.board.dto.service.CommentServiceDto.Update;
import hello.board.exception.FailToFindEntityException;
import hello.board.service.command.CommentService;
import hello.board.service.query.CommentQueryService;
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

import java.lang.reflect.Field;
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
@WebMvcTest(CommentApiController.class)
@AutoConfigureMockMvc
@MockBean(JpaMetamodelMappingContext.class)
class CommentApiControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    WebApplicationContext context;
    
    @MockBean
    CommentQueryService commentQueryService;
    
    @MockBean
    CommentService commentService;

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

    private static SaveRequest getSaveRequest(String content) {
        SaveRequest saveRequest = new SaveRequest();
        saveRequest.setContent(content);
        return saveRequest;
    }
    
    @Test
    @DisplayName("POST | /api/articles/{articleId}/comments | 성공")
    void addComment() throws Exception {
        //given
        final long articleId = 1L;
        final Long id = 1L;
        final SaveRequest saveRequest = getSaveRequest("content");
        final String requestBody = objectMapper.writeValueAsString(saveRequest);
        given(commentService.save(any(), any(), any(Save.class)))
                .willReturn(id);

        //when
        ResultActions result = mockMvc.perform(
                post("/api/articles/" + articleId + "/comments")
                        .contentType(APPLICATION_JSON)
                        .content(requestBody)
        );

        //then
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    @DisplayName("POST | /api/articles/{articleId}/comments | 실패: content empty")
    void addComment_fail_empty() throws Exception {
        //given
        final long articleId = 1L;
        final SaveRequest saveRequest = getSaveRequest("");
        final String requestBody = objectMapper.writeValueAsString(saveRequest);

        //when
        ResultActions result = mockMvc.perform(
                post("/api/articles/" + articleId + "/comments")
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
    @DisplayName("GET | /api/articles/{articleId}/comments | 성공: default")
    void getComments_default() throws Exception {
        //given
        final long articleId = 1L;
        final PageRequest pageable = PageRequest.of(0, 10);
        final Page<Comment> page = Page.empty(pageable);

        given(commentQueryService.findByArticleId(eq(articleId), eq(pageable)))
                .willReturn(page);

        //when
        ResultActions result = mockMvc.perform(
                get("/api/articles/" + articleId + "/comments")
                        .contentType(APPLICATION_JSON)
        );

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.pageable.offset").value(0))
                .andExpect(jsonPath("$.pageable.pageSize").value(10));
    }

    @Test
    @DisplayName("GET | /api/articles/{articleId}/comments | 성공: parameter")
    void getComments_parameter() throws Exception {
        //given
        final long articleId = 1L;
        final PageRequest pageable = PageRequest.of(2, 20);
        final Page<Comment> page = Page.empty(pageable);

        given(commentQueryService.findByArticleId(eq(articleId), eq(pageable)))
                .willReturn(page);

        //when
        ResultActions result = mockMvc.perform(
                get("/api/articles/" + articleId + "/comments")
                        .contentType(APPLICATION_JSON)
                        .param("page", "3")
                        .param("size", "20")
        );

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.pageable.offset").value(2 * 20))
                .andExpect(jsonPath("$.pageable.pageSize").value(20));
    }

    @Test
    @DisplayName("GET | /api/articles/{articleId}/comments | 실패: wrong article id")
    void getComments_fail_articleId() throws Exception {
        //given
        final long WRONG_ARTICLE_ID = 666L;
        final PageRequest pageable = PageRequest.of(0, 10);
        final Page<Comment> page = Page.empty(pageable);

        given(commentQueryService.findByArticleId(eq(WRONG_ARTICLE_ID), eq(pageable)))
                .willThrow(IllegalArgumentException.class);

        //when
        ResultActions result = mockMvc.perform(
                get("/api/articles/" + WRONG_ARTICLE_ID + "/comments")
                        .contentType(APPLICATION_JSON)
        );

        //then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD"));
    }

    @Test
    @DisplayName("GET | /api/articles/{articleId}/comments | 실패: wrong page request")
    void getComments_fail_pageRequest() throws Exception {
        //given
        final long id = 1L;

        //when
        ResultActions result = mockMvc.perform(
                get("/api/articles/" + id + "/comments")
                        .contentType(APPLICATION_JSON)
                        .param("page", "3")
                        .param("size", "0")
        );

        //then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD"));
    }

    @Test
    @DisplayName("GET | /api/articles/{articleId}/comments/{id} | 성공")
    void getComment() throws Exception {
        //given
        final long articleId = 1L;
        final Long id = 1L;

        Comment comment = Comment.create(
                "content",
                Article.create("", "", User.create("", "", "")),
                User.create("author", "", "")
        );

        setCommentId(id, comment);

        given(commentQueryService.findWithArticle(eq(id), eq(articleId)))
                .willReturn(comment);

        //when
        ResultActions result = mockMvc.perform(
                get("/api/articles/" + articleId + "/comments/" + id)
                        .contentType(APPLICATION_JSON)
        );

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.content").value("content"))
                .andExpect(jsonPath("$.author").value("author"));
    }

    @Test
    @DisplayName("GET | /api/articles/{articleId}/comments/{id} | 실패: wrong comment id")
    void getComment_fail_commentId() throws Exception {
        //given
        final long articleId = 1L;
        final Long WRONG_ID = 666L;

        given(commentQueryService.findWithArticle(eq(WRONG_ID), eq(articleId)))
                .willThrow(FailToFindEntityException.class);

        //when
        ResultActions result = mockMvc.perform(
                get("/api/articles/" + articleId + "/comments/" + WRONG_ID)
                        .contentType(APPLICATION_JSON)
        );

        //then
        result.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("BAD"));
    }

    @Test
    @DisplayName("GET | /api/articles/{articleId}/comments/{id} | 실패: wrong article id")
    void getComment_fail_articleId() throws Exception {
        //given
        final long WRONG_ARTICLE_ID = 666L;
        final Long id = 1L;

        given(commentQueryService.findWithArticle(eq(id), eq(WRONG_ARTICLE_ID)))
                .willThrow(IllegalArgumentException.class);

        //when
        ResultActions result = mockMvc.perform(
                get("/api/articles/" + WRONG_ARTICLE_ID + "/comments/" + id)
                        .contentType(APPLICATION_JSON)
        );

        //then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD"));
    }

    @Test
    @DisplayName("GET | /api/articles/{articleId}/comments/{id} | 실패: wrong article & comment id")
    void getComment_fail_BothId() throws Exception {
        //given
        final long WRONG_ARTICLE_ID = 666L;
        final Long WRONG_ID = 666L;

        given(commentQueryService.findWithArticle(eq(WRONG_ID), eq(WRONG_ARTICLE_ID)))
                .willThrow(FailToFindEntityException.class);

        //when
        ResultActions result = mockMvc.perform(
                get("/api/articles/" + WRONG_ARTICLE_ID + "/comments/" + WRONG_ID)
                        .contentType(APPLICATION_JSON)
        );

        //then
        result.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("BAD"));
    }

    private static void setCommentId(Long id, Comment comment) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        Class<?> commentClass = Class.forName("hello.board.domain.Comment");
        Field fieldId = commentClass.getDeclaredField("id");
        fieldId.setAccessible(true);
        fieldId.set(comment, id);
    }

    @Test
    @DisplayName("PUT | /api/articles/{articleId}/comments/{id} | 성공")
    void updateComment() throws Exception {
        //given
        final long articleId = 1L;
        final Long id = 1L;
        final UpdateRequest request = new UpdateRequest();
        request.setContent("contentUpdate");
        final String requestBody = objectMapper.writeValueAsString(request);

        final Comment updatedComment = Comment.create(
                "contentUpdate",
                Article.create("", "", User.create("", "", "")),
                User.create("author", "", "")
        );

        given(commentQueryService.findById(eq(id)))
                .willReturn(updatedComment);

        //when
        ResultActions result = mockMvc.perform(
                put("/api/articles/" + articleId + "/comments/" + id)
                        .contentType(APPLICATION_JSON)
                        .content(requestBody)
        );

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("contentUpdate"))
                .andExpect(jsonPath("$.modifiedAt").hasJsonPath());
    }

    @Test
    @DisplayName("PUT | /api/articles/{articleId}/comments/{id} | 실패: content empty")
    void updateComment_fail_empty() throws Exception {
        //given
        final long articleId = 1L;
        final Long id = 1L;
        final UpdateRequest request = new UpdateRequest();
        request.setContent("");
        final String requestBody = objectMapper.writeValueAsString(request);

        //when
        ResultActions result = mockMvc.perform(
                put("/api/articles/" + articleId + "/comments/" + id)
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
    @DisplayName("PUT | /api/articles/{articleId}/comments/{id} | 실패: wrong comment id")
    void updateComment_fail_commentId() throws Exception {
        //given
        final long articleId = 1L;
        final Long WRONG_ID = 666L;
        final UpdateRequest request = new UpdateRequest();
        request.setContent("contentUpdate");
        final String requestBody = objectMapper.writeValueAsString(request);

        doThrow(FailToFindEntityException.class)
                .when(commentService)
                        .update(eq(WRONG_ID), eq(articleId), any(), any(Update.class));

        //when
        ResultActions result = mockMvc.perform(
                put("/api/articles/" + articleId + "/comments/" + WRONG_ID)
                        .contentType(APPLICATION_JSON)
                        .content(requestBody)
        );

        //then
        result.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("BAD"));
    }

    @Test
    @DisplayName("PUT | /api/articles/{articleId}/comments/{id} | 실패: wrong article id")
    void updateComment_fail_articleId() throws Exception {
        //given
        final long WRONG_ARTICLE_ID = 666L;
        final Long id = 1L;
        final UpdateRequest request = new UpdateRequest();
        request.setContent("contentUpdate");
        final String requestBody = objectMapper.writeValueAsString(request);

        doThrow(IllegalArgumentException.class)
                .when(commentService)
                .update(eq(id), eq(WRONG_ARTICLE_ID), any(), any(Update.class));

        //when
        ResultActions result = mockMvc.perform(
                put("/api/articles/" + WRONG_ARTICLE_ID + "/comments/" + id)
                        .contentType(APPLICATION_JSON)
                        .content(requestBody)
        );

        //then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD"));
    }

    @Test
    @DisplayName("PUT | /api/articles/{articleId}/comments/{id} | 실패: wrong article & comment id")
    void updateComment_fail_bothId() throws Exception {
        //given
        final long WRONG_ARTICLE_ID = 666L;
        final Long WRONG_ID = 666L;
        final UpdateRequest request = new UpdateRequest();
        request.setContent("contentUpdate");
        final String requestBody = objectMapper.writeValueAsString(request);

        doThrow(FailToFindEntityException.class)
                .when(commentService)
                .update(eq(WRONG_ID), eq(WRONG_ARTICLE_ID), any(), any(Update.class));

        //when
        ResultActions result = mockMvc.perform(
                put("/api/articles/" + WRONG_ARTICLE_ID + "/comments/" + WRONG_ID)
                        .contentType(APPLICATION_JSON)
                        .content(requestBody)
        );

        //then
        result.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("BAD"));
    }

    @Test
    @DisplayName("DELETE | /api/articles/{articleId}/comments/{id} | 성공")
    void deleteComment() throws Exception {
        //given
        final long articleId = 1L;
        final long id = 1L;

        //when
        ResultActions result = mockMvc.perform(
                delete("/api/articles/" + articleId + "/comments/" + id)
                        .contentType(APPLICATION_JSON)
        );

        //then
        result.andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE | /api/articles/{articleId}/comments/{id} | 실패: wrong comment id")
    void deleteComment_fail_commentId() throws Exception {
        //given
        final long articleId = 1L;
        final Long WRONG_ID = 666L;

        doThrow(FailToFindEntityException.class)
                .when(commentService)
                .delete(eq(WRONG_ID), eq(articleId), any());

        //when
        ResultActions result = mockMvc.perform(
                delete("/api/articles/" + articleId + "/comments/" + WRONG_ID)
                        .contentType(APPLICATION_JSON)
        );

        //then
        result.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("BAD"));
    }

    @Test
    @DisplayName("DELETE | /api/articles/{articleId}/comments/{id} | 실패: wrong article id")
    void deleteComment_fail_articleId() throws Exception {
        //given
        final long WRONG_ARTICLE_ID = 666L;
        final Long id = 1L;

        doThrow(IllegalArgumentException.class)
                .when(commentService)
                .delete(eq(id), eq(WRONG_ARTICLE_ID), any());

        //when
        ResultActions result = mockMvc.perform(
                delete("/api/articles/" + WRONG_ARTICLE_ID + "/comments/" + id)
                        .contentType(APPLICATION_JSON)
        );

        //then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD"));
    }

    @Test
    @DisplayName("DELETE | /api/articles/{articleId}/comments/{id} | 실패: wrong article & comment id")
    void deleteComment_fail_bothId() throws Exception {
        //given
        final long WRONG_ARTICLE_ID = 666L;
        final Long WRONG_ID = 666L;

        doThrow(FailToFindEntityException.class)
                .when(commentService)
                .delete(eq(WRONG_ID), eq(WRONG_ARTICLE_ID), any());

        //when
        ResultActions result = mockMvc.perform(
                delete("/api/articles/" + WRONG_ARTICLE_ID + "/comments/" + WRONG_ID)
                        .contentType(APPLICATION_JSON)
        );

        //then
        result.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("BAD"));
    }
}