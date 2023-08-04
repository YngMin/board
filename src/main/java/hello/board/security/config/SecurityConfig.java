package hello.board.security.config;

import hello.board.security.jwt.TokenAuthenticationFilter;
import hello.board.security.oauth2.handler.OAuth2AuthenticationFailureHandler;
import hello.board.security.oauth2.handler.OAuth2AuthenticationSuccessHandler;
import hello.board.security.oauth2.repository.CookieAuthorizationRequestRepository;
import hello.board.security.oauth2.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.toH2Console;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;
import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";

    private final CustomOAuth2UserService oAuth2UserService;
    private final TokenAuthenticationFilter tokenAuthenticationFilter;
    private final CookieAuthorizationRequestRepository cookieAuthorizationRequestRepository;
    private final OAuth2AuthenticationSuccessHandler successHandler;
    private final OAuth2AuthenticationFailureHandler failureHandler;

    @Bean
    @Profile("local")
    public WebSecurityCustomizer configure() {
        return (web) -> web.ignoring()
                .requestMatchers(toH2Console())
                .requestMatchers(antMatcher("/img/**"), antMatcher("/css/**"), antMatcher("/js/**"));
    }

    @Bean
    @Profile("local")
    public SecurityFilterChain filterChain(HttpSecurity http) {
        try {
            return http
                    .httpBasic(AbstractHttpConfigurer::disable)
                    .csrf(AbstractHttpConfigurer::disable)
                    .formLogin(AbstractHttpConfigurer::disable)
                    .rememberMe(AbstractHttpConfigurer::disable)
                    .sessionManagement(config -> config
                            .sessionCreationPolicy(STATELESS)
                    )
                    .authorizeHttpRequests(request -> request
                            .requestMatchers(antMatcher("/api/token")).permitAll()
                            .requestMatchers(antMatcher("/api/**")).authenticated()
                            .anyRequest().permitAll()
                    )
                    .oauth2Login(login -> login
                                    .loginPage("/login")
                                    .authorizationEndpoint(
                                            config -> config
                                                    .baseUri("/oauth2/authorization")
                                                    .authorizationRequestRepository(cookieAuthorizationRequestRepository)
                                    )
                                    .userInfoEndpoint(config -> config
                                            .userService(oAuth2UserService)
                                    )
                                    .successHandler(successHandler)
                    )
                    .logout(logout -> logout
                            .logoutSuccessUrl("/login")
                    )
                    .addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                    .exceptionHandling(config -> config
                            .defaultAuthenticationEntryPointFor(
                                    new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                    antMatcher("/api/**")
                                    )
                    )
                    .build();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
