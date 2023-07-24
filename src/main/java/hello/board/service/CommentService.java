package hello.board.service;

import hello.board.domain.Article;
import hello.board.domain.Comment;
import hello.board.domain.User;
import hello.board.dto.service.CommentServiceDto;
import hello.board.repository.ArticleRepository;
import hello.board.repository.CommentRepository;
import hello.board.repository.UserRepository;
import hello.board.service.exception.NoAuthorityException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentService {

    private final ArticleRepository articleRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    @Transactional
    public Long save(Long articleId, Long userId, CommentServiceDto.Save param) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(IllegalArgumentException::new);

        User user = userRepository.findById(userId)
                .orElseThrow(IllegalArgumentException::new);

        return commentRepository.save(param.toEntity(article, user)).getId();
    }

    public Comment findById(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(IllegalArgumentException::new);
    }

    public Comment findComment(Long commentId, Long articleId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(IllegalArgumentException::new);

        validateArticle(comment, articleId);
        return comment;
    }

    public List<Comment> findCommentsOfArticle(Long articleId) {
        return commentRepository.findCommentByArticleId(articleId);
    }

    @Transactional
    public void update(Long commentId, Long articleId, Long userId, CommentServiceDto.Update param) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(IllegalArgumentException::new);

        validateUser(comment, userId);
        validateArticle(comment, articleId);

        if (param != null) {
            comment.update(param.getContent());
        }
    }

    @Transactional
    public void delete(Long commentId, Long articleId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(IllegalArgumentException::new);

        validateUser(comment, userId);
        validateArticle(comment, articleId);

        comment.deleteFromArticle();
        commentRepository.delete(comment);
    }

    private void validateArticle(Comment comment, Long articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(IllegalArgumentException::new);

        if (comment.getArticle() != article) {
            throw new IllegalStateException();
        }
    }

    private void validateUser(Comment comment, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(IllegalArgumentException::new);

        if (comment.getAuthor() != user) {
            throw new NoAuthorityException();
        }
    }

}
