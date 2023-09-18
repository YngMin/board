package hello.board.web.aspect;

import hello.board.exception.BindingErrorException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.validation.BindingResult;

@Aspect
public class BindingAspect {
    @Before("execution(* hello.board.web.controller.api..*(..))")
    public void handleBindingErrors(JoinPoint joinPoint) {
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof BindingResult br && br.hasErrors()) {
                throw BindingErrorException.of(br.getFieldErrors(), br.getGlobalErrors());
            }
        }
    }
}
