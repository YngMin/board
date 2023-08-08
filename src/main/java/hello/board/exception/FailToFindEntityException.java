package hello.board.exception;

public class FailToFindEntityException extends RuntimeException {

    public FailToFindEntityException() {
        super();
    }

    public FailToFindEntityException(String s) {
        super(s);
    }

    public FailToFindEntityException(String message, Throwable cause) {
        super(message, cause);
    }

    public FailToFindEntityException(Throwable cause) {
        super(cause);
    }

    public static FailToFindEntityException of(String entityName) {
        return new FailToFindEntityException(entityName + " Not Found");
    }
}
