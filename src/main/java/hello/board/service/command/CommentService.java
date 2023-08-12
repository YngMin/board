package hello.board.service.command;

import hello.board.domain.Article;
import hello.board.domain.Comment;
import hello.board.domain.User;
import hello.board.exception.NoAuthorityException;
import hello.board.repository.CommentRepository;
import hello.board.service.query.ArticleQueryService;
import hello.board.service.query.CommentQueryService;
import hello.board.service.query.UserQueryService;
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

    private final UserQueryService userQueryService;
    private final ArticleQueryService articleQueryService;
    private final CommentQueryService commentQueryService;

    public Long save(Long articleId, Long userId, Save param) {
        Article article = articleQueryService.findById(articleId);
        User author = userQueryService.findById(userId);

        return commentRepository.save(param.toEntity(article, author)).getId();
    }

    public void update(Long commentId, Long articleId, Long userId, Update param) {
        Comment comment = commentQueryService.findWithArticle(commentId, articleId);

        validateAuthor(comment, userId);

        if (param != null) {
            comment.update(param.getContent());
        }
    }

    public void delete(Long commentId, Long articleId, Long userId) {
        Comment comment = commentQueryService.findWithArticle(commentId, articleId);

        validateAuthor(comment, userId);

        comment.deleteFromArticle();
        commentRepository.delete(comment);
    }

    /* ################################################### */

    private static void validateAuthor(Comment comment, Long userId) {
        if (!Objects.equals(comment.getAuthor().getId(), userId)) {
            throw new NoAuthorityException("You do not have authority!");
        }
    }

}
