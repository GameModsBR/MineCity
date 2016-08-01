package br.com.gamemods.minecity.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * A method or constructor that must be executed in the primary thread (Server Thread)
 */
@Documented
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface Sync
{
}
