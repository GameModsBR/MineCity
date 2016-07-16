package br.com.gamemods.minecity.structure;

import br.com.gamemods.minecity.api.BlockPos;
import br.com.gamemods.minecity.datasource.DataSourceException;
import br.com.gamemods.minecity.test.TestData;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CityTest
{
    private TestData test;
    @Before
    public void setUp() throws Exception
    {
        test = new TestData();
    }

    @Test
    public void testAA_CreateCity() throws DataSourceException
    {
        City city = new City(test.mineCity, "Test City", test.joserobjr, new BlockPos(test.overWorld, 0, 64, 0));
        city.create();
        assertEquals("ID not set", city.getId(), 1);

        assertEquals(test.mineCity.loadChunk(city.getSpawn().getChunk()), new ClaimedChunk(city.islands().iterator().next(), city.getSpawn().getChunk()));
    }
}