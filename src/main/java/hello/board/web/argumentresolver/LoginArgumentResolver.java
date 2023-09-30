package hello.board.web.argumentresolver;

import hello.board.web.annotation.Login;
import hello.board.web.user.LoginInfo;
import hello.board.web.user.UserDetailsImpl;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Slf4j
@RequiredArgsConstructor
public class LoginArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean hasLoginAnnotation = parameter.hasParameterAnnotation(Login.class);
        boolean hasLoginInfoType = LoginInfo.class.isAssignableFrom(parameter.getParameterType());

        return hasLoginAnnotation && hasLoginInfoType;
    }

    @Override
    public LoginInfo resolveArgument(@Nonnull MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {

        Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();

        if (authentication == null) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        return (principal instanceof UserDetailsImpl userDetails)
                ? LoginInfo.of(userDetails.getUserId(), userDetails.getName())
                : null;
    }
}
