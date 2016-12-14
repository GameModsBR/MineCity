package br.com.gamemods.minecity.reactive.game.block.data;

import br.com.gamemods.minecity.reactive.ReactiveLayer;
import br.com.gamemods.minecity.reactive.game.block.ReactiveBlockType;
import br.com.gamemods.minecity.reactive.game.block.data.supplier.SupplierBlockTypeData;
import br.com.gamemods.minecity.reactive.game.item.data.ItemData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Information about a general block, the base of the block states.
 */
public interface BlockTypeData extends SupplierBlockTypeData
{
    Object getBlockType();

    BlockStateData getDefaultBlockStateData();

    Optional<ItemData> getItemData();

    Optional<BlockTraitData<?>> getTraitData(String traitId);

    /**
     * The reactive object that will react to events related to this block type.
     */
    @NotNull
    default Optional<ReactiveBlockType> getReactiveBlockType()
    {
        return ReactiveLayer.getReactor().getBlockReactor().getBlockType(this);
    }

    default void setReactive(@Nullable ReactiveBlockType reactiveBlock)
    {
        System.out.println("Interface SET! ");
    }

    /**
     * @see ReactiveBlockType#getBlockRole()
     */
    @NotNull
    default BlockRole getBlockTypeRole()
    {
        return getReactiveBlockType().map(ReactiveBlockType::getBlockRole).orElse(BlockRole.DECORATIVE);
    }

    /**
     * The string that represents this block type. Might not be available on all server implementations.
     */
    @NotNull
    default Optional<String> getBlockIdName()
    {
        return Optional.empty();
    }

    /**
     * The integer that represents this block. Might not be available on all server implementations.
     */
    @NotNull
    default Optional<Integer> getBlockId()
    {
        return Optional.empty();
    }

    /**
     * Returns itself
     */
    @NotNull
    @Override
    default BlockTypeData getBlockTypeData()
    {
        return this;
    }
}
