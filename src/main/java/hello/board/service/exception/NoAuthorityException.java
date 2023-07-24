package hello.board.service.exception;

public class NoAuthorityException extends RuntimeException {

    public NoAuthorityException() {
        super();
    }

    public NoAuthorityException(String message) {
        super(message);
    }

    public NoAuthorityException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoAuthorityException(Throwable cause) {
        super(cause);
    }
}
