package hello.board.web.controller.api;

import hello.board.domain.Comment;
import hello.board.domain.User;
import hello.board.dto.api.page.CommentPageRequest;
import hello.board.exception.BindingErrorException;
import hello.board.service.command.CommentService;
import hello.board.service.query.CommentQueryService;
import hello.board.web.annotation.Login;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import static hello.board.dto.api.CommentApiDto.*;

@RestController
@RequiredArgsConstructor
public class CommentApiController {

    private final CommentService commentService;
    private final CommentQueryService commentQueryService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/api/articles/{articleId}/comments")
    public SaveResponse addComment(@RequestBody @Valid SaveRequest request, BindingResult bindingResult, @Login User user, @PathVariable Long articleId) {

        handleBindingErrors(bindingResult);

        Long id = commentService.save(articleId, user.getId(), request.toDto());
        return SaveResponse.create(id);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/api/articles/{articleId}/comments")
    public Page<FindResponse> getComments(@Valid @ModelAttribute("pageRequest") CommentPageRequest pageRequest, BindingResult bindingResult, @PathVariable Long articleId) {

        handleBindingErrors(bindingResult);

        Pageable pageable = pageRequest.toPageable();
        return commentQueryService.findByArticleId(articleId, pageable)
                .map(FindResponse::of);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/api/articles/{articleId}/comments/{commentId}")
    public FindResponse getComment(@PathVariable Long articleId, @PathVariable Long commentId) {
        Comment comment = commentQueryService.findWithArticle(commentId, articleId);
        return FindResponse.of(comment);
    }

    @PutMapping("/api/articles/{articleId}/comments/{commentId}")
    public UpdateResponse updateComment(@RequestBody @Valid UpdateRequest request, BindingResult bindingResult, @Login User user, @PathVariable Long articleId, @PathVariable Long commentId) {

        handleBindingErrors(bindingResult);

        commentService.update(commentId, articleId, user.getId(), request.toDto());
        Comment comment = commentQueryService.findById(commentId);
        return UpdateResponse.of(comment);
    }

    @DeleteMapping("/api/articles/{articleId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@Login User user, @PathVariable Long articleId, @PathVariable Long commentId) {
        commentService.delete(commentId, articleId ,user.getId());
        return ResponseEntity.ok().build();
    }

    private static void handleBindingErrors(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw BindingErrorException.of(bindingResult.getFieldErrors(), bindingResult.getGlobalErrors());
        }
    }
}
