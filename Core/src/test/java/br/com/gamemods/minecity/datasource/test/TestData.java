package br.com.gamemods.minecity.datasource.test;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.MineCityConfig;
import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.Server;
import br.com.gamemods.minecity.api.command.MessageTransformer;
import br.com.gamemods.minecity.api.world.WorldDim;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.stream.Stream;

public class TestData implements Server
{
    public FakeDataSource dataSource;
    public MineCityConfig config;
    public MineCity mineCity;
    public PlayerID joserobjr = new PlayerID(UUID.fromString("185e2176-0095-4ff8-a201-6f2aed9a032a"), "joserobjr");
    public WorldDim overWorld = new WorldDim(0, "world", "OverWorld");

    public TestData()
    {
        config = new MineCityConfig();
        dataSource = new FakeDataSource();
        mineCity = new MineCity(this, config, dataSource, new MessageTransformer());
        mineCity.lazyReloads = false;
        dataSource.mineCity = mineCity;
    }

    @Override
    public MineCity getMineCity()
    {
        return mineCity;
    }

    @Override
    public Optional<PlayerID> getPlayerId(String name)
    {
        return Optional.empty();
    }

    @Override
    public Stream<PlayerID> getOnlinePlayers()
    {
        return Stream.empty();
    }

    @Override
    public Stream<String> getOnlinePlayerNames()
    {
        return Stream.empty();
    }

    @Override
    public <R> Future<R> callSyncMethod(Callable<R> callable)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void runAsynchronously(Runnable runnable)
    {
        throw new UnsupportedOperationException();
    }
}
