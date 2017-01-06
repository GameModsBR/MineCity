package br.com.gamemods.minecity.reactive.game.block.data;

import br.com.gamemods.minecity.reactive.ReactiveLayer;
import br.com.gamemods.minecity.reactive.game.block.ReactiveBlockState;
import br.com.gamemods.minecity.reactive.game.block.ReactiveBlockTrait;
import br.com.gamemods.minecity.reactive.game.block.data.supplier.SupplierBlockStateData;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The information about a block state
 */
public interface BlockStateData extends SupplierBlockStateData
{
    Object getBlockState();

    /**
     * The reactive object that will react to events related to this state.
     */
    @NotNull
    default Optional<ReactiveBlockState> getReactiveBlockState()
    {
        return ReactiveLayer.getReactor().getBlockReactor().getReactiveBlockState(this);
    }

    /**
     * @see ReactiveBlockState#getBlockRole()
     */
    @NotNull
    default BlockRole getBlockStateRole()
    {
        return getReactiveBlockState().flatMap(ReactiveBlockState::getBlockRole)
                .orElseGet(()-> getBlockTypeData().getBlockTypeRole());
    }

    /**
     * The string that represents this block state.  Might not be available on all server implementations.
     */
    @NotNull
    default Optional<String> getStateIdName()
    {
        return Optional.empty();
    }

    /**
     * The integer that represents this block state. Might not be available on all server implementations.
     */
    @NotNull
    default Optional<Integer> getStateId()
    {
        return Optional.empty();
    }

    /**
     * The base block of this state.
     */
    @NotNull
    BlockTypeData getBlockTypeData();

    @NotNull
    <V extends Comparable<V>> Optional<V> getTrait(BlockTraitData<V> trait);

    @NotNull
    default Optional<?> getTrait(String traitId)
    {
        return ReactiveLayer.getBlockTrait(traitId).flatMap(this::getTrait);
    }

    @NotNull
    default String getTraitString(String traitId)
    {
        return getTrait(traitId).map(Object::toString).orElse("");
    }

    <T extends Comparable<T>> Optional<BlockStateData> withTrait(BlockTraitData<T> trait, T value);

    default <T extends Comparable<T>> Optional<BlockStateData> withTrait(String traitId, Object value)
    {
        if(value == null)
            return Optional.empty();

        @SuppressWarnings("unchecked")
        BlockTraitData<T> trait = (BlockTraitData) ReactiveLayer.getBlockTrait(traitId).orElse(null);
        if(trait == null)
            return Optional.empty();

        Function<Object, Object> caster;
        Class<T> valueClass = trait.getValueClass();
        if(valueClass.isInstance(value))
            caster = Function.identity();
        else if(valueClass.isAssignableFrom(Integer.class))
            caster = obj -> Integer.parseInt(obj.toString());
        else if(valueClass.isAssignableFrom(Boolean.class))
            caster = obj -> Boolean.parseBoolean(obj.toString());
        else
            return Optional.empty();

        try
        {
            value = caster.apply(value);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return Optional.empty();
        }

        if(value == null)
            return Optional.empty();

        T casted;
        try
        {
            casted = valueClass.cast(value);
        }
        catch(ClassCastException e)
        {
            e.printStackTrace();
            return Optional.empty();
        }

        return withTrait(trait, casted);
    }

    /**
     * The traits presents in this state.
     */
    @NotNull
    default Collection<BlockTraitData<?>> getBlockTraits()
    {
        return blockTraitStream().collect(Collectors.toList());
    }

    Stream<BlockTraitData<?>> blockTraitStream();

    /**
     * A stream containing all traits presents in this state that can react to events.
     */
    @NotNull
    default Stream<ReactiveBlockTrait<?>> reactiveBlockTraitStream()
    {
        return blockTraitStream().map(BlockTraitData::getReactiveBlockTrait)
                .filter(Optional::isPresent).map(Optional::get);
    }

    /**
     * Returns itself
     */
    @NotNull
    @Override
    default BlockStateData getBlockStateData()
    {
        return this;
    }

    boolean isReplaceable();
}
