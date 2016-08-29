package br.com.gamemods.minecity.forge.base;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Indicates that a class transformer changes an existing method
 */
@Target(ElementType.TYPE)
public @interface MethodPatcher
{
}
