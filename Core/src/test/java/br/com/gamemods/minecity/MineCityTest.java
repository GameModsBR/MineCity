package br.com.gamemods.minecity;

import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.datasource.test.TestData;
import br.com.gamemods.minecity.structure.City;
import br.com.gamemods.minecity.structure.ClaimedChunk;
import br.com.gamemods.minecity.structure.Nature;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

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
        test.mineCity.loadChunk(city.getSpawn().getChunk());
    }

    @Test
    public void testGetChunk() throws Exception
    {
        BlockPos off = spawn.subtract(1, 0, 1);
        assertEquals(Optional.empty(), test.mineCity.getChunk(off));

        Optional<ClaimedChunk> expected = Optional.of(new ClaimedChunk(test.mineCity.nature(spawn.world), off.getChunk()));
        assertEquals(Optional.empty(), test.mineCity.getChunk(off));

        assertEquals(expected.get(), test.mineCity.loadChunk(off.getChunk()));
        assertEquals(expected, test.mineCity.getChunk(off));

        ChunkPos chunk = spawn.getChunk();
        expected = Optional.of(new ClaimedChunk(city.islands().iterator().next(), chunk));
        assertEquals(expected, test.mineCity.getChunk(spawn));
        assertEquals(expected, test.mineCity.getChunk(chunk));

        test.mineCity.unloadChunk(off.getChunk());
        assertEquals(Optional.empty(), test.mineCity.getChunk(off));
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
        assertEquals(Optional.empty(), test.mineCity.getChunk(pos));
    }
}