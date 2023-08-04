package hello.board.service;

import hello.board.domain.Article;
import hello.board.domain.User;
import hello.board.dto.service.ArticleServiceDto;
import hello.board.exception.FailToFindEntityException;
import hello.board.exception.NoAuthorityException;
import hello.board.repository.article.ArticleRepository;
import hello.board.service.query.ArticleQueryService;
import hello.board.service.query.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;

    private final UserQueryService userQueryService;
    private final ArticleQueryService articleQueryService;

    public Long save(Long userId, ArticleServiceDto.Save param) {
        User user = userQueryService.findById(userId);
        Article article = param.toEntity(user);
        return articleRepository.save(article).getId();
    }

    public void update(Long articleId, Long userId, ArticleServiceDto.Update param) {
        Article article = validateUser(articleId, userId);
        if (param != null) {
            article.update(param.getTitle(), param.getContent());
        }
    }

    public void delete(Long articleId, Long userId) {
        Article article = validateUser(articleId, userId);
        articleRepository.delete(article);
    }

    public Article lookUpArticle(Long id) {
        return articleRepository.findArticleWithComments(id)
                .orElseThrow(() -> FailToFindEntityException.of("Article"))
                .increaseView();
    }


    /* ################################# */

    private Article validateUser(Long articleId, Long userId) throws NoAuthorityException {
        User user = userQueryService.findById(userId);
        Article article = articleQueryService.findById(articleId);

        if (article.getAuthor() != user) {
            throw new NoAuthorityException("You cannot modify what someone else has written");
        }
        return article;
    }
}
