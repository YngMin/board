package hello.board.web.aspect;

import hello.board.exception.WrongPageRequestException;
import hello.board.web.annotation.ValidPage;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Page;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Slf4j
@Aspect
@Order(2)
public class PageRequestValidationAspect {

    @AfterReturning("@annotation(validPage)")
    public void validateMaliciousPageRequest(JoinPoint joinPoint, ValidPage validPage) {
        Model model = getModel(joinPoint);

        if (model != null) {
            String attributeName = validPage.attributeName();
            Object attribute = model.getAttribute(attributeName);

            Class<?> requestType = validPage.requestType();
            Object request = getRequest(joinPoint, requestType);

            if (request != null) {
                try {
                    String getterName = buildGetterName(validPage.requestPageFieldName());
                    Method getter = requestType.getMethod(getterName);
                    int requestPage = (int) getter.invoke(request);

                    if (attribute instanceof Page<?> result) {
                        filterOutMaliciousPageRequest(requestPage, validPage.pageSize(), result);
                    }

                } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
    }

    private static Object getRequest(JoinPoint joinPoint, Class<?> requestType) {
        for (Object arg : joinPoint.getArgs()) {
            if (arg != null && requestType.isAssignableFrom(arg.getClass())) {
                return arg;
            }
        }
        return null;
    }

    private static String buildGetterName(String fieldName) {
        if (!StringUtils.hasText(fieldName)) {
            throw new IllegalStateException(" \"requestPageFieldName()\" of @ValidPage is wrong");
        }
        StringBuilder builder = new StringBuilder(fieldName);
        builder.setCharAt(0, Character.toUpperCase(fieldName.charAt(0)));
        builder.insert(0, "get");
        return builder.toString();
    }

    private static Model getModel(JoinPoint joinPoint) {
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof Model model) {
                return model;
            }
        }
        return null;
    }

    private static <T> void filterOutMaliciousPageRequest(int requestPage, int pageSize, Page<T> result) {
        if (result.getTotalPages() == 0) {
            if (requestPage > 1) {
                throw WrongPageRequestException.of(requestPage, pageSize);
            }
        }

        else if (requestPage > result.getTotalPages()) {
            throw WrongPageRequestException.of(requestPage, pageSize);
        }
    }
}
