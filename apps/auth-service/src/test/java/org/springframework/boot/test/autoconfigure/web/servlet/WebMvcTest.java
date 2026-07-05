package org.springframework.boot.test.autoconfigure.web.servlet;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WebMvcTest {
    Class<?>[] value() default {};
}
