package hello.board.domain.util;

import hello.board.domain.Article;
import hello.board.domain.Comment;
import hello.board.domain.User;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EntityReflectionUtils {

    public static User createUserByReflection(String name, String email, String password) {
        try {
            Class<?> userClass = Class.forName("hello.board.domain.User");
            Constructor<?> constructor = userClass.getDeclaredConstructor(String.class, String.class, String.class);
            constructor.setAccessible(true);
            return (User) constructor.newInstance(name, email, password);

        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public static Article createArticleByReflection(String title, String content, User author) {

        try {
            Class<?> articleClass = Class.forName("hello.board.domain.Article");
            Constructor<?> constructor = articleClass.getDeclaredConstructor(String.class, String.class, User.class);
            constructor.setAccessible(true);
            return (Article) constructor.newInstance(title, content, author);

        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }

    }

    public static Comment createCommentByReflection(String content, Article article, User author) {
        try {
            Class<?> commentClass = Class.forName("hello.board.domain.Comment");
            Constructor<?> constructor = commentClass.getDeclaredConstructor(String.class, Article.class, User.class);
            constructor.setAccessible(true);
            return (Comment) constructor.newInstance(content,article , author);

        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setIdOfUser(User user, Long id) {
        try {
            Class<?> userClass = Class.forName("hello.board.domain.User");

            Field fieldName = userClass.getDeclaredField("id");
            fieldName.setAccessible(true);
            fieldName.set(user, id);

        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setIdOfArticle(Article article, Long id) {
        try {
            Class<?> articleClass = Class.forName("hello.board.domain.Article");

            Field fieldName = articleClass.getDeclaredField("id");
            fieldName.setAccessible(true);
            fieldName.set(article, id);

        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
