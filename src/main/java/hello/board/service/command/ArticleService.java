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
    private final UserRepository userRepository;

    public Long save(Long userId, Save param) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> FailToFindEntityException.of("User"));

        Article article = param.toEntity(user);

        return articleRepository.save(article).getId();
    }

    public void update(Long articleId, Long userId, Update param) {
        Article article = articleRepository.findById(articleId)
                        .orElseThrow(() -> FailToFindEntityException.of("Article"));

        validateAuthor(article, userId);

        if (param != null) {
            article.update(param.getTitle(), param.getContent());
        }
    }

    public void delete(Long articleId, Long userId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> FailToFindEntityException.of("Article"));

        validateAuthor(article, userId);

        articleRepository.delete(article);
    }

    public Article lookUp(Long id) {
        return articleRepository.findWithComments(id)
                .orElseThrow(() -> FailToFindEntityException.of("Article"))
                .increaseView();

    }

    public LookUp lookUp(Long id, int page, int size) {

        validatePageRequest(page, size);

        Page<ArticleCommentFlatDto> result = articleRepository.findWithComments(id, PageRequest.of(page, size));

        Article article =
                extractArticleFrom(result)
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

    private static void validatePageRequest(int page, int size) {
        if (page < 0 || size < 1) {
            throw WrongPageRequestException.of(page, size);
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
