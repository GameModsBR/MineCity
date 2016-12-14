package br.com.gamemods.minecity.reactive.game.block.data;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * An object that can collect block information about an object from the server software
 */
public interface BlockManipulator
{
    /**
     * Extract information about a block type.
     * @param block The server object that represents a block type.
     * @return The extracted data
     */
    @NotNull
    Optional<BlockTypeData> getBlockTypeData(@NotNull Object block);

    /**
     * Extract information about a block state.
     * @param blockState The server object that represents a block state.
     * @return The extracted data
     */
    @NotNull
    Optional<BlockStateData> getBlockStateData(@NotNull Object blockState);

    /**
     * Extract information about a tile entity.
     * @param tileEntity The server object that represents a tile entity
     * @return The extracted data
     */
    @NotNull
    Optional<TileEntityData> getTileEntityData(@NotNull Object tileEntity);

    /**
     * Extract information about a block trait.
     * @param blockTrait The server object that represents a block trait.
     * @return The extracted data
     */
    @NotNull
    Optional<BlockTraitData<?>> getBlockTraitData(@NotNull Object blockTrait);
}
