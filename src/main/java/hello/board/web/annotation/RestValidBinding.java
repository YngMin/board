package hello.board.web.annotation;

import org.springframework.web.bind.annotation.ResponseBody;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@ResponseBody
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RestValidBinding {

}
