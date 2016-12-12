package br.com.gamemods.minecity.reactive.game.block.data.supplier;

import br.com.gamemods.minecity.reactive.game.block.data.TileEntityData;
import org.jetbrains.annotations.NotNull;

/**
 * An object that can provide information about a tile entity
 */
@FunctionalInterface
public interface SupplierTileEntityData
{
    /**
     * The tile entity data relative to this object
     */
    @NotNull
    TileEntityData getTileEntityData();
}
