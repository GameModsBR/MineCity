package br.com.gamemods.minecity.api.command;

import java.lang.annotation.*;

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

    enum Type
    {
        UNDEFINED,
        PREDEFINED,
        PLAYER,
        CITY
    }
}
