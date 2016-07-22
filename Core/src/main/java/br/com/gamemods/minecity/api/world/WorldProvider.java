package br.com.gamemods.minecity.api.world;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface WorldProvider
{
    @Nullable
    WorldDim getWorld(int dim, @NotNull String dir);

    Optional<ChunkProvider> getChunkProvider();
}
