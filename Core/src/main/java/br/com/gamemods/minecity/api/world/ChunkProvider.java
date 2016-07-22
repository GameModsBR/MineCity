package br.com.gamemods.minecity.api.world;

import br.com.gamemods.minecity.structure.ClaimedChunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public interface ChunkProvider
{
    @Nullable
    ChunkPos getChunk(@NotNull WorldDim world,  int x, int z);

    @Nullable
    ClaimedChunk getClaim(@NotNull WorldDim world, int x, int z);

    @Nullable
    ClaimedChunk getClaim(@NotNull ChunkPos pos);

    boolean setClaim(@NotNull ClaimedChunk claim);

    @NotNull
    Stream<ClaimedChunk> loadedChunks();
}
