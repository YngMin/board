package hello.board.web.controller.api;

import hello.board.domain.Article;
import hello.board.domain.User;
import hello.board.dto.api.page.ArticlePageRequest;
import hello.board.dto.service.search.ArticleSearchCond;
import hello.board.exception.BindingErrorException;
import hello.board.service.command.ArticleService;
import hello.board.service.query.ArticleQueryService;
import hello.board.web.annotation.Login;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/api/articles")
    public SaveResponse postArticle(@RequestBody @Valid SaveRequest request, BindingResult bindingResult, @Login User user) {

        handleBindingErrors(bindingResult);

        Long id = articleService.save(user.getId(), request.toDto());
        return SaveResponse.create(id);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/api/articles")
    public Page<FindListResponse> getArticles(@Valid @ModelAttribute("pageRequest") ArticlePageRequest pageRequest, BindingResult bindingResult,
                                              @ModelAttribute("cond") ArticleSearchCond cond
    ) {

        handleBindingErrors(bindingResult);

        Pageable pageable = pageRequest.toPageable();
        return articleQueryService.search(cond, pageable)
                .map(FindListResponse::of);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/api/articles/{id}")
    public FindResponse getArticle(@PathVariable Long id) {
        Article article = articleService.lookUp(id);
        return FindResponse.of(article);
    }

    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/api/articles/{id}")
    public UpdateResponse updateArticle(@RequestBody @Valid UpdateRequest request, BindingResult bindingResult, @Login User user, @PathVariable Long id) {

        handleBindingErrors(bindingResult);

        articleService.update(id, user.getId(), request.toDto());
        Article updatedArticle = articleQueryService.findById(id);
        return UpdateResponse.of(updatedArticle);
    }

    @DeleteMapping("/api/articles/{id}")
    public ResponseEntity<Void> deleteArticle(@Login User user, @PathVariable Long id) {
        articleService.delete(id, user.getId());
        return ResponseEntity.ok().build();
    }

    private static void handleBindingErrors(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw BindingErrorException.of(bindingResult.getFieldErrors(), bindingResult.getGlobalErrors());
        }
    }
}
