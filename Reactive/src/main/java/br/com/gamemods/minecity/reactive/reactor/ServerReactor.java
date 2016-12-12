package br.com.gamemods.minecity.reactive.reactor;

import br.com.gamemods.minecity.reactive.game.server.ReactiveChunk;
import br.com.gamemods.minecity.reactive.game.server.data.ServerData;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface ServerReactor
{
    @NotNull
    Optional<ReactiveChunk> getChunk(Object chunk);

    @NotNull
    Optional<ServerData> getServer(Object server);
}
