package dev.anvilcraft.rg.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Rule {
    RGEnvironment env() default RGEnvironment.SERVER;

    String description() default "";

    String serialize() default "";

    String[] preset() default {};

    Class<? extends RGValidator> validator() default RGValidator.DefaultValidator.class;
}
