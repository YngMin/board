package hello.board.exception;

public class DuplicatedEmailException extends RuntimeException {

    public DuplicatedEmailException() {
        super();
    }

    public DuplicatedEmailException(String message) {
        super(message);
    }

    public DuplicatedEmailException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicatedEmailException(Throwable cause) {
        super(cause);
    }
}
