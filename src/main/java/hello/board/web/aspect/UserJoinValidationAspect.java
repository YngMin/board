package hello.board.web.aspect;

import hello.board.dto.form.UserForm;
import hello.board.service.query.UserQueryService;
import hello.board.web.annotation.ValidNewUser;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.validation.BindingResult;

@Aspect
@Order(3)
@RequiredArgsConstructor
public class UserJoinValidationAspect {

    private final UserQueryService userQueryService;

    @Around("@annotation(validNewUser) && args(saveForm, bindingResult, ..)")
    public Object handleBindingErrors(ProceedingJoinPoint joinPoint, ValidNewUser validNewUser, UserForm.Save saveForm, BindingResult bindingResult) throws Throwable {

        final String viewName = validNewUser.goBackTo();

        if (bindingResult.hasErrors()) {
            return viewName;
        }

        if (saveForm.passwordsDoNotMatch()) {
            bindingResult.reject("PasswordNotMatch");
            return viewName;
        }

        if (userQueryService.existsByEmail(saveForm.getEmail())) {
            bindingResult.reject("EmailExists");
            return viewName;
        }

        try {
            return joinPoint.proceed();
        } catch (DataIntegrityViolationException e) {
            bindingResult.reject("EmailExists");
            return viewName;
        }
    }
}
