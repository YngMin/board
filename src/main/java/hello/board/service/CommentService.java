package hello.board.service;

import hello.board.domain.Article;
import hello.board.domain.Comment;
import hello.board.domain.User;
import hello.board.dto.service.CommentServiceDto;
import hello.board.exception.NoAuthorityException;
import hello.board.repository.CommentRepository;
import hello.board.service.query.ArticleQueryService;
import hello.board.service.query.CommentQueryService;
import hello.board.service.query.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;

    private final UserQueryService userQueryService;
    private final ArticleQueryService articleQueryService;
    private final CommentQueryService commentQueryService;

    public Long save(Long articleId, Long userId, CommentServiceDto.Save param) {
        Article article = articleQueryService.findById(articleId);
        User user = userQueryService.findById(userId);

        return commentRepository.save(param.toEntity(article, user)).getId();
    }

    public void update(Long commentId, Long articleId, Long userId, CommentServiceDto.Update param) {

        Comment comment = commentQueryService.findById(commentId);

        validateUser(comment, userId);
        validateArticle(comment, articleId);

        if (param != null) {
            comment.update(param.getContent());
        }
    }

    public void delete(Long commentId, Long articleId, Long userId) {
        Comment comment = commentQueryService.findById(commentId);

        validateUser(comment, userId);
        validateArticle(comment, articleId);

        comment.deleteFromArticle();
        commentRepository.delete(comment);
    }

    @Transactional(readOnly = true)
    public Comment lookUpComment(Long commentId, Long articleId) {
        Comment comment = commentQueryService.findById(commentId);
        validateArticle(comment, articleId);
        return comment;
    }

    private void validateArticle(Comment comment, Long articleId) {
        Article article = articleQueryService.findById(articleId);

        if (comment.getArticle() != article) {
            throw new IllegalArgumentException("This Article does not have this Comment");
        }
    }

    private void validateUser(Comment comment, Long userId) {
        User user = userQueryService.findById(userId);

        if (comment.getAuthor() != user) {
            throw new NoAuthorityException();
        }
    }

}
