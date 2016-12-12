package br.com.gamemods.minecity.reactive.game.server;

import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.reactive.game.block.ReactiveTileEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface ReactiveChunk
{
    @NotNull
    ChunkPos getChunkPos();

    ReactiveWorld getReactiveWorld();
    Optional<ReactiveTileEntity> getReactiveTileEntity(BlockPos pos);
}
