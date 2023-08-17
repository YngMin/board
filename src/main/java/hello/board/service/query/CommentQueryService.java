package hello.board.service.query;

import hello.board.domain.Comment;
import hello.board.exception.FailToFindEntityException;
import hello.board.exception.WrongPageRequestException;
import hello.board.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentQueryService {

    private final CommentRepository commentRepository;

    public Comment findById(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> FailToFindEntityException.of("Comment"));
    }

    public Comment findWithArticle(Long commentId, Long articleId) {
        Comment comment = commentRepository.findWithArticleById(commentId)
                        .orElseThrow(() -> FailToFindEntityException.of("Comment"));

        validateArticle(comment, articleId);

        return comment;
    }

    public Page<Comment> findByArticleId(Long articleId, int page, int size) {
        validatePageRequest(page, size);
        return commentRepository.findByArticleId(articleId, PageRequest.of(page, size));
    }

    /* ################################################## */

    private static void validateArticle(Comment comment, Long articleId) {
        if (!Objects.equals(comment.getArticle().getId(), articleId)) {
            throw new IllegalArgumentException("This Article does not have this Comment");
        }
    }

    private static void validatePageRequest(int page, int size) {
        if (page < 0 || size < 1) {
            throw WrongPageRequestException.of(page, size);
        }
    }
}
