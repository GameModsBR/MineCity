package br.com.gamemods.minecity.structure;

import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.datasource.api.DataSourceException;
import br.com.gamemods.minecity.datasource.test.TestData;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

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
    public void testDisclaim() throws Exception
    {
        BlockPos spawn = new BlockPos(test.overWorld, 200,64,100);
        ChunkPos spawnChunk = spawn.getChunk();
        City city = new City(test.mineCity, "Disclaim", test.joserobjr, spawn);
        city.create();
        Island spawnIsland = city.islands().iterator().next();

        ChunkPos chunk = spawnChunk.add(Direction.NORTH);
        assertEquals(spawnIsland, city.claim(chunk, false));
        assertEquals(Collections.singleton(spawnIsland), city.disclaim(chunk, false));
        assertEquals(1, city.getSizeX());
        assertEquals(1, city.getSizeZ());
        assertEquals(1, city.getChunkCount());

        chunk = spawnChunk.add(Direction.SOUTH, 5);
        Island islandA = city.claim(chunk, true);
        assertNotEquals(spawnIsland, islandA);
        assertEquals(2, city.getSizeX());
        assertEquals(2, city.getSizeZ());
        assertEquals(2, city.getChunkCount());
        assertEquals(Arrays.asList(spawnIsland, islandA), new ArrayList<>(city.islands()));

        assertEquals(Collections.singleton(islandA), city.disclaim(chunk, false));
        assertEquals(1, city.getSizeX());
        assertEquals(1, city.getSizeZ());
        assertEquals(1, city.getChunkCount());
        assertEquals(Collections.singletonList(spawnIsland), new ArrayList<>(city.islands()));

        Island islandB = city.claim(chunk, true);
        assertNotEquals(islandA, islandB);
        assertEquals(2, city.getSizeX());
        assertEquals(2, city.getSizeZ());
        assertEquals(2, city.getChunkCount());
        assertEquals(Arrays.asList(spawnIsland, islandB), new ArrayList<>(city.islands()));

        assertEquals(islandB, city.claim(chunk.add(Direction.EAST), false));
        assertEquals(3, city.getSizeX());
        assertEquals(2, city.getSizeZ());
        assertEquals(3, city.getChunkCount());
        assertEquals(Collections.singleton(islandB), city.disclaim(chunk.add(Direction.EAST), false));
        assertEquals(2, city.getSizeX());
        assertEquals(2, city.getSizeZ());
        assertEquals(2, city.getChunkCount());

        /*
         *     X →
         *     123456789
         * Z-1|   X     |
         * ↓ 0|   XX    |
         *   1| XXDXXXX |
         *   4|   X     |
         *   5|   XX    |
         *   6|   X     |
         */
        assertEquals(islandB, city.claim(chunk.add(Direction.EAST), false));
        chunk = chunk.add(Direction.EAST, 2);
        assertEquals(islandB, city.claim(chunk, false));
        assertEquals(islandB, city.claim(chunk.add(Direction.EAST), false));
        assertEquals(islandB, city.claim(chunk.add(Direction.EAST, 2), false));
        assertEquals(islandB, city.claim(chunk.add(Direction.EAST, 3), false));
        assertEquals(islandB, city.claim(chunk.add(Direction.EAST, 4), false));
        assertEquals(islandB, city.claim(chunk.add(Direction.NORTH), false));
        assertEquals(islandB, city.claim(chunk.add(Direction.NORTH, 2), false));
        assertEquals(islandB, city.claim(chunk.add(Direction.NORTH_EAST), false));
        assertEquals(islandB, city.claim(chunk.add(Direction.SOUTH), false));
        assertEquals(islandB, city.claim(chunk.add(Direction.SOUTH, 2), false));
        assertEquals(islandB, city.claim(chunk.add(Direction.SOUTH, 2).add(Direction.EAST), false));
        assertEquals(islandB, city.claim(chunk.add(Direction.SOUTH, 3), false));
        assertEquals(14, islandB.getChunkCount());
        assertEquals(7, islandB.getSizeX());
        assertEquals(6, islandB.getSizeZ());
        assertEquals(15, city.getChunkCount());
        assertEquals(8, city.getSizeX());
        assertEquals(7, city.getSizeZ());
        assertEquals(2, city.islands().size());

        /*
         *     X →
         *     123456789
         * Z-1|   B     |
         * ↓ 0|   BB    |
         *   1| XX BBBB |
         *   4|   Y     |
         *   5|   YY    |
         *   6|   Y     |
         */
        Collection<Island> islands = city.disclaim(new ChunkPos(test.overWorld, 14, 1), true);
        assertEquals(2, islands.size());
        Island islandX = islands.stream().min((a,b)-> a.getChunkCount()-b.getChunkCount()).get();
        Island islandY = islands.stream().filter(i-> i != islandX).findAny().get();
        assertEquals(2, islandX.getChunkCount());
        assertEquals(4, islandY.getChunkCount());
        assertEquals(7, islandB.getChunkCount());
        assertEquals(5, islandB.getSizeX());
        assertEquals(3, islandB.getSizeZ());
        assertEquals(14, city.getChunkCount());
        assertEquals(10, city.getSizeX());
        assertEquals(8, city.getSizeZ());
        assertEquals(4, city.islands().size());
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