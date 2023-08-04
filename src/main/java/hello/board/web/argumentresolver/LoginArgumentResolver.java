package hello.board.web.argumentresolver;

import hello.board.exception.NeedLoginException;
import hello.board.security.jwt.TokenProvider;
import hello.board.web.annotation.Login;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Slf4j
@RequiredArgsConstructor
public class LoginArgumentResolver implements HandlerMethodArgumentResolver {

    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    private final TokenProvider tokenProvider;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean hasLoginAnnotation = parameter.hasParameterAnnotation(Login.class);
        boolean hasLongType = Long.class.isAssignableFrom(parameter.getParameterType());

        return hasLoginAnnotation && hasLongType;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {

        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();

        String header = request.getHeader(HEADER_AUTHORIZATION);
        String token = getAccessToken(header);

        return tokenProvider.getUserId(token);
    }

    private String getAccessToken(String authorizationHeader) {
        if (doesNotStartWithPrefix(authorizationHeader)) {
            return null;
        }
        return authorizationHeader.substring(TOKEN_PREFIX.length());
    }

    private static boolean doesNotStartWithPrefix(String authorizationHeader) {
        return authorizationHeader == null || !authorizationHeader.startsWith(TOKEN_PREFIX);
    }

    private static Long loginCheck(Long userId) {
        if (userId == null) {
            throw new NeedLoginException("Need Login");
        }

        return userId;
    }
}
