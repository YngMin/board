package hello.board.service.command;

import hello.board.domain.Article;
import hello.board.domain.Comment;
import hello.board.domain.User;
import hello.board.dto.service.ArticleCommentFlatDto;
import hello.board.exception.FailToFindEntityException;
import hello.board.exception.NoAuthorityException;
import hello.board.repository.ArticleRepository;
import hello.board.service.query.ArticleQueryService;
import hello.board.service.query.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

import static hello.board.dto.service.ArticleServiceDto.*;

@Service
@Transactional
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;

    private final UserQueryService userQueryService;
    private final ArticleQueryService articleQueryService;

    public Long save(Long userId, Save param) {
        User user = userQueryService.findById(userId);
        Article article = param.toEntity(user);

        return articleRepository.save(article).getId();
    }

    public void update(Long articleId, Long userId, Update param) {
        Article article = articleQueryService.findById(articleId);

        validateAuthor(article, userId);

        if (param != null) {
            article.update(param.getTitle(), param.getContent());
        }
    }

    public void delete(Long articleId, Long userId) {
        Article article = articleQueryService.findById(articleId);

        validateAuthor(article, userId);

        articleRepository.delete(article);
    }

    public Article lookUp(Long id) {
        return articleQueryService.findWithComments(id)
                .increaseView();
    }

    public LookUp lookUp(Long id, int page, int size) {
        Page<ArticleCommentFlatDto> result = articleRepository.findWithComments(id, PageRequest.of(page, size));

        Article article = extractArticleFrom(result)
                .increaseView();

        Page<Comment> comments = extractCommentsFrom(result);

        return LookUp.from(article, comments);
    }

    /* ################################################## */

    private static void validateAuthor(Article article, Long userId) throws NoAuthorityException {
        if (!Objects.equals(article.getAuthor().getId(), userId)) {
            throw new NoAuthorityException("You do not have authority!");
        }
    }

    private static Article extractArticleFrom(Page<ArticleCommentFlatDto> result) {
        return result.getContent().stream()
                .map(ArticleCommentFlatDto::getArticle)
                .findAny()
                .orElseThrow(() -> FailToFindEntityException.of("Article"));
    }

    private static Page<Comment> extractCommentsFrom(Page<ArticleCommentFlatDto> result) {
        boolean noComment = result.stream()
                .anyMatch(dto -> dto.getComment() == null);

        return noComment ? Page.empty() : result.map(ArticleCommentFlatDto::getComment);
    }
}
