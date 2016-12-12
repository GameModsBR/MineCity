package br.com.gamemods.minecity.reactive.game.block.data;

import br.com.gamemods.minecity.reactive.ReactiveLayer;
import br.com.gamemods.minecity.reactive.game.block.ReactiveBlockState;
import br.com.gamemods.minecity.reactive.game.block.ReactiveBlockTrait;
import br.com.gamemods.minecity.reactive.game.block.data.supplier.SupplierBlockStateData;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;
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
        return ReactiveLayer.getReactor().getBlockReactor().getBlockState(this);
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

    /**
     * The traits presents in this state.
     */
    @NotNull
    default Collection<BlockTraitData<?>> getBlockTraits()
    {
        return traitStream().collect(Collectors.toList());
    }

    Stream<BlockTraitData<?>> traitStream();

    /**
     * A stream containing all traits presents in this state that can react to events.
     */
    @NotNull
    default Stream<ReactiveBlockTrait<?>> reactiveTraitStream()
    {
        return traitStream().map(BlockTraitData::getReactiveBlockTrait)
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
}
