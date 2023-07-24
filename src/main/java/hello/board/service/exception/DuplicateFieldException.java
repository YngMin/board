package hello.board.service.exception;

public class DuplicateFieldException extends RuntimeException {

    public DuplicateFieldException() {
        super();
    }

    public DuplicateFieldException(String message) {
        super(message);
    }

    public DuplicateFieldException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicateFieldException(Throwable cause) {
        super(cause);
    }
}
