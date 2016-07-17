package br.com.gamemods.minecity.structure;

import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.datasource.api.DataSourceException;
import br.com.gamemods.minecity.datasource.test.TestData;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

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
    public void testCreateCity() throws DataSourceException
    {
        BlockPos spawn = new BlockPos(test.overWorld, 0, 64, 0);
        City city = new City(test.mineCity, "Test City", test.joserobjr, spawn);
        city.create();
        assertTrue(city.getId() > 0);
        assertEquals(test.joserobjr, city.getOwner());
        assertEquals(spawn, city.getSpawn());
        assertEquals(1, city.getSizeX());
        assertEquals(1, city.getSizeZ());
        assertEquals(1, city.getChunkCount());
        assertEquals(1, city.islands().size());
        assertEquals("Test City", city.getName());

        Island island = city.islands().iterator().next();
        assertEquals(city, island.getCity());
        assertEquals(1, island.getSizeX());
        assertEquals(1, island.getSizeZ());
        assertEquals(1, island.getChunkCount());
        assertTrue(island.getId() > 0);

        assertEquals(test.mineCity.loadChunk(city.getSpawn().getChunk()), new ClaimedChunk(
                island, city.getSpawn().getChunk()));
    }

    @Test
    public void testClaim() throws DataSourceException
    {
        BlockPos spawn = new BlockPos(test.overWorld, 250, 32, -200);
        ChunkPos chunk = spawn.getChunk();
        City city = new City(test.mineCity, "City 2", test.joserobjr, spawn);
        city.create();

        Island islandA = city.islands().iterator().next();
        assertEquals(islandA, city.claim(chunk.add(Direction.NORTH), false));
        assertEquals(2, islandA.getChunkCount());
        assertEquals(2, islandA.getSizeZ());
        assertEquals(1, islandA.getSizeX());
        assertEquals(2, city.getChunkCount());
        assertEquals(2, city.getSizeZ());
        assertEquals(1, city.getSizeX());

        assertEquals(islandA, city.claim(chunk.add(Direction.WEST), false));
        assertEquals(3, islandA.getChunkCount());
        assertEquals(2, islandA.getSizeZ());
        assertEquals(2, islandA.getSizeX());
        assertEquals(3, city.getChunkCount());
        assertEquals(2, city.getSizeZ());
        assertEquals(2, city.getSizeX());

        Island islandB = city.claim(chunk.add(Direction.EAST, 2), true);
        assertNotEquals(islandA, islandB);
        assertEquals(1, islandB.getChunkCount());
        assertEquals(1, islandB.getSizeZ());
        assertEquals(1, islandB.getSizeX());
        assertEquals(4, city.getChunkCount());
        assertEquals(3, city.getSizeZ());
        assertEquals(3, city.getSizeX());
        assertEquals(2, city.islands().size());

        assertEquals(islandA, city.claim(chunk.add(Direction.EAST), false));
        assertEquals(5, islandA.getChunkCount());
        assertEquals(2, islandA.getSizeZ());
        assertEquals(4, islandA.getSizeX());
        assertEquals(5, city.getChunkCount());
        assertEquals(2, city.getSizeZ());
        assertEquals(4, city.getSizeX());
        assertEquals(1, city.islands().size());
        assertEquals(0, islandB.getChunkCount());
        assertEquals(0, islandB.getSizeX());
        assertEquals(0, islandB.getSizeX());
    }
}