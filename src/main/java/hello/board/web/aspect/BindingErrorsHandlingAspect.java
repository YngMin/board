package hello.board.web.aspect;

import hello.board.exception.BindingErrorException;
import hello.board.web.annotation.ValidBinding;
import hello.board.web.annotation.RestValidBinding;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;
import org.springframework.validation.BindingResult;

@Slf4j
@Aspect
@Order(1)
public class BindingErrorsHandlingAspect {

    @Before("@annotation(restValidBinding)")
    public void handleBindingErrors(JoinPoint joinPoint, RestValidBinding restValidBinding) {
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof BindingResult bindingResult) {
                if (bindingResult.hasErrors()) {
                    throw BindingErrorException.of(
                            bindingResult.getFieldErrors(),
                            bindingResult.getGlobalErrors()
                    );
                }
            }
        }
    }

    @Around("@annotation(validBinding)")
    public Object handleBindingErrors(ProceedingJoinPoint joinPoint, ValidBinding validBinding) throws Throwable {
        final String viewName = validBinding.goBackTo();
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof BindingResult bindingResult) {
                if (bindingResult.hasErrors()) {
                    return viewName;
                }
            }
        }

        return joinPoint.proceed();
    }
}
