package br.com.gamemods.minecity.reactive.game.server.data;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface ServerManipulator
{
    @NotNull
    Optional<ChunkData> getChunkData(@NotNull Object chunk);

    @NotNull
    Optional<WorldData> getWorldData(@NotNull Object world);

    @NotNull
    Optional<ServerData> getServerData(@NotNull Object server);
}
