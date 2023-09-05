package hello.board.service.command;

import hello.board.domain.Article;
import hello.board.domain.Comment;
import hello.board.domain.User;
import hello.board.exception.FailToFindEntityException;
import hello.board.exception.NoAuthorityException;
import hello.board.repository.ArticleRepository;
import hello.board.repository.CommentRepository;
import hello.board.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

import static hello.board.dto.service.CommentServiceDto.Save;
import static hello.board.dto.service.CommentServiceDto.Update;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;

    public Long save(Long articleId, Long userId, Save param) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> FailToFindEntityException.of("Article"));

        User author = userRepository.findById(userId)
                .orElseThrow(() -> FailToFindEntityException.of("User"));

        return commentRepository.save(param.toEntity(article, author)).getId();
    }

    public void update(Long commentId, Long articleId, Long userId, Update param) {
        Comment comment = findAndValidate(commentId, articleId, userId);

        if (param != null) {
            comment.update(param.getContent());
        }
    }

    public void delete(Long commentId, Long articleId, Long userId) {
        Comment comment = findAndValidate(commentId, articleId, userId);

        comment.deleteFromArticle();
        commentRepository.delete(comment);
    }

    /* ################################################### */

    private Comment findAndValidate(Long commentId, Long articleId, Long userId) {
        Comment comment = commentRepository.findWithArticleById(commentId)
                .orElseThrow(() -> FailToFindEntityException.of("Comment"));

        validateArticle(comment, articleId);
        validateAuthor(comment, userId);
        return comment;
    }

    private static void validateArticle(Comment comment, Long articleId) {
        if (!Objects.equals(comment.getArticle().getId(), articleId)) {
            throw new IllegalArgumentException("This Article does not have this Comment");
        }
    }

    private static void validateAuthor(Comment comment, Long userId) {
        if (!Objects.equals(comment.getAuthor().getId(), userId)) {
            throw new NoAuthorityException("You do not have authority!");
        }
    }

}
