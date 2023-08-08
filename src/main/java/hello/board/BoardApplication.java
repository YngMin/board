package hello.board;

import hello.board.repository.CommentRepository;
import hello.board.repository.UserRepository;
import hello.board.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.crypto.password.PasswordEncoder;

@EnableJpaAuditing
@SpringBootApplication
public class BoardApplication {

    public static void main(String[] args) {
        SpringApplication.run(BoardApplication.class, args);
    }

    @Configuration
    @RequiredArgsConstructor
    static class Config {

        private final UserRepository userRepository;
        private final ArticleRepository articleRepository;
        private final CommentRepository commentRepository;
        private final PasswordEncoder passwordEncoder;

        @Profile("local")
        @Bean
        public TestDataInit testDataInit() {
            return new TestDataInit(userRepository, articleRepository, commentRepository, passwordEncoder);
        }
    }


}
