package io.github.ruiferreira05.annotations.form;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.RECORD_COMPONENT)
public @interface FormProperty {
    String prompt() default "";
    boolean acceptEmpty() default true;
}
