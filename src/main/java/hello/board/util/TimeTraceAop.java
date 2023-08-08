package hello.board.util;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class TimeTraceAop {

    @Around("execution(* hello.board.service..*(..))")
    public Object execute(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        log.info("Start={}", joinPoint);
        try {
            return joinPoint.proceed();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            long finish = System.currentTimeMillis();
            long timeMs = finish - start;
            log.info("End={} {}ms", joinPoint, timeMs);
        }

    }
}
