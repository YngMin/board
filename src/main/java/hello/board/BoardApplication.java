package hello.board;

import hello.board.repository.ArticleRepository;
import hello.board.repository.CommentRepository;
import hello.board.repository.UserRepository;
import hello.board.util.TimeTraceAop;
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
    static class Config {
        @Profile("local")
        @Bean
        public TestDataInit testDataInit(UserRepository userRepository, ArticleRepository articleRepository, CommentRepository commentRepository, PasswordEncoder passwordEncoder) {
            return new TestDataInit(userRepository, articleRepository, commentRepository, passwordEncoder);
        }

        @Profile({"local", "test"})
        @Bean
        public TimeTraceAop timeTraceAop() {
            return new TimeTraceAop();
        }
    }


}
