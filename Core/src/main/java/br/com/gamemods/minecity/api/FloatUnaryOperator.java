package br.com.gamemods.minecity.api;

import java.util.Objects;

/**
 * @see java.util.function.IntUnaryOperator
 */
@FunctionalInterface
public interface FloatUnaryOperator
{
    static FloatUnaryOperator identity()
    {
        return t -> t;
    }

    float applyAsFloat(float operand);

    default FloatUnaryOperator compose(FloatUnaryOperator before)
    {
        Objects.requireNonNull(before);
        return (float v) -> applyAsFloat(before.applyAsFloat(v));
    }

    default FloatUnaryOperator andThen(FloatUnaryOperator after)
    {
        Objects.requireNonNull(after);
        return (float t) -> after.applyAsFloat(applyAsFloat(t));
    }
}
