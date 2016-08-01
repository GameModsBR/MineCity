package br.com.gamemods.minecity.api;

import java.lang.annotation.*;

/**
 * Indicates methods that should be executed asynchronously (outside the main server thread).
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface Async
{
}
