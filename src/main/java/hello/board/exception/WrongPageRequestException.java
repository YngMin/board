package hello.board.exception;

public class WrongPageRequestException extends IllegalArgumentException {
    public WrongPageRequestException() {
        super();
    }

    public WrongPageRequestException(String s) {
        super(s);
    }

    public WrongPageRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public WrongPageRequestException(Throwable cause) {
        super(cause);
    }

    public static WrongPageRequestException of(int page, int size) {
        return new WrongPageRequestException("Page: " + page + ": Size: " + size);
    }
}
