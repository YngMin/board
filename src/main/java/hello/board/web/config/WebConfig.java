package hello.board.web.config;

import hello.board.web.argumentresolver.LoginArgumentResolver;
import hello.board.web.aspect.BindingAspect;
import hello.board.web.dtoresolver.ArticleServiceDtoResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Profile({"prod", "local"})
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new LoginArgumentResolver());
    }

    @Bean
    public BindingAspect bindingAspect() {
        return new BindingAspect();
    }

}
