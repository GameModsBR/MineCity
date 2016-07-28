package br.com.gamemods.minecity.api.command;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface Command
{
    String value();
    boolean console() default true;
    Arg[] args() default {};
}
