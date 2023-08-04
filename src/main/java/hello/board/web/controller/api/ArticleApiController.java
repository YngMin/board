package hello.board.web.controller.api;

import hello.board.domain.Article;
import hello.board.exception.BindingErrorException;
import hello.board.service.ArticleService;
import hello.board.service.query.ArticleQueryService;
import hello.board.web.annotation.Login;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import static hello.board.dto.api.ArticleApiDto.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ArticleApiController {

    private final ArticleService articleService;
    private final ArticleQueryService articleQueryService;

    @PostMapping("/api/articles")
    public ResponseEntity<SaveResponse> postArticle(@RequestBody @Valid SaveRequest request, BindingResult bindingResult, @Login Long userId) {

        handleBindingError(bindingResult);

        Long articleId = articleService.save(userId, request.toDto());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SaveResponse.create(articleId));
    }

    private static void handleBindingError(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw BindingErrorException.of(bindingResult.getFieldErrors(), bindingResult.getGlobalErrors());
        }
    }

    @GetMapping("/api/articles")
    public ResponseEntity<Page<FindListResponse>> getArticles(@RequestParam(defaultValue = "1") int page,
                                                                @RequestParam(defaultValue = "10") int size
    ) {

        Page<FindListResponse> articles = articleQueryService.search(convertPageNumber(page), size)
                .map(FindListResponse::from);

        return ResponseEntity.ok(articles);
    }

    @GetMapping("/api/articles/{id}")
    public ResponseEntity<FindResponse> getArticle(@PathVariable Long id) {
        Article article = articleService.lookUpArticle(id);

        return ResponseEntity.ok(FindResponse.from(article));
    }

    @PutMapping("/api/articles/{articleId}")
    public ResponseEntity<UpdateResponse> updateArticle(@PathVariable Long articleId,
                                                        @RequestBody @Valid UpdateRequest request,
                                                        BindingResult bindingResult,
                                                        @Login Long userId) {

        handleBindingError(bindingResult);

        articleService.update(articleId, userId, request.toDto());
        Article updatedArticle = articleQueryService.findById(articleId);

        return ResponseEntity.ok(UpdateResponse.from(updatedArticle));
    }

    @DeleteMapping("/api/articles/{articleId}")
    public ResponseEntity<Void> deleteArticle(@PathVariable Long articleId, @Login Long userId) {

        articleService.delete(articleId, userId);
        return ResponseEntity.ok().build();
    }

    private static int convertPageNumber(int page) {
        return Integer.max(0, page - 1);
    }

}
