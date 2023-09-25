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

        Comment comment = param.toEntity(article, author);

        return commentRepository.save(comment).getId();
    }

    public void update(Long commentId, Long articleId, Long userId, Update param) {
        if (param != null) {
            Comment comment = findCommentById(commentId);

            validateArticleId(comment, articleId);
            validateUserId(comment, userId);

            comment.modifyContent(param.getContent());
        }
    }

    public void delete(Long commentId, Long articleId, Long userId) {
        Comment comment = findCommentById(commentId);

        validateArticleId(comment, articleId);
        validateUserId(comment, userId);

        comment.deleteFromArticle();
        commentRepository.delete(comment);
    }

    /* ################################################### */

    private Comment findCommentById(Long commentId) {
        return commentRepository.findWithArticleById(commentId)
                .orElseThrow(() -> FailToFindEntityException.of("Comment"));
    }

    private static void validateArticleId(Comment comment, Long articleId) {
        if (comment.isNotMyArticleId(articleId)) {
            throw new IllegalArgumentException("This Article does not have this Comment");
        }
    }

    private static void validateUserId(Comment comment, Long userId) {
        if (comment.isNotAuthorId(userId)) {
            throw new NoAuthorityException("You do not have authority!");
        }
    }

}
