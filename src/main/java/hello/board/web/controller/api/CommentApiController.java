package hello.board.web.controller.api;

import hello.board.domain.Comment;
import hello.board.dto.api.Result;
import hello.board.exception.BindingErrorException;
import hello.board.service.CommentService;
import hello.board.service.query.CommentQueryService;
import hello.board.web.annotation.Login;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static hello.board.dto.api.CommentApiDto.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CommentApiController {

    private final CommentService commentService;
    private final CommentQueryService commentQueryService;

    @PostMapping("/api/articles/{articleId}/comments")
    public ResponseEntity<SaveResponse> addComment(@PathVariable Long articleId,
                                                   @RequestBody @Valid SaveRequest request,
                                                   BindingResult bindingResult,
                                                   @Login Long userId) {

        handleBindingError(bindingResult);

        Long id = commentService.save(articleId, userId, request.toDto());
        return ResponseEntity.status(HttpStatus.CREATED)
                        .body(SaveResponse.create(id));
    }

    @GetMapping("/api/articles/{articleId}/comments")
    public ResponseEntity<Result<FindResponse>> getComments(@PathVariable Long articleId) {
        List<FindResponse> comments = commentQueryService.findCommentsOfArticle(articleId).stream()
                .map(FindResponse::from)
                .toList();

        return ResponseEntity.ok(Result.of(comments));
    }

    @GetMapping("/api/articles/{articleId}/comments/{commentId}")
    public ResponseEntity<FindResponse> getComment(@PathVariable Long articleId, @PathVariable Long commentId) {
        Comment comment = commentService.lookUpComment(commentId, articleId);

        return ResponseEntity.ok(FindResponse.from(comment));
    }

    @PutMapping("/api/articles/{articleId}/comments/{commentId}")
    public ResponseEntity<UpdateResponse> updateComment(@PathVariable Long articleId,
                                                        @PathVariable Long commentId,
                                                        @RequestBody @Valid UpdateRequest request,
                                                        BindingResult bindingResult,
                                                        @Login Long userId) {

        handleBindingError(bindingResult);

        commentService.update(commentId, articleId, userId, request.toDto());
        Comment comment = commentQueryService.findById(commentId);

        return ResponseEntity.ok(UpdateResponse.from(comment));
    }

    @DeleteMapping("/api/articles/{articleId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long articleId, @PathVariable Long commentId, @Login Long userId) {
        commentService.delete(commentId, articleId ,userId);

        return ResponseEntity.ok().build();
    }

    private static void handleBindingError(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw BindingErrorException.of(bindingResult.getFieldErrors(), bindingResult.getGlobalErrors());
        }
    }

}
