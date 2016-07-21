package br.com.gamemods.minecity.datasource.test;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.MineCityConfig;
import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.Server;
import br.com.gamemods.minecity.api.command.MessageTransformer;
import br.com.gamemods.minecity.api.world.WorldDim;

import java.util.Optional;
import java.util.UUID;

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
        dataSource.mineCity = mineCity;
    }

    @Override
    public Optional<PlayerID> getPlayerId(String name)
    {
        return Optional.empty();
    }
}
