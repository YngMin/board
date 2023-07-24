package hello.board.web.controller.api;

import hello.board.domain.Comment;
import hello.board.service.CommentService;
import hello.board.web.annotation.Login;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static hello.board.dto.api.CommentApiDto.*;

@RestController
@RequiredArgsConstructor
public class CommentApiController {

    private final CommentService commentService;

    @PostMapping("/api/articles/{articleId}/comments")
    public ResponseEntity<SaveResponse> addComment(@PathVariable Long articleId, @RequestBody @Valid SaveRequest request, @Login Long userId) {
        Long id = commentService.save(articleId, userId, request.toDto());
        return ResponseEntity.status(HttpStatus.CREATED)
                        .body(SaveResponse.create(id));
    }

    @GetMapping("/api/articles/{articleId}/comments")
    public ResponseEntity<List<FindResponse>> getComments(@PathVariable Long articleId) {
        List<FindResponse> comments = commentService.findCommentsOfArticle(articleId).stream()
                .map(FindResponse::from)
                .toList();

        return ResponseEntity.ok(comments);
    }

    @GetMapping("/api/articles/{articleId}/comments/{commentId}")
    public ResponseEntity<FindResponse> getComment(@PathVariable Long articleId, @PathVariable Long commentId) {
        Comment comment = commentService.findComment(commentId, articleId);

        return ResponseEntity.ok(FindResponse.from(comment));
    }

    @PutMapping("/api/articles/{articleId}/comments/{commentId}")
    public ResponseEntity<UpdateResponse> updateComment(@PathVariable Long articleId, @PathVariable Long commentId, @RequestBody @Valid UpdateRequest request, @Login Long userId) {
        commentService.update(commentId, articleId, userId, request.toDto());
        Comment comment = commentService.findById(commentId);

        return ResponseEntity.ok(UpdateResponse.from(comment));
    }

    @DeleteMapping("/api/articles/{articleId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long articleId, @PathVariable Long commentId, @Login Long userId) {
        commentService.delete(commentId, articleId ,userId);

        return ResponseEntity.ok().build();
    }

}
