package br.com.gamemods.minecity.forge.base.accessors;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * A method that does not have a {@code final} keyword but cannot be overridden by accessors because it would cause
 * a conflict with overrides from specific versions
 */
@Target(ElementType.METHOD)
public @interface Final
{
}
