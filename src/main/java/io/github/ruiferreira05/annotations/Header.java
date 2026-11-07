package io.github.ruiferreira05.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates a header in the menu. Will be printed before the list of options.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Header {
    String title() default "";
    boolean showTitle() default false;
}
