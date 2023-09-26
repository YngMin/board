package hello.board.web.controlleradvice;

import hello.board.dto.api.error.BindingErrorResult;
import hello.board.dto.api.error.ErrorResult;
import hello.board.exception.BindingErrorException;
import hello.board.exception.FailToFindEntityException;
import hello.board.exception.NoAuthorityException;
import hello.board.exception.WrongPageRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(annotations = RestController.class)
public class RestControllerExAdvice {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(FailToFindEntityException.class)
    public ErrorResult failToFindEntityExHandle(FailToFindEntityException e) {
        return ErrorResult.of(e);
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(NoAuthorityException.class)
    public ErrorResult noAuthorityExHandle(NoAuthorityException e) {
        return ErrorResult.of(e);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BindingErrorException.class)
    public BindingErrorResult bindingErrorExHandle(BindingErrorException e) {
        return BindingErrorResult.of(e);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(WrongPageRequestException.class)
    public ErrorResult wrongPageExHandle(WrongPageRequestException e) {
        return ErrorResult.of(e);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public BindingErrorResult methodArgumentNotValidExHandle(MethodArgumentNotValidException e) {
        return BindingErrorResult.of(e);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(RuntimeException.class)
    public ErrorResult runtimeExHandle(RuntimeException e) {
        log.info("RuntimeException", e);
        return ErrorResult.of(e);
    }

}
