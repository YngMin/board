package hello.board.web.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Objects;

import static jakarta.servlet.http.HttpServletResponse.SC_BAD_REQUEST;

public class UserJoinHttpStatusInterceptor implements HandlerInterceptor {
    @Override
    public void postHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler, ModelAndView modelAndView) {
        if (isPost(request) && returnToJoinView(modelAndView)) {
            response.setStatus(SC_BAD_REQUEST);
        }

    }

    private static boolean returnToJoinView(ModelAndView modelAndView) {
        return Objects.equals(modelAndView.getViewName(), "login/joinForm");
    }

    private static boolean isPost(HttpServletRequest request) {
        return request.getMethod().equalsIgnoreCase("POST");
    }
}
