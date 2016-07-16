package br.com.gamemods.minecity.test;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.MineCityConfig;
import br.com.gamemods.minecity.PlayerID;
import br.com.gamemods.minecity.api.WorldDim;
import br.com.gamemods.minecity.test.datasource.FakeDataSource;

import java.util.UUID;

public class TestData
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
        mineCity = new MineCity(config, dataSource);
        dataSource.mineCity = mineCity;
    }
}
