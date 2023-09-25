package hello.board.web.controller.view;

import com.fasterxml.jackson.databind.ObjectMapper;
import hello.board.dto.form.UserForm;
import hello.board.dto.form.UserForm.Save;
import hello.board.service.command.UserService;
import hello.board.service.query.UserQueryService;
import hello.board.web.aspect.BindingErrorsHandlingAspect;
import hello.board.web.aspect.UserJoinValidationAspect;
import hello.board.web.config.SecurityConfig;
import hello.board.web.config.WebConfig;
import hello.board.web.dtoresolver.UserServiceDtoResolver;
import hello.board.web.interceptor.UserJoinHttpStatusInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.*;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.mockito.BDDMockito.given;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@AutoConfigureMockMvc
@EnableAspectJAutoProxy
@MockBean(JpaMetamodelMappingContext.class)
@Import({UserViewController.class, SecurityConfig.class})
@WebMvcTest(value = UserViewController.class,
        excludeFilters = @Filter(type = ASSIGNABLE_TYPE, classes = WebConfig.class))
class UserViewControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    WebApplicationContext context;

    @MockBean
    UserService userService;

    @MockBean
    UserQueryService userQueryService;

    @TestConfiguration
    static class Config implements WebMvcConfigurer {

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(new UserJoinHttpStatusInterceptor())
                    .addPathPatterns("/join");
        }

        @Bean
        BindingErrorsHandlingAspect bindingErrorsHandlingAspect() {
            return new BindingErrorsHandlingAspect();
        }

        @Bean
        UserJoinValidationAspect userJoinValidationAspect(UserQueryService userQueryService) {
            return new UserJoinValidationAspect(userQueryService);
        }

        @Bean
        UserServiceDtoResolver userServiceDtoResolver() {
            return new UserServiceDtoResolver();
        }
    }


    @Test
    @DisplayName("GET | /login | 로그인 페이지 조회")
    void loginForm() throws Exception {
        //given
        UserForm.Login form = UserForm.Login.empty();

        //when
        ResultActions result = mockMvc.perform(
                get("/login")
        );

        //then
        result.andExpect(status().isOk())
                .andExpect(view().name("login/loginForm"))
                .andExpect(model().attribute("user", form));
    }

    @Test
    @DisplayName("GET | /join | 회원가입 페이지 조회")
    void joinForm() throws Exception {
        //given
        Save form = Save.empty();

        //when
        ResultActions result = mockMvc.perform(
                get("/join")
        );

        //then
        result.andExpect(status().isOk())
                .andExpect(view().name("login/joinForm"))
                .andExpect(model().attribute("user", form));
    }

    @Test
    @DisplayName("POST | /join | 회원가입 성공")
    void join() throws Exception {
        //given
        final String name = "user";
        final String email = "test@board.com";
        final String password = "password";

        //when
        ResultActions result = mockMvc.perform(
                post("/join")
                        .param("name", name)
                        .param("email", email)
                        .param("password", password)
                        .param("passwordCheck", "password")
        );

        //then
        result.andExpect((status()).is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("POST | /join | 회원가입 실패 - name")
    void join_fail_name_minSize() throws Exception {
        //given
        final String name = "U";
        final String email = "test@board.com";
        final String password = "password";
        final String passwordCheck = "password";

        final Save saveForm = new Save();
        saveForm.setName(name);
        saveForm.setEmail(email);
        saveForm.setPassword(password);
        saveForm.setPasswordCheck(passwordCheck);

        //when
        ResultActions result = mockMvc.perform(
                post("/join")
                        .param("name", name)
                        .param("email", email)
                        .param("password", password)
                        .param("passwordCheck", passwordCheck)
        );

        //then
        result.andExpect((status()).isBadRequest())
                .andExpect(view().name("login/joinForm"))
                .andExpect(model().attribute("user", saveForm));
    }

    @Test
    @DisplayName("POST | /join | 회원가입 실패 - email")
    void join_fail_email() throws Exception {
        //given
        final String name = "user";
        final String email = "NOT_EMAIL";
        final String password = "password";
        final String passwordCheck = "password";

        final Save saveForm = new Save();
        saveForm.setName(name);
        saveForm.setEmail(email);
        saveForm.setPassword(password);
        saveForm.setPasswordCheck(passwordCheck);

        //when
        ResultActions result = mockMvc.perform(
                post("/join")
                        .param("name", name)
                        .param("email", email)
                        .param("password", password)
                        .param("passwordCheck", passwordCheck)
        );

        //then
        result.andExpect((status()).isBadRequest())
                .andExpect(view().name("login/joinForm"))
                .andExpect(model().attribute("user", saveForm));
    }

    @Test
    @DisplayName("POST | /join | 회원가입 실패 - password min size")
    void join_fail_password_minSize() throws Exception {
        //given
        final String name = "user";
        final String email = "test@board.com";
        final String password = "pw";
        final String passwordCheck = "pw";

        final Save saveForm = new Save();
        saveForm.setName(name);
        saveForm.setEmail(email);
        saveForm.setPassword(password);
        saveForm.setPasswordCheck(passwordCheck);

        //when
        ResultActions result = mockMvc.perform(
                post("/join")
                        .param("name", name)
                        .param("email", email)
                        .param("password", password)
                        .param("passwordCheck", passwordCheck)
        );

        //then
        result.andExpect((status()).isBadRequest())
                .andExpect(view().name("login/joinForm"))
                .andExpect(model().attribute("user", saveForm));
    }

    @Test
    @DisplayName("POST | /join | 회원가입 실패 - password max size")
    void join_fail_password_maxSize() throws Exception {
        //given
        final String name = "user";
        final String email = "test@board.com";
        final String password = "abcdefghijklmnopqrstuvwxyz";
        final String passwordCheck = "abcdefghijklmnopqrstuvwxyz";

        final Save saveForm = new Save();
        saveForm.setName(name);
        saveForm.setEmail(email);
        saveForm.setPassword(password);
        saveForm.setPasswordCheck(passwordCheck);

        //when
        ResultActions result = mockMvc.perform(
                post("/join")
                        .param("name", name)
                        .param("email", email)
                        .param("password", password)
                        .param("passwordCheck", passwordCheck)
        );

        //then
        result.andExpect((status()).isBadRequest())
                .andExpect(view().name("login/joinForm"))
                .andExpect(model().attribute("user", saveForm));
    }

    @Test
    @DisplayName("POST | /join | 회원가입 실패 - passwords do not match")
    void join_fail_password_notMatch() throws Exception {
        //given
        final String name = "user";
        final String email = "test@board.com";
        final String password = "password";
        final String passwordCheck = "WRONG_PASSWORD";

        final Save saveForm = new Save();
        saveForm.setName(name);
        saveForm.setEmail(email);
        saveForm.setPassword(password);
        saveForm.setPasswordCheck(passwordCheck);

        //when
        ResultActions result = mockMvc.perform(
                post("/join")
                        .param("name", name)
                        .param("email", email)
                        .param("password", password)
                        .param("passwordCheck", passwordCheck)
        );

        //then
        result.andExpect((status()).isBadRequest())
                .andExpect(view().name("login/joinForm"))
                .andExpect(model().attribute("user", saveForm));
    }

    @Test
    @DisplayName("POST | /join | 회원가입 실패 - email exists")
    void join_fail_email_exists() throws Exception {
        //given
        final String name = "user";
        final String email = "test@board.com";
        final String password = "password";
        final String passwordCheck = "password";

        final Save saveForm = new Save();
        saveForm.setName(name);
        saveForm.setEmail(email);
        saveForm.setPassword(password);
        saveForm.setPasswordCheck(passwordCheck);

        given(userQueryService.existsByEmail(email))
                .willReturn(true);

        //when
        ResultActions result = mockMvc.perform(
                post("/join")
                        .param("name", name)
                        .param("email", email)
                        .param("password", password)
                        .param("passwordCheck", passwordCheck)
        );

        //then
        result.andExpect((status()).isBadRequest())
                .andExpect(view().name("login/joinForm"))
                .andExpect(model().attribute("user", saveForm));
    }

}