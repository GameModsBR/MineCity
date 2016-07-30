package br.com.gamemods.minecity.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Potentially slow methods
 */
@Documented
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface Slow
{
}
