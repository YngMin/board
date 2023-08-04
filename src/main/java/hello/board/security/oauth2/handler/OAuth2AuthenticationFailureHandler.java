package hello.board.security.oauth2.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {

        if (response.isCommitted()) {
            log.debug("Response has already been committed.");
        } else {
            try {
                getRedirectStrategy().sendRedirect(request, response, "/login");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        super.onAuthenticationFailure(request, response, exception);
    }
}
