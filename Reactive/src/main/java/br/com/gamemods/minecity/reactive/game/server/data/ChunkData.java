package br.com.gamemods.minecity.reactive.game.server.data;

import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.reactive.game.block.data.TileEntityData;
import br.com.gamemods.minecity.reactive.game.server.data.supplier.SupplierChunkData;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * The information about a chunk
 */
public interface ChunkData extends SupplierChunkData
{
    Object getChunk();

    /**
     * The chunk position.
     * @return the associated WorldDim must contains an object that is accepted by {@link ServerManipulator#getWorldData(Object)}
     */
    @NotNull
    ChunkPos getChunkPos();

    /**
     * The world where this chunk is located
     */
    @NotNull
    WorldData getWorldData();

    /**
     * A tile entity that is inside this chunk.
     * @param pos The coordinates are related to the world
     * @throws IndexOutOfBoundsException If the position is not inside this chunk
     */
    @NotNull
    Optional<TileEntityData> getTileEntityData(BlockPos pos) throws IndexOutOfBoundsException;

    /**
     * Returns itself
     */
    @NotNull
    @Override
    default ChunkData getChunkData()
    {
        return this;
    }
}
