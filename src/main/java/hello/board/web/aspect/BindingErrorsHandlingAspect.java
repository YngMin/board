package hello.board.web.aspect;

import hello.board.dto.form.UserForm;
import hello.board.dto.view.BoardRequest.ArticleListRequest;
import hello.board.dto.view.BoardRequest.ArticleRequest;
import hello.board.exception.BindingErrorException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.validation.BindingResult;

@Slf4j
@Aspect
@Order(1)
public class BindingErrorsHandlingAspect {

    @Pointcut("execution(* hello.board.web.controller.api..*(..))")
    private void apiControllers() {}

    @Pointcut("execution(* hello.board.web.controller.view..*(..))")
    private void viewControllers() {}

    @Before("apiControllers()")
    public void handleBindingErrors(JoinPoint joinPoint) {
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof BindingResult br && br.hasErrors()) {
                throw BindingErrorException.of(br.getFieldErrors(), br.getGlobalErrors());
            }
        }
    }

    @Around("viewControllers() && args(listRequest, bindingResult, ..)")
    public Object handleBindingErrors(ProceedingJoinPoint joinPoint, ArticleListRequest listRequest, BindingResult bindingResult) throws Throwable {
        if (bindingResult.hasErrors()) {
            return "redirect:/board";
        }
        return joinPoint.proceed();
    }

    @Around("viewControllers() && args(articleRequest, bindingResult, ..)")
    public Object handleBindingErrors(ProceedingJoinPoint joinPoint, ArticleRequest articleRequest, BindingResult bindingResult) throws Throwable {
        if (bindingResult.hasErrors()) {
            return "redirect:/board";
        }

        return joinPoint.proceed();
    }

    @Around("viewControllers() && args(saveForm, bindingResult, ..)")
    public Object handleBindingErrors(ProceedingJoinPoint joinPoint, UserForm.Save saveForm, BindingResult bindingResult) throws Throwable {
        if (bindingResult.hasErrors()) {
            return "login/joinForm";
        }
        return joinPoint.proceed();
    }

}
