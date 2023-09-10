package hello.board.dto.api.error;

import hello.board.exception.BindingErrorException;
import lombok.Getter;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Collection;
import java.util.List;

@Getter
public final class BindingErrorResult extends ErrorResult {

    private final List<FieldErrorSpecification> fieldErrors;
    private final List<GlobalErrorSpecification> globalErrors;

    private BindingErrorResult(String code, String message, Collection<FieldError> fieldErrors, Collection<ObjectError> globalErrors) {
        super(code, message);

        this.fieldErrors = fieldErrors.stream()
                .map(FieldErrorSpecification::from)
                .toList();

        this.globalErrors = globalErrors.stream()
                .map(GlobalErrorSpecification::from)
                .toList();
    }

    public static BindingErrorResult of(BindingErrorException e) {
        return new BindingErrorResult("BAD", "입력 값 오류", e.getFieldErrors(), e.getGlobalErrors());
    }

    public static BindingErrorResult of(MethodArgumentNotValidException e) {
        return new BindingErrorResult("BAD", "입력 값 오류", e.getFieldErrors(), e.getGlobalErrors());
    }

    @Getter
    static final class FieldErrorSpecification {
        private final String field;
        private final String message;

        private FieldErrorSpecification(String field, String message) {
            this.field = field;
            this.message = message;
        }

        public static FieldErrorSpecification from(FieldError fieldError) {
            return new FieldErrorSpecification(fieldError.getField(), fieldError.getDefaultMessage());
        }

    }

    @Getter
    static final class GlobalErrorSpecification {
        private final String code;
        private final String message;

        private GlobalErrorSpecification(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public static GlobalErrorSpecification from(ObjectError globalError) {
            return new GlobalErrorSpecification(globalError.getCode(), globalError.getDefaultMessage());
        }

    }
}
