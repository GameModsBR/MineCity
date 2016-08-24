package br.com.gamemods.minecity.forge.base;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;

@Documented
@Repeatable(References.class)
public @interface Referenced
{
    String value() default "";
    Class<?> at() default Void.class;
}
