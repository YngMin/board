package hello.board.service.command;

import hello.board.domain.Article;
import hello.board.domain.Comment;
import hello.board.domain.User;
import hello.board.exception.NoAuthorityException;
import hello.board.repository.ArticleRepository;
import hello.board.service.query.ArticleQueryService;
import hello.board.service.query.CommentQueryService;
import hello.board.service.query.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static hello.board.dto.service.ArticleServiceDto.*;

@Service
@Transactional
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;

    private final UserQueryService userQueryService;
    private final ArticleQueryService articleQueryService;
    private final CommentQueryService commentQueryService;


    public Long save(Long userId, Save param) {
        User user = userQueryService.findById(userId);
        Article article = param.toEntity(user);

        return articleRepository.save(article).getId();
    }

    public void update(Long articleId, Long userId, Update param) {
        Article article = articleQueryService.findById(articleId);
        User user = userQueryService.findById(userId);

        validateAuthor(article, user);

        if (param != null) {
            article.update(param.getTitle(), param.getContent());
        }
    }

    public void delete(Long articleId, Long userId) {
        Article article = articleQueryService.findById(articleId);
        User user = userQueryService.findById(userId);

        validateAuthor(article, user);

        articleRepository.delete(article);
    }

    public Article lookUpWithAllComments(Long id) {
        return articleQueryService.findWithComments(id)
                .increaseView();
    }

    public LookUp lookUpWithPaginatedComments(Long id, int page, int size) {
        Page<Comment> comments = commentQueryService.findCommentsWithArticle(id, PageRequest.of(page, size));

        Article article = comments.getContent().stream()
                .map(Comment::getArticle)
                .findAny()
                .orElse(articleQueryService.findById(id))
                .increaseView();

        return LookUp.from(article, comments);
    }

    /* ################################################## */

    private static void validateAuthor(Article article, User user) throws NoAuthorityException {
        if (article.getAuthor() != user) {
            throw new NoAuthorityException("You cannot modify what someone else has written");
        }
    }
}
