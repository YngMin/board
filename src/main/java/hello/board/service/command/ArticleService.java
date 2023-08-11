package hello.board.service.command;

import hello.board.domain.Article;
import hello.board.domain.Comment;
import hello.board.domain.User;
import hello.board.dto.service.ArticleServiceDto;
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

@Service
@Transactional
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;

    private final UserQueryService userQueryService;
    private final ArticleQueryService articleQueryService;
    private final CommentQueryService commentQueryService;


    public Long save(Long userId, ArticleServiceDto.Save param) {
        User user = userQueryService.findById(userId);
        Article article = param.toEntity(user);

        return articleRepository.save(article).getId();
    }

    public void update(Long articleId, Long userId, ArticleServiceDto.Update param) {
        Article article = articleQueryService.findById(articleId);
        User user = userQueryService.findById(userId);

        validateUser(article, user);

        if (param != null) {
            article.update(param.getTitle(), param.getContent());
        }
    }

    public void delete(Long articleId, Long userId) {
        Article article = articleQueryService.findById(articleId);
        User user = userQueryService.findById(userId);

        validateUser(article, user);

        articleRepository.delete(article);
    }

    public Article lookUpNoPaging(Long id) {
        return articleQueryService.findWithComments(id)
                .increaseView();
    }

    public ArticleServiceDto.LookUp lookUpWithPaginatedCommentsOld(Long id, int page, int size) {
        Article article = articleQueryService.findById(id)
                .increaseView();

        Page<Comment> comments = commentQueryService.findByArticle(article, PageRequest.of(page, size));

        return ArticleServiceDto.LookUp.from(article, comments);
    }

    public ArticleServiceDto.LookUp lookUpWithPaginatedComments(Long id, int page, int size) {
        Page<Comment> comments = commentQueryService.findCommentsWithArticle(id, PageRequest.of(page, size));

        Article article = comments.getContent().stream()
                .map(Comment::getArticle)
                .findAny()
                .orElse(articleQueryService.findById(id))
                .increaseView();

        return ArticleServiceDto.LookUp.from(article, comments);
    }

    /* ################################# */

    private static void validateUser(Article article, User user) throws NoAuthorityException {
        if (article.getAuthor() != user) {
            throw new NoAuthorityException("You cannot modify what someone else has written");
        }
    }
}
