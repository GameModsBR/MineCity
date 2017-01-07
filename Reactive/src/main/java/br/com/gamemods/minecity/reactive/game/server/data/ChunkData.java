package br.com.gamemods.minecity.reactive.game.server.data;

import br.com.gamemods.minecity.api.permission.Identity;
import br.com.gamemods.minecity.api.shape.Point;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.reactive.game.block.data.BlockStateData;
import br.com.gamemods.minecity.reactive.game.block.data.BlockTypeData;
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
     * A tile entity from the world checking this chunk first.
     * @param pos The coordinates are related to the world
     */
    @NotNull
    Optional<TileEntityData> getTileEntityData(Point pos);

    /**
     * Returns itself
     */
    @NotNull
    @Override
    default ChunkData getChunkData()
    {
        return this;
    }

    @NotNull
    BlockStateData getBlockStateData(BlockPos pos);

    @NotNull
    default BlockTypeData getBlockTypeData(BlockPos pos)
    {
        return getBlockStateData(pos).getBlockTypeData();
    }

    boolean setAir(Point pos);

    boolean setBlock(Point pos, BlockStateData state);

    boolean drop(Point pos);

    boolean dropAsItem(Point pos);

    Identity<?> getOwner(BlockPos pos);
}
