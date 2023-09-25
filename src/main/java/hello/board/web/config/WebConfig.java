package hello.board.web.config;

import hello.board.service.query.UserQueryService;
import hello.board.web.argumentresolver.LoginArgumentResolver;
import hello.board.web.aspect.BindingErrorsHandlingAspect;
import hello.board.web.aspect.PageRequestValidationAspect;
import hello.board.web.aspect.UserJoinValidationAspect;
import hello.board.web.dtoresolver.ArticleServiceDtoResolver;
import hello.board.web.dtoresolver.CommentServiceDtoResolver;
import hello.board.web.dtoresolver.UserServiceDtoResolver;
import hello.board.web.interceptor.UserJoinHttpStatusInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new LoginArgumentResolver());
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new UserJoinHttpStatusInterceptor())
                .addPathPatterns("/join");
    }

    @Bean
    public BindingErrorsHandlingAspect bindingErrorsHandlingAspect() {
        return new BindingErrorsHandlingAspect();
    }

    @Bean
    public PageRequestValidationAspect pageRequestValidationAspect() {
        return new PageRequestValidationAspect();
    }

    @Bean
    public UserJoinValidationAspect userJoinValidationAspect(UserQueryService userQueryService) {
        return new UserJoinValidationAspect(userQueryService);
    }

    @Bean
    public ArticleServiceDtoResolver articleServiceDtoResolver() {
        return new ArticleServiceDtoResolver();
    }

    @Bean
    public CommentServiceDtoResolver commentServiceDtoResolver() {
        return new CommentServiceDtoResolver();
    }

    @Bean
    public UserServiceDtoResolver userServiceDtoResolver() {
        return new UserServiceDtoResolver();
    }

}
