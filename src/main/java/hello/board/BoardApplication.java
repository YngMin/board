package hello.board;

import hello.board.repository.CommentRepository;
import hello.board.repository.UserRepository;
import hello.board.repository.article.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
@RequiredArgsConstructor
public class BoardApplication {

    UserRepository userRepository;
    ArticleRepository articleRepository;
    CommentRepository commentRepository;

    public static void main(String[] args) {
        SpringApplication.run(BoardApplication.class, args);
    }

    @Profile("local")
    @Bean
    public TestDataInit testDataInit() {
        return new TestDataInit(userRepository, articleRepository, commentRepository);
    }

}
