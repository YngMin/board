package hello.board.web.advice;

import hello.board.dto.api.error.BindingErrorResult;
import hello.board.dto.api.error.ErrorResult;
import hello.board.exception.BindingErrorException;
import hello.board.exception.FailToFindEntityException;
import hello.board.exception.NeedLoginException;
import hello.board.exception.NoAuthorityException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(annotations = RestController.class)
public class RestControllerExAdvice {
    @ExceptionHandler(FailToFindEntityException.class)
    public ResponseEntity<ErrorResult> failToFindEntityExHandle(FailToFindEntityException e) {
        log.info("FailToFindEntityException");
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResult.of(e));
    }

    @ExceptionHandler(NoAuthorityException.class)
    public ResponseEntity<ErrorResult> noAuthorityExHandle(NoAuthorityException e) {
        log.info("NoAuthorityException");
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResult.of(e));
    }

    @ExceptionHandler(BindingErrorException.class)
    public ResponseEntity<BindingErrorResult> bindingErrorExHandle(BindingErrorException e) {
        log.info("BindingErrorException");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(BindingErrorResult.of(e));
    }

    @ExceptionHandler(NeedLoginException.class)
    public ResponseEntity<ErrorResult> needLoginExHandle(NeedLoginException e) {
        log.info("NeedLoginException");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResult.of(e));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResult> runtimeExHandle(RuntimeException e) {
        log.info("RuntimeException");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResult.of(e));
    }

}
