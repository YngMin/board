package hello.board.web.controller.api;

import hello.board.domain.Comment;
import hello.board.dto.service.CommentServiceDto;
import hello.board.service.command.CommentService;
import hello.board.service.query.CommentQueryService;
import hello.board.web.annotation.Login;
import hello.board.web.annotation.RestValidBinding;
import hello.board.web.dtoresolver.CommentServiceDtoResolver;
import hello.board.web.user.LoginInfo;
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
    private final CommentServiceDtoResolver dtoResolver;

    @RestValidBinding
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/api/articles/{articleId}/comments")
    public SaveResponse addComment(@Valid @RequestBody SaveRequest request, BindingResult br, @Login LoginInfo loginInfo, @PathVariable Long articleId) {
        CommentServiceDto.Save saveDto = dtoResolver.toSaveDto(request);
        Long id = commentService.save(articleId, loginInfo.getUserId(), saveDto);
        return SaveResponse.create(id);
    }

    @RestValidBinding
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/api/articles/{articleId}/comments")
    public Page<FindResponse> getComments(@Valid @ModelAttribute PageRequest pageRequest, BindingResult br, @PathVariable Long articleId) {
        Pageable pageable = dtoResolver.toPageable(pageRequest);
        return commentQueryService.findByArticleId(articleId, pageable)
                .map(FindResponse::of);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/api/articles/{articleId}/comments/{commentId}")
    public FindResponse getComment(@PathVariable Long articleId, @PathVariable Long commentId) {
        Comment comment = commentQueryService.findWithArticle(commentId, articleId);
        return FindResponse.of(comment);
    }

    @RestValidBinding
    @PutMapping("/api/articles/{articleId}/comments/{commentId}")
    public UpdateResponse updateComment(@Valid @RequestBody UpdateRequest request, BindingResult br, @Login LoginInfo loginInfo, @PathVariable Long articleId, @PathVariable Long commentId) {
        CommentServiceDto.Update updateDto = dtoResolver.toUpdateDto(request);
        commentService.update(commentId, articleId, loginInfo.getUserId(), updateDto);
        Comment comment = commentQueryService.findById(commentId);
        return UpdateResponse.of(comment);
    }

    @DeleteMapping("/api/articles/{articleId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@Login LoginInfo loginInfo, @PathVariable Long articleId, @PathVariable Long commentId) {
        commentService.delete(commentId, articleId, loginInfo.getUserId());
        return ResponseEntity.ok().build();
    }
}
