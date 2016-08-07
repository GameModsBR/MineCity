package br.com.gamemods.minecity.api.command;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({})
@Documented
public @interface Arg
{
    String name();
    Type type() default Type.UNDEFINED;
    boolean sticky() default false;
    boolean optional() default false;
    String[] options() default {};
    String relative() default "";

    enum Type
    {
        UNDEFINED,
        PREDEFINED,
        PLAYER,
        CITY,
        GROUP,
        GROUP_OR_CITY
    }
}
