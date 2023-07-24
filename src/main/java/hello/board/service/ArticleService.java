package hello.board.service;

import hello.board.domain.Article;
import hello.board.domain.Comment;
import hello.board.domain.User;
import hello.board.dto.service.ArticleCommentsDto;
import hello.board.dto.service.ArticleServiceDto;
import hello.board.repository.ArticleRepository;
import hello.board.repository.CommentRepository;
import hello.board.repository.UserRepository;
import hello.board.service.exception.NoAuthorityException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public Long save(Long userId, ArticleServiceDto.Save param) {
        User user = userRepository.findById(userId)
                .orElseThrow(IllegalArgumentException::new);

        Article article = param.toEntity(user);
        return articleRepository.save(article).getId();
    }

    public Article findById(Long id) {
        return articleRepository.findById(id)
                .orElseThrow(IllegalArgumentException::new);
    }

    public ArticleCommentsDto findByIdWithComments(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(IllegalArgumentException::new);
        List<Comment> comments = commentRepository.findCommentByArticleId(id);

        return ArticleCommentsDto.from(article, comments);
    }

    public List<Article> findAll() {
        return articleRepository.findAll();
    }

    @Transactional
    public void update(Long articleId, Long userId, ArticleServiceDto.Update param) {
        Article article = validateUser(articleId, userId);
        if (param != null) {
            article.update(param.getTitle(), param.getContent());
        }
    }

    @Transactional
    public void delete(Long articleId, Long userId) {
        Article article = validateUser(articleId, userId);

        articleRepository.delete(article);
    }

    private Article validateUser(Long articleId, Long userId) throws NoAuthorityException {
        User user = userRepository.findById(userId)
                .orElseThrow(IllegalArgumentException::new);

        Article article = articleRepository.findById(articleId)
                .orElseThrow(IllegalArgumentException::new);

        if (article.getAuthor() != user) {
            throw new NoAuthorityException();
        }
        return article;
    }

}
