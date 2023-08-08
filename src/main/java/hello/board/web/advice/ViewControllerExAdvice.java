package hello.board.web.advice;

import hello.board.exception.DuplicatedEmailException;
import hello.board.exception.FailToFindEntityException;
import hello.board.exception.NeedLoginException;
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

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(NeedLoginException.class)
    public String needLoginExHandle(NeedLoginException e) {
        log.info("NeedLoginException", e);
        return "redirect:/login";
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(DuplicatedEmailException.class)
    public String duplicatedEmailExHandle(DuplicatedEmailException e) {
        log.info("DuplicatedEmailException", e);
        return "redirect:/join";
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public String illegalArgumentExHandle(IllegalArgumentException e) {
        log.info("IllegalArgumentException", e);
        return "error/400";
    }
}
