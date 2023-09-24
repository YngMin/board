package hello.board.web.controlleradvice;

import hello.board.exception.FailToFindEntityException;
import hello.board.exception.WrongPageRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@Slf4j
@ControllerAdvice(annotations = Controller.class)
public class ViewControllerExAdvice {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(FailToFindEntityException.class)
    public String failToFindEntityExHandle(FailToFindEntityException e) {
        log.info("FailToFindEntityException", e);
        return "error/404";
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public String illegalArgumentExHandle(IllegalArgumentException e) {
        log.info("IllegalArgumentException", e);
        return "error/400";
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(WrongPageRequestException.class)
    public String wrongPageRequestExHandle(WrongPageRequestException e) {
        log.info("WrongPageRequestExHandle", e);
        return "error/400";
    }
}
