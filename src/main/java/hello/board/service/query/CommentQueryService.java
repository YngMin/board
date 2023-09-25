package hello.board.service.query;

import hello.board.domain.Comment;
import hello.board.exception.FailToFindEntityException;
import hello.board.repository.ArticleRepository;
import hello.board.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentQueryService {

    private final ArticleRepository articleRepository;
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

    public Page<Comment> findByArticleId(Long articleId, Pageable pageable) {
        if (isWrongArticleId(articleId)) {
            throw new IllegalArgumentException("wrong article id: " + articleId);
        }
        return commentRepository.findByArticleId(articleId, pageable);
    }



    /* ################################################## */

    private boolean isWrongArticleId(Long articleId) {
        return !articleRepository.existsById(articleId);
    }

    private static void validateArticle(Comment comment, Long articleId) {
        if (comment.isNotMyArticleId(articleId)) {
            throw new IllegalArgumentException("wrong article id: " + articleId);
        }
    }


}
