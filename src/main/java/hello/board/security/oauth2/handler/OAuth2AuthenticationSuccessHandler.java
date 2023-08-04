package hello.board.security.oauth2.handler;

import hello.board.domain.RefreshToken;
import hello.board.domain.User;
import hello.board.repository.RefreshTokenRepository;
import hello.board.security.config.SecurityConfig;
import hello.board.security.jwt.TokenProvider;
import hello.board.security.oauth2.repository.CookieAuthorizationRequestRepository;
import hello.board.service.query.UserQueryService;
import hello.board.util.CookieUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.WebUtils;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;

import static hello.board.security.oauth2.repository.CookieAuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME;

@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";
    public static final Duration REFRESH_TOKEN_DURATION = Duration.ofDays(14);
    public static final Duration ACCESS_TOKEN_DURATION = Duration.ofDays(1);

    @Value("${spring.security.oauth2.authorizedRedirectUri}")
    private String REDIRECT_PATH;

    private final UserQueryService userQueryService;
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CookieAuthorizationRequestRepository authorizationRequestRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {

        User user = getUser(authentication);

        String refreshToken = tokenProvider.generateToken(user, REFRESH_TOKEN_DURATION);
        saveRefreshToken(user, refreshToken);
        addRefreshTokenToCookie(request, response, refreshToken);

        String accessToken = tokenProvider.generateToken(user, ACCESS_TOKEN_DURATION);
        String targetUrl = getTargetUrl(request, accessToken);

        if (response.isCommitted()) {
            log.debug("Response has already been committed.");
        } else {
            clearAuthenticationAttributes(request, response);
            try {
//                response.addHeader(SecurityConfig.HEADER_AUTHORIZATION, accessToken);
                getRedirectStrategy().sendRedirect(request, response, targetUrl);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    private User getUser(Authentication authentication) {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        return userQueryService.findByEmail(oAuth2User.getAttribute("email"));
    }

    private void saveRefreshToken(User user, String refreshToken) {
        RefreshToken newRefreshToken = refreshTokenRepository.findByUserId(user.getId())
                .map(tkn -> tkn.update(refreshToken))
                .orElse(RefreshToken.create(user.getId(), refreshToken));

        refreshTokenRepository.save(newRefreshToken);
    }

    private void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        authorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
    }

    private String getTargetUrl(HttpServletRequest request, String token) {

        Cookie cookie = WebUtils.getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME);

        String targetUrl = cookie == null || cookie.getValue() == null
                ? REDIRECT_PATH
                : validateRedirectUri(cookie.getValue());

        return UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("token", token)
                .build()
                .toUriString();
    }

    private static void addRefreshTokenToCookie(HttpServletRequest request, HttpServletResponse response, String refreshToken) {
        int cookieMaxAge = (int) REFRESH_TOKEN_DURATION.toSeconds();
        CookieUtils.deleteCookie(request, response, REFRESH_TOKEN_COOKIE_NAME);
        CookieUtils.addCookie(response, REFRESH_TOKEN_COOKIE_NAME, refreshToken, cookieMaxAge);
    }

    private String validateRedirectUri(String uri) {
        URI clientRedirectUri = URI.create(uri);
        URI authorizedUri = URI.create(REDIRECT_PATH);

        if (authorizedUri.getHost().equalsIgnoreCase(clientRedirectUri.getHost())
                && authorizedUri.getPort() == clientRedirectUri.getPort()) {
            return uri;
        }
        return REDIRECT_PATH;
    }


}
