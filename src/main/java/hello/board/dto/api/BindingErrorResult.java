package hello.board.dto.api;

import hello.board.exception.BindingErrorException;
import lombok.Getter;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.util.Collection;
import java.util.List;

@Getter
public class BindingErrorResult extends ErrorResult {

    private final List<FieldErrorSpecification> fieldErrors;
    private final List<GlobalErrorSpecification> globalErrors;

    public BindingErrorResult(String code, String message, Collection<FieldError> fieldErrors, Collection<ObjectError> globalErrors) {
        super(code, message);
        this.fieldErrors = fieldErrors.stream()
                .map(err -> new FieldErrorSpecification(err.getField(), err.getDefaultMessage()))
                .toList();

        this.globalErrors = globalErrors.stream()
                .map(err -> new GlobalErrorSpecification(err.getCode(), err.getDefaultMessage()))
                .toList();
    }

    public static BindingErrorResult of(BindingErrorException e) {
        return new BindingErrorResult("BAD", "입력 값 오류", e.getFieldErrors(), e.getGlobalErrors());
    }

    @Getter
    static class FieldErrorSpecification {
        private final String field;
        private final String message;

        public FieldErrorSpecification(String field, String message) {
            this.field = field;
            this.message = message;
        }

    }

    @Getter
    static class GlobalErrorSpecification {
        private final String code;
        private final String message;

        public GlobalErrorSpecification(String code, String message) {
            this.code = code;
            this.message = message;
        }
    }
}
