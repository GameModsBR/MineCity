package br.com.gamemods.minecity.reactive.game.block.data.supplier;

import br.com.gamemods.minecity.reactive.game.block.data.BlockTypeData;
import org.jetbrains.annotations.NotNull;

/**
 * An object that can provide information about a block type
 */
@FunctionalInterface
public interface SupplierBlockTypeData
{
    /**
     * The block type data that is relative to this object.
     */
    @NotNull
    BlockTypeData getBlockTypeData();
}
