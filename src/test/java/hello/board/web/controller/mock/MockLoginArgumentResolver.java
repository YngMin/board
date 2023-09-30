package hello.board.web.controller.mock;

import hello.board.domain.User;
import hello.board.web.annotation.Login;
import hello.board.web.user.LoginInfo;
import jakarta.annotation.Nonnull;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class MockLoginArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean hasLoginAnnotation = parameter.hasParameterAnnotation(Login.class);
        boolean hasIdType = Long.class.isAssignableFrom(parameter.getParameterType());
        return hasLoginAnnotation && hasIdType;
    }

    @Override
    public LoginInfo resolveArgument(@Nonnull MethodParameter parameter, ModelAndViewContainer mavContainer, @Nonnull NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        return LoginInfo.of(1L, "name");
    }
}
