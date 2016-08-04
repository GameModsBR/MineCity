package br.com.gamemods.minecity.api;

import br.com.gamemods.minecity.MineCity;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.stream.Stream;

public interface Server
{
    MineCity getMineCity();
    Optional<PlayerID> getPlayerId(String name);
    Stream<PlayerID> getOnlinePlayers();
    Stream<String> getOnlinePlayerNames();

    void runAsynchronously(Runnable runnable);

    <R> Future<R> callSyncMethod(Callable<R> callable);

    default <R> Future<R> callSyncMethod(Runnable runnable, R result)
    {
        return callSyncMethod(()-> {
            runnable.run();
            return result;
        });
    }

    default Future<Void> callSyncMethod(Runnable runnable)
    {
        return callSyncMethod(runnable, null);
    }
}
