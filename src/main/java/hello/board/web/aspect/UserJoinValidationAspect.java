package hello.board.web.aspect;

import hello.board.dto.form.UserForm;
import hello.board.service.query.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.validation.BindingResult;

@Aspect
@Order(3)
@RequiredArgsConstructor
public class UserJoinValidationAspect {

    private final UserQueryService userQueryService;

    @Pointcut("execution(* hello.board.web.controller.view.UserViewController.*(..))")
    private void userViewController(){}

    @Around("userViewController() && args(saveForm, bindingResult, ..)")
    public Object handleBindingErrors(ProceedingJoinPoint joinPoint, UserForm.Save saveForm, BindingResult bindingResult) throws Throwable {
        if (bindingResult.hasErrors()) {
            return "login/joinForm";
        }

        if (saveForm.passwordsDoNotMatch()) {
            bindingResult.reject("PasswordNotMatch");
            return "login/joinForm";
        }

        if (userQueryService.existsByEmail(saveForm.getEmail())) {
            bindingResult.reject("EmailExists");
            return "login/joinForm";
        }

        try {
            return joinPoint.proceed();
        } catch (DataIntegrityViolationException e) {
            bindingResult.reject("EmailExists");
            return "login/joinForm";
        }
    }
}
