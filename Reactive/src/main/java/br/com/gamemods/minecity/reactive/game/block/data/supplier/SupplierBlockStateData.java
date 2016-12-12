package br.com.gamemods.minecity.reactive.game.block.data.supplier;

import br.com.gamemods.minecity.reactive.game.block.data.BlockStateData;
import br.com.gamemods.minecity.reactive.game.block.data.BlockTypeData;
import org.jetbrains.annotations.NotNull;

/**
 * An object that can provide information about a block state
 */
@FunctionalInterface
public interface SupplierBlockStateData extends SupplierBlockTypeData
{
    /**
     * The block state data that is relative to this object.
     */
    @NotNull
    BlockStateData getBlockStateData();

    @NotNull
    @Override
    default BlockTypeData getBlockTypeData()
    {
        return getBlockStateData().getBlockTypeData();
    }
}
