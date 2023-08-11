package hello.board;

import hello.board.domain.Article;
import hello.board.domain.Comment;
import hello.board.domain.User;
import hello.board.repository.CommentRepository;
import hello.board.repository.UserRepository;
import hello.board.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Transactional
@RequiredArgsConstructor
public class TestDataInit {

    private static final int NUM_OF_ARTICLES = 100;

    private final UserRepository userRepository;
    private final ArticleRepository articleRepository;
    private final CommentRepository commentRepository;

    private final PasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    public void initData() {
        User user = User.create("Test", "test@board.com", passwordEncoder.encode("1234"));
        userRepository.save(user);

        List<User> users = saveUsers();

        List<Article> articles = saveArticles(users);

        int numComments = 1;

        for (Article article : articles) {
            saveComments(article, users, numComments++);
        }
    }

    private List<User> saveUsers() {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < 31; i++) {
            users.add(getUser());
        }
        return users;
    }

    private User getUser() {
        User user = User.create(createRandomString(), createRandomString(), passwordEncoder.encode(createRandomString()));
        return userRepository.save(user);
    }

    private static String createRandomString() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private List<Article> saveArticles(List<User> users) {

        List<Article> articles = new ArrayList<>();

        for (int i = 0; i < NUM_OF_ARTICLES; i++) {
            Article article = Article.create("title: " + i, "content: " + i, users.get(i % 31));
            articles.add(article);
        }

        articleRepository.saveAll(articles);

        return articles;
    }

    private void saveComments(Article article, List<User> users, int numComments) {

        for (int i = 0; i < numComments; i++) {
            Comment comment = Comment.create("comment " + i, article, users.get(i % 31));
            commentRepository.save(comment);
        }
    }
}
