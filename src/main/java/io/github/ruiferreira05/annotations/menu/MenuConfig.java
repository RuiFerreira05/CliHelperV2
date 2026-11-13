package io.github.ruiferreira05.annotations.menu;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allows a user to configure a menu. Is required for all menus.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MenuConfig {
    String title() default "";
    String prompt() default ">";
}
