package br.com.gamemods.minecity.forge.base.accessors;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.IBlockStateTransformer;
import net.minecraft.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.IntUnaryOperator;

@Referenced(at = IBlockStateTransformer.class)
public interface IState
{
    IBlock getIBlock();
    Block getForgeBlock();

    boolean isOpaqueCube();

    int getStateId();

    Collection<IProp<?>> getPropertyKeys();

    <T extends Comparable<T>> T getValue(IProp<T> prop);

    Map<IProp<?>, Comparable<?>> getProps();

    @Nullable
    default <T extends Comparable<T>> T getValue(String key, Function<Object, T> cast)
    {
        for(IProp<?> prop: getPropertyKeys())
            if(prop.getName().equals(key))
                return cast.apply(getValue(prop));

        return null;
    }

    default <T extends Comparable<T>> T getValueOrMeta(String key, Function<Object, T> cast, IntFunction<T> metaConverter)
    {
        for(IProp<?> prop: getPropertyKeys())
        {
            String name = prop.getName();
            if(name.equals(key))
                return cast.apply(getValue(prop));

            if(name.equals("metadata"))
                return metaConverter.apply( (Integer) getValue(prop));
        }

        throw new NoSuchElementException("The property neither "+key+" or metadata properties were found in "+this);
    }

    default int getIntValueOrMeta(String key, IntUnaryOperator metaConverter)
    {
        for(IProp<?> prop: getPropertyKeys())
        {
            String name = prop.getName();
            if(name.equals(key))
                return (Integer) getValue(prop);

            if(name.equals("metadata"))
                return metaConverter.applyAsInt( (Integer) getValue(prop));
        }

        throw new NoSuchElementException("The property neither "+key+" or metadata properties were found in "+this);
    }

    default int getIntValueOrMeta(String key)
    {
        return getIntValueOrMeta(key, IntUnaryOperator.identity());
    }

    default Integer getIntValue(String key)
    {
        return getValue(key, Integer.class::cast);
    }

    @SuppressWarnings("unchecked")
    default Enum<?> getEnumValue(String key)
    {
        return getValue(key, Enum.class::cast);
    }

    default Integer getEnumOrdinalValue(String key)
    {
        return getValue(key, o -> Enum.class.cast(o).ordinal());
    }

    default int getEnumOrdinalOrMeta(String key, IntUnaryOperator metaConverter)
    {
        return getValueOrMeta(key, o -> Enum.class.cast(o).ordinal(), metaConverter::applyAsInt);
    }

    default int getEnumOrdinalOrMeta(String key)
    {
        return getEnumOrdinalOrMeta(key, IntUnaryOperator.identity());
    }
}
