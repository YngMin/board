package hello.board.security.config;

import hello.board.security.jwt.JwtProperties;
import hello.board.security.jwt.TokenAuthenticationFilter;
import hello.board.security.jwt.TokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class JwtConfig {

    @Bean
    @Profile("local")
    public JwtProperties jwtProperties() {
        return new JwtProperties();
    }

    @Bean
    @Profile("local")
    public TokenProvider tokenProvider() {
        return new TokenProvider(jwtProperties());
    }

    @Bean
    @Profile("local")
    public TokenAuthenticationFilter tokenAuthenticationFilter() {
        return new TokenAuthenticationFilter(tokenProvider());
    }
}
