package hello.board.web.controller.api;

import hello.board.domain.Article;
import hello.board.service.ArticleService;
import hello.board.web.annotation.Login;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static hello.board.dto.api.ArticleApiDto.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ArticleApiController {

    private final ArticleService articleService;

    @PostMapping("/api/articles")
    public ResponseEntity<SaveResponse> postArticle(@RequestBody @Valid SaveRequest request, @Login Long userId) {

        Long articleId = articleService.save(userId, request.toDto());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SaveResponse.create(articleId));
    }

    @GetMapping("/api/articles")
    public ResponseEntity<List<FindResponse>> getArticles() {
        List<FindResponse> articles = articleService.findAll().stream()
                .map(FindResponse::from)
                .toList();

        return ResponseEntity.ok(articles);
    }

    @GetMapping("/api/articles/{id}")
    public ResponseEntity<FindResponse> getArticle(@PathVariable Long id) {
        Article article = articleService.findById(id);

        return ResponseEntity.ok(FindResponse.from(article));
    }

    @PutMapping("/api/articles/{articleId}")
    public ResponseEntity<UpdateResponse> updateArticle(@PathVariable Long articleId,
                                                        @RequestBody @Valid UpdateRequest request,
                                                        @Login Long userId) {

        articleService.update(articleId, userId, request.toDto());
        Article updatedArticle = articleService.findById(articleId);

        return ResponseEntity.ok(UpdateResponse.from(updatedArticle));
    }

    @DeleteMapping("/api/articles/{articleId}")
    public ResponseEntity<Void> deleteArticle(@PathVariable Long articleId, @Login Long userId) {

        articleService.delete(articleId, userId);
        return ResponseEntity.ok().build();
    }

}
