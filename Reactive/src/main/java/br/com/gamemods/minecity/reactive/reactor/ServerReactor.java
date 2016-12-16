package br.com.gamemods.minecity.reactive.reactor;

import br.com.gamemods.minecity.reactive.game.server.ReactiveChunk;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface ServerReactor
{
    @NotNull
    Optional<ReactiveChunk> getReactiveChunk(Object chunk);
}
