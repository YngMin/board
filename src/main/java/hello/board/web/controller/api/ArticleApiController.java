package hello.board.web.controller.api;

import hello.board.domain.Article;
import hello.board.dto.service.ArticleServiceDto.Save;
import hello.board.dto.service.ArticleServiceDto.Update;
import hello.board.dto.service.search.ArticleSearchCond;
import hello.board.service.command.ArticleService;
import hello.board.service.query.ArticleQueryService;
import hello.board.web.annotation.Login;
import hello.board.web.annotation.RestValidBinding;
import hello.board.web.dtoresolver.ArticleServiceDtoResolver;
import hello.board.web.user.LoginInfo;
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

    private final ArticleServiceDtoResolver dtoResolver;

    @RestValidBinding
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/api/articles")
    public SaveResponse postArticle(@Valid @RequestBody SaveRequest request, BindingResult br, @Login LoginInfo loginInfo) {
        Save saveDto = dtoResolver.toSaveDto(request);
        Long id = articleService.save(loginInfo.getUserId(), saveDto);
        return SaveResponse.create(id);
    }

    @RestValidBinding
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/api/articles")
    public Page<FindListResponse> getArticles(@Valid @ModelAttribute FindRequest request, BindingResult br) {
        Pageable pageable = dtoResolver.toPageable(request);
        ArticleSearchCond cond = dtoResolver.toSearchCond(request);
        return articleQueryService.search(cond, pageable)
                .map(FindListResponse::of);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/api/articles/{id}")
    public FindResponse getArticle(@PathVariable Long id) {
        Article article = articleService.lookUp(id);
        return FindResponse.of(article);
    }

    @RestValidBinding
    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/api/articles/{id}")
    public UpdateResponse updateArticle(@Valid @RequestBody UpdateRequest request, BindingResult br, @Login LoginInfo loginInfo, @PathVariable Long id) {
        Update updateDto = dtoResolver.toUpdateDto(request);
        articleService.update(id, loginInfo.getUserId(), updateDto);
        Article updatedArticle = articleQueryService.findById(id);
        return UpdateResponse.of(updatedArticle);
    }

    @DeleteMapping("/api/articles/{id}")
    public ResponseEntity<Void> deleteArticle(@Login LoginInfo loginInfo, @PathVariable Long id) {
        articleService.delete(id, loginInfo.getUserId());
        return ResponseEntity.ok().build();
    }
}
