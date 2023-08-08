package hello.board.dto.api.error;

import hello.board.exception.FailToFindEntityException;
import hello.board.exception.NeedLoginException;
import hello.board.exception.NoAuthorityException;
import lombok.Getter;

@Getter
public class ErrorResult {

    private final String code;
    private final String message;

    protected ErrorResult(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public static ErrorResult of(FailToFindEntityException e) {
        return new ErrorResult("BAD", e.getMessage());
    }

    public static ErrorResult of(NeedLoginException e) {
        return new ErrorResult("BAD", e.getMessage());
    }

    public static ErrorResult of(NoAuthorityException e) {
        return new ErrorResult("BAD", e.getMessage());
    }

    public static ErrorResult of(RuntimeException e) {
        return new ErrorResult("BAD", e.getMessage());
    }
}
