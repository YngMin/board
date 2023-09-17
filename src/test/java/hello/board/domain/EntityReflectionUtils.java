package hello.board.domain;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.reflect.Field;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EntityReflectionUtils {

    public static User createUserByReflection(String name, String email, String password) {
        try {
            User user = new User();
            Class<?> userClass = Class.forName("hello.board.domain.User");

            Field fieldName = userClass.getDeclaredField("name");
            fieldName.setAccessible(true);
            fieldName.set(user, name);

            Field fieldEmail = userClass.getDeclaredField("email");
            fieldEmail.setAccessible(true);
            fieldEmail.set(user, email);

            Field fieldPassword = userClass.getDeclaredField("password");
            fieldPassword.setAccessible(true);
            fieldPassword.set(user, password);

            return user;

        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static Article createArticleByReflection(String title, String content, User author) {

        try {
            Article article = new Article();
            Class<?> articleClass = Class.forName("hello.board.domain.Article");

            Field fieldTitle = articleClass.getDeclaredField("title");
            fieldTitle.setAccessible(true);
            fieldTitle.set(article, title);

            Field fieldContent = articleClass.getDeclaredField("content");
            fieldContent.setAccessible(true);
            fieldContent.set(article, content);

            Field fieldAuthor = articleClass.getDeclaredField("author");
            fieldAuthor.setAccessible(true);
            fieldAuthor.set(article, author);

            return article;
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

    }

    public static Comment createCommentByReflection(String content, Article article, User author) {
        try {
            Comment comment = new Comment();
            Class<?> commentClass = Class.forName("hello.board.domain.Comment");

            Field fieldContent = commentClass.getDeclaredField("content");
            fieldContent.setAccessible(true);
            fieldContent.set(comment, content);

            Field fieldArticle = commentClass.getDeclaredField("article");
            fieldArticle.setAccessible(true);
            fieldArticle.set(comment, article);

            Field fieldAuthor = commentClass.getDeclaredField("author");
            fieldAuthor.setAccessible(true);
            fieldAuthor.set(comment, author);

            return comment;
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
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
