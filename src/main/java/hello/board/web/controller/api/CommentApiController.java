package hello.board.web.controller.api;

import hello.board.domain.Comment;
import hello.board.domain.User;
import hello.board.exception.BindingErrorException;
import hello.board.exception.NeedLoginException;
import hello.board.service.CommentService;
import hello.board.service.query.CommentQueryService;
import hello.board.web.annotation.Login;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import static hello.board.dto.api.CommentApiDto.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CommentApiController {

    private final CommentService commentService;
    private final CommentQueryService commentQueryService;

    @PostMapping("/api/articles/{articleId}/comments")
    public ResponseEntity<SaveResponse> addComment(@RequestBody @Valid SaveRequest request, BindingResult bindingResult,
                                                   @Login User user, @PathVariable Long articleId) {

        validateUser(user);
        handleBindingError(bindingResult);

        Long id = commentService.save(articleId, user.getId(), request.toDto());
        return ResponseEntity.status(HttpStatus.CREATED)
                        .body(SaveResponse.create(id));
    }

    @GetMapping("/api/articles/{articleId}/comments")
    public ResponseEntity<Page<FindResponse>> getComments(@RequestParam(defaultValue = "1") int page,
                                                            @RequestParam(defaultValue = "10") int size,
                                                            @PathVariable Long articleId) {

        Page<FindResponse> comments = commentQueryService.findByArticleId(articleId, PageRequest.of(page, size))
                .map(FindResponse::from);

        return ResponseEntity.ok(comments);
    }

    @GetMapping("/api/articles/{articleId}/comments/{commentId}")
    public ResponseEntity<FindResponse> getComment(@PathVariable Long articleId, @PathVariable Long commentId) {
        Comment comment = commentService.lookUpComment(commentId, articleId);

        return ResponseEntity.ok(FindResponse.from(comment));
    }

    @PutMapping("/api/articles/{articleId}/comments/{commentId}")
    public ResponseEntity<UpdateResponse> updateComment(@RequestBody @Valid UpdateRequest request, BindingResult bindingResult,
                                                        @Login User user, @PathVariable Long articleId, @PathVariable Long commentId) {

        validateUser(user);
        handleBindingError(bindingResult);

        commentService.update(commentId, articleId, user.getId(), request.toDto());
        Comment comment = commentQueryService.findById(commentId);

        return ResponseEntity.ok(UpdateResponse.from(comment));
    }

    @DeleteMapping("/api/articles/{articleId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@Login User user, @PathVariable Long articleId, @PathVariable Long commentId) {

        validateUser(user);
        commentService.delete(commentId, articleId ,user.getId());

        return ResponseEntity.ok().build();
    }

    private static void handleBindingError(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw BindingErrorException.of(bindingResult.getFieldErrors(), bindingResult.getGlobalErrors());
        }
    }

    private static void validateUser(User user) {
        if (user == null) {
            throw new NeedLoginException();
        }
    }
}
