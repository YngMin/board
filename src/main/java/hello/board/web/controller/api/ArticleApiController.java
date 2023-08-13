package hello.board.web.controller.api;

import hello.board.domain.Article;
import hello.board.domain.User;
import hello.board.dto.service.search.ArticleSearchCond;
import hello.board.exception.BindingErrorException;
import hello.board.exception.NeedLoginException;
import hello.board.service.command.ArticleService;
import hello.board.service.query.ArticleQueryService;
import hello.board.web.annotation.Login;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import static hello.board.dto.api.ArticleApiDto.*;

@RestController
@RequiredArgsConstructor
public class ArticleApiController {

    private final ArticleService articleService;
    private final ArticleQueryService articleQueryService;

    @PostMapping("/api/articles")
    public ResponseEntity<SaveResponse> postArticle(@RequestBody @Valid SaveRequest request, BindingResult bindingResult, @Login User user) {

        validateUser(user);

        handleBindingError(bindingResult);

        Long id = articleService.save(user.getId(), request.toDto());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SaveResponse.create(id));
    }

    @GetMapping("/api/articles")
    public ResponseEntity<Page<FindListResponse>> getArticles(@RequestParam(defaultValue = "1") int page,
                                                              @RequestParam(defaultValue = "10") int size,
                                                              @ModelAttribute("cond") ArticleSearchCond cond
                                                              ) {

        Page<FindListResponse> articles = articleQueryService.search(cond, toZeroStartIndex(page), size)
                .map(FindListResponse::from);

        return ResponseEntity.ok(articles);
    }

    @GetMapping("/api/articles/{id}")
    public ResponseEntity<FindResponse> getArticle(@PathVariable Long id) {
        Article article = articleService.lookUp(id);

        return ResponseEntity.ok(FindResponse.from(article));
    }

    @PutMapping("/api/articles/{id}")
    public ResponseEntity<UpdateResponse> updateArticle(@RequestBody @Valid UpdateRequest request, BindingResult bindingResult,
                                                        @Login User user, @PathVariable Long id) {

        validateUser(user);

        handleBindingError(bindingResult);

        articleService.update(id, user.getId(), request.toDto());
        Article updatedArticle = articleQueryService.findById(id);

        return ResponseEntity.ok(UpdateResponse.from(updatedArticle));
    }

    @DeleteMapping("/api/articles/{id}")
    public ResponseEntity<Void> deleteArticle(@Login User user, @PathVariable Long id) {

        validateUser(user);

        articleService.delete(id, user.getId());
        return ResponseEntity.ok().build();
    }

    /* ##################################################### */

    private static void handleBindingError(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw BindingErrorException.of(bindingResult.getFieldErrors(), bindingResult.getGlobalErrors());
        }
    }

    private static int toZeroStartIndex(int page) {
        return Integer.max(0, page - 1);
    }

    private static void validateUser(User user) {
        if (user == null) {
            throw new NeedLoginException();
        }
    }

}
