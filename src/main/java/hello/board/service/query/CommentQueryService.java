package hello.board.service.query;

import hello.board.domain.Article;
import hello.board.domain.Comment;
import hello.board.exception.FailToFindEntityException;
import hello.board.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentQueryService {

    private final ArticleQueryService articleQueryService;
    private final CommentRepository commentRepository;

    public Comment findById(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> FailToFindEntityException.of("Comment"));
    }

    public Comment getCommentForModifying(Long commentId, Long articleId) {
        Comment comment = this.findById(commentId);
        validateArticle(comment, articleId);
        return comment;
    }

    private void validateArticle(Comment comment, Long articleId) {
        Article article = articleQueryService.findById(articleId);

        if (comment.getArticle() != article) {
            throw new IllegalArgumentException("This Article does not have this Comment");
        }
    }

    public List<Comment> findCommentsOfArticle(Long articleId) {
        return commentRepository.findCommentByArticleId(articleId);
    }
}
