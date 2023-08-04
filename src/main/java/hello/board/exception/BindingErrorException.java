package hello.board.exception;


import lombok.Getter;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
public class BindingErrorException extends RuntimeException {

    private final List<FieldError> fieldErrors = new ArrayList<>();
    private final List<ObjectError> globalErrors = new ArrayList<>();

    public BindingErrorException() {
        super();
    }

    public BindingErrorException(String message) {
        super(message);
    }

    public BindingErrorException(String message, Throwable cause) {
        super(message, cause);
    }

    public BindingErrorException(Throwable cause) {
        super(cause);
    }

    public BindingErrorException(Collection<FieldError> fieldErrors, Collection<ObjectError> globalErrors) {
        this.fieldErrors.addAll(fieldErrors);
        this.globalErrors.addAll(globalErrors);
    }

    public static BindingErrorException of(Collection<FieldError> fieldErrors, Collection<ObjectError> globalErrors) {
        return new BindingErrorException(fieldErrors, globalErrors);
    }
}
