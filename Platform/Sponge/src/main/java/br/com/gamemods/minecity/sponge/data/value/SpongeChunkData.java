package br.com.gamemods.minecity.sponge.data.value;

import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.reactive.game.block.data.BlockStateData;
import br.com.gamemods.minecity.reactive.game.block.data.TileEntityData;
import br.com.gamemods.minecity.reactive.game.server.data.ChunkData;
import br.com.gamemods.minecity.reactive.game.server.data.WorldData;
import br.com.gamemods.minecity.sponge.data.manipulator.reactive.SpongeManipulator;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.extent.BlockVolume;

import java.util.Optional;

public class SpongeChunkData implements ChunkData
{
    private final SpongeManipulator manipulator;
    private final Chunk chunk;
    private final ChunkPos pos;

    public SpongeChunkData(SpongeManipulator manipulator, Chunk chunk)
    {
        this.manipulator = manipulator;
        this.chunk = chunk;

        this.pos = manipulator.sponge.chunk(chunk);
    }

    @Override
    public Chunk getChunk()
    {
        return chunk;
    }

    @NotNull
    @Override
    public ChunkPos getChunkPos()
    {
        return pos;
    }

    @NotNull
    @Override
    public WorldData getWorldData()
    {
        return manipulator.server.getWorldData(chunk.getWorld());
    }

    @NotNull
    @Override
    public Optional<TileEntityData> getTileEntityData(BlockPos pos) throws IndexOutOfBoundsException
    {
        return Optional.empty();
    }

    @NotNull
    @Override
    public BlockStateData getBlockStateData(BlockPos pos)
    {
        BlockVolume volume = chunk.containsBlock(pos.x, pos.y, pos.z)? chunk : chunk.getWorld();
        return manipulator.block.getBlockStateData(volume.getBlock(pos.x, pos.y, pos.z));
    }
}
