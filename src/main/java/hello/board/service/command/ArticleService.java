package hello.board.service.command;

import hello.board.domain.Article;
import hello.board.domain.Comment;
import hello.board.domain.User;
import hello.board.dto.service.ArticleCommentFlatDto;
import hello.board.exception.FailToFindEntityException;
import hello.board.exception.NoAuthorityException;
import hello.board.exception.WrongPageRequestException;
import hello.board.repository.ArticleRepository;
import hello.board.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

import static hello.board.dto.service.ArticleServiceDto.*;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;

    public Long save(Long userId, Save param) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> FailToFindEntityException.of("User"));

        Article article = param.toEntity(author);

        return articleRepository.save(article).getId();
    }

    public void update(Long articleId, Long userId, Update param) {
        Article article = findArticleById(articleId);

        validateUserId(article, userId);

        if (param != null) {
            article.modifyTitle(param.getTitle());
            article.modifyContent(param.getContent());
        }
    }

    public void delete(Long articleId, Long userId) {
        Article article = findArticleById(articleId);

        validateUserId(article, userId);

        articleRepository.delete(article);
    }

    public Article lookUp(Long id) {
        return articleRepository.findWithComments(id)
                .orElseThrow(() -> FailToFindEntityException.of("Article"))
                .increaseView();

    }

    public LookUp lookUp(Long id, Pageable pageable) {
        Page<ArticleCommentFlatDto> result = articleRepository.findWithComments(id, pageable);

        Article article = extractArticleFrom(result)
                .increaseView();

        Page<Comment> comments = extractCommentsFrom(result);

        return LookUp.of(article, comments);
    }

    /* ################################################## */

    private Article findArticleById(Long articleId) {
        return articleRepository.findById(articleId)
                .orElseThrow(() -> FailToFindEntityException.of("Article"));
    }

    private static void validateUserId(Article article, Long userId) throws NoAuthorityException {
        if (!article.isAuthorId(userId)) {
            throw new NoAuthorityException("You do not have authority!");
        }
    }

    private static Article extractArticleFrom(Page<ArticleCommentFlatDto> result) {
        if (result.getTotalElements() == 0) {
            throw FailToFindEntityException.of("Article");
        }

        return result.getContent().stream()
                .map(ArticleCommentFlatDto::getArticle)
                .findAny()
                .orElseThrow(() -> WrongPageRequestException.of(result.getNumber(), result.getSize()));
    }

    private static Page<Comment> extractCommentsFrom(Page<ArticleCommentFlatDto> result) {
        final boolean commentDoesNotExist = result.stream()
                .anyMatch(Objects::isNull);

        return commentDoesNotExist ? Page.empty() : result.map(ArticleCommentFlatDto::getComment);
    }
}
