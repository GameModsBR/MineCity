package br.com.gamemods.minecity.reactive.game.block.data;

import br.com.gamemods.minecity.reactive.ReactiveLayer;
import br.com.gamemods.minecity.reactive.game.block.ReactiveTileEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Information about a tile entity
 */
public interface TileEntityData
{
    Object getTileEntity();

    /**
     * The reactive object that will react to events related to this tile entity.
     */
    @NotNull
    default Optional<ReactiveTileEntity> getReactiveTileEntity()
    {
        return ReactiveLayer.getReactor().getBlockReactor().getTileEntity(this);
    }
}
