package br.com.gamemods.minecity.api;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.stream.Stream;

public interface Server
{
    Optional<PlayerID> getPlayerId(String name);
    Stream<PlayerID> getOnlinePlayers();
    Stream<String> getOnlinePlayerNames();

    <R> Future<R> callSyncMethod(Callable<R> callable);
    void runAsynchronously(Runnable runnable);
}
