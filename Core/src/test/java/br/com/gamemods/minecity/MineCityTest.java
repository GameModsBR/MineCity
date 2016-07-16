package br.com.gamemods.minecity;

import br.com.gamemods.minecity.api.BlockPos;
import br.com.gamemods.minecity.api.ChunkPos;
import br.com.gamemods.minecity.api.WorldDim;
import br.com.gamemods.minecity.structure.City;
import br.com.gamemods.minecity.structure.ClaimedChunk;
import br.com.gamemods.minecity.structure.Nature;
import br.com.gamemods.minecity.test.TestData;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MineCityTest
{
    private TestData test;
    private City city;
    private BlockPos spawn;

    @Before
    public void setUp() throws Exception
    {
        test = new TestData();
        spawn = new BlockPos(test.overWorld, 0,64,0);
        city = new City(test.mineCity, "Test City", test.joserobjr, spawn);
        city.create();
        test.mineCity.loadChunk(city.getSpawn().getChunk());
    }

    @Test
    public void testGetChunk() throws Exception
    {
        BlockPos off = spawn.subtract(1, 0, 1);
        assertNull(test.mineCity.getChunk(off));

        ClaimedChunk expected = new ClaimedChunk(test.mineCity.nature(spawn.world), off.getChunk());
        assertNull(test.mineCity.getChunk(off));

        assertEquals(expected, test.mineCity.loadChunk(off.getChunk()));
        assertEquals(expected, test.mineCity.getChunk(off));

        ChunkPos chunk = spawn.getChunk();
        expected = new ClaimedChunk(city.islands().iterator().next(), chunk);
        assertEquals(expected, test.mineCity.getChunk(spawn));
        assertEquals(expected, test.mineCity.getChunk(chunk));

        test.mineCity.unloadChunk(off.getChunk());
        assertNull(test.mineCity.getChunk(off));
    }

    @Test
    public void testGetNature() throws Exception
    {
        WorldDim nether = new WorldDim(-1, "nether", "Nether");
        assertNull(test.mineCity.getNature(nether));

        Nature nature = test.mineCity.nature(nether);
        assertNotNull(nature);
        assertEquals(test.mineCity.getNature(nether), nature);
        ChunkPos pos = new ChunkPos(nether, 0, 0);
        ClaimedChunk claim = test.mineCity.loadChunk(pos);
        assertNotNull(claim);
        assertEquals(nature, claim.owner);
        assertEquals(test.mineCity.unloadNature(nether), nature);
        assertNull(test.mineCity.getNature(nether));
        assertNull(test.mineCity.getChunk(pos));
    }
}