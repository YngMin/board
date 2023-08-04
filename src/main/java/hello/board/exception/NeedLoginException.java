package hello.board.exception;

public class NeedLoginException extends RuntimeException {

    public NeedLoginException() {
        super();
    }

    public NeedLoginException(String message) {
        super(message);
    }

    public NeedLoginException(String message, Throwable cause) {
        super(message, cause);
    }

    public NeedLoginException(Throwable cause) {
        super(cause);
    }
}
