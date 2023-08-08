package hello.board.service;

import hello.board.domain.Article;
import hello.board.domain.Comment;
import hello.board.domain.User;
import hello.board.dto.service.CommentServiceDto;
import hello.board.exception.FailToFindEntityException;
import hello.board.exception.NoAuthorityException;
import hello.board.repository.CommentRepository;
import hello.board.service.query.ArticleQueryService;
import hello.board.service.query.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;

    private final UserQueryService userQueryService;
    private final ArticleQueryService articleQueryService;

    public Long save(Long articleId, Long userId, CommentServiceDto.Save param) {
        Article article = articleQueryService.findById(articleId);
        User user = userQueryService.findById(userId);

        return commentRepository.save(param.toEntity(article, user)).getId();
    }

    public void update(Long commentId, Long articleId, Long userId, CommentServiceDto.Update param) {
        Comment comment = commentRepository.findWithArticleAndAuthorById(commentId)
                .orElseThrow(() -> FailToFindEntityException.of("comment"));

        validateUser(comment, userId);
        validateArticle(comment, articleId);

        if (param != null) {
            comment.update(param.getContent());
        }
    }

    public void delete(Long commentId, Long articleId, Long userId) {
        Comment comment = commentRepository.findWithArticleAndAuthorById(commentId)
                .orElseThrow(() -> FailToFindEntityException.of("comment"));

        validateUser(comment, userId);
        validateArticle(comment, articleId);

        comment.deleteFromArticle();
        commentRepository.delete(comment);
    }

    @Transactional(readOnly = true)
    public Comment lookUpComment(Long commentId, Long articleId) {
        Comment comment = commentRepository.findWithArticleById(commentId)
                .orElseThrow(() -> FailToFindEntityException.of("comment"));

        validateArticle(comment, articleId);
        return comment;
    }

    private static void validateArticle(Comment comment, Long articleId) {
        if (!Objects.equals(comment.getArticle().getId(), articleId)) {
            throw new IllegalArgumentException("This Article does not have this Comment");
        }
    }

    private static void validateUser(Comment comment, Long userId) {
        if (!Objects.equals(comment.getAuthor().getId(), userId)) {
            throw new NoAuthorityException();
        }
    }

}
