package br.com.gamemods.minecity.structure;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.datasource.api.DataSourceException;
import br.com.gamemods.minecity.datasource.test.TestData;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static com.github.kolorobot.exceptions.java8.AssertJThrowableAssert.assertThrown;
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
    @SuppressWarnings("SpellCheckingInspection")
    public void testDisclaim() throws Exception
    {
        BlockPos spawn = new BlockPos(test.overWorld, 200,64,100);
        ChunkPos spawnChunk = spawn.getChunk();
        City city = new City(test.mineCity, "Disclaim", test.joserobjr, spawn);
        Island spawnIsland = city.islands().iterator().next();

        assertThrown(()-> city.disclaim(spawnChunk, false))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("last");


        ChunkPos chunk = spawnChunk.add(Direction.NORTH);
        assertEquals(spawnIsland, city.claim(chunk, false));

        assertThrown(()-> city.disclaim(spawnChunk, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("spawn");

        assertThrown(()-> city.claim(spawnChunk, false))
                .hasMessageContaining("reserved")
                .isInstanceOf(IllegalArgumentException.class);

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
        assertThrown(()-> city.disclaim(new ChunkPos(test.overWorld, 14, 1), false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("required");

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

        city.islands().forEach(island -> assertEquals(island, city.getIsland(island.getId())));
    }

    @Test
    public void testCreateCity() throws DataSourceException
    {
        BlockPos spawn = new BlockPos(test.overWorld, 0, 64, 0);
        test.mineCity.loadNature(spawn.world);
        test.mineCity.loadChunk(spawn.getChunk());

        City city = new City(test.mineCity, "Test City", test.joserobjr, spawn);
        assertTrue(city.getId() > 0);
        assertEquals(test.joserobjr, city.getOwner());
        assertEquals(spawn, city.getSpawn());
        assertEquals(1, city.getSizeX());
        assertEquals(1, city.getSizeZ());
        assertEquals(1, city.getChunkCount());
        assertEquals(1, city.islands().size());
        assertEquals("Test City", city.getName());
        assertEquals(city, test.mineCity.getChunk(spawn.getChunk()).flatMap(ClaimedChunk::getCity).orElse(null));

        Island island = city.islands().iterator().next();
        assertEquals(city, island.getCity());
        assertEquals(1, island.getSizeX());
        assertEquals(1, island.getSizeZ());
        assertEquals(1, island.getChunkCount());
        assertTrue(island.getId() > 0);
        assertEquals(island, city.getIsland(island.getId()));

        assertEquals(test.mineCity.loadChunk(city.getSpawn().getChunk()), new ClaimedChunk(
                island, city.getSpawn().getChunk()));

        assertThrown(()-> new City(test.mineCity, "Bad City", test.joserobjr, spawn))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("reserved");

    }

    @Test
    public void testSetId() throws Exception
    {
        City badCity = new City(test.mineCity, "Bad City", test.joserobjr, new BlockPos(test.overWorld, 400,40, 65));
        assertThrown(()-> badCity.setId(-3))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("id = "+-3);
        assertThrown(()-> badCity.setId(3))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("change");
    }

    @Test
    public void testSetName() throws Exception
    {
        City first = new City(test.mineCity, "First City", test.joserobjr, new BlockPos(test.overWorld, -598, 44, -998));
        //noinspection SpellCheckingInspection
        assertEquals("firstcity", first.getIdentityName());
        assertThrown(()-> new City(test.mineCity, "first_ciTy!", test.joserobjr, new BlockPos(test.overWorld, 98988,55,9874)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("taken");

        City second = new City(test.mineCity, "City 2", test.joserobjr, new BlockPos(test.overWorld, 788,68,9885));
        assertEquals("city2", second.getIdentityName());
        first.setName("City1");
        assertEquals("city1", first.getIdentityName());
        assertEquals("City1", first.getName());

        assertThrown(() -> first.setName("CITY2"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("taken");
        first.setName("CITY 1");
        assertEquals("city1", first.getIdentityName());
        assertEquals("CITY 1", first.getName());

        assertThrown(()-> first.setName("c1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Bad");

        assertThrown(()-> new City(test.mineCity, "c1", test.joserobjr, new BlockPos(test.overWorld, 5846487,4,448)));
    }

    @Test
    public void testSetSpawn() throws Exception
    {
        BlockPos spawn = new BlockPos(test.overWorld, 54648, 32, 5855);
        City spawnCity = new City(test.mineCity, "SpawnCity", test.joserobjr, spawn);

        assertEquals(spawn, spawnCity.getSpawn());
        assertThrown(()-> spawnCity.setSpawn(spawn.getChunk().add(Direction.EAST).getMaxBlock()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not part of the city");

        BlockPos newSpawn = spawn.getChunk().getMaxBlock();
        spawnCity.setSpawn(newSpawn);
        assertEquals(newSpawn, spawnCity.getSpawn());
    }

    @Test
    public void testSetOwner() throws Exception
    {
        City owned = new City(test.mineCity, "Owned", test.joserobjr, new BlockPos(test.overWorld, 456484878, 32, 445454));

        assertEquals(test.joserobjr, owned.getOwner());
        PlayerID newOwner = new PlayerID(UUID.randomUUID(), "Randy");
        owned.setOwner(newOwner);
        assertEquals(newOwner, owned.getOwner());
    }

    @Test
    public void testClaim() throws DataSourceException
    {
        BlockPos spawn = new BlockPos(test.overWorld, 250, 32, -200);
        ChunkPos chunk = spawn.getChunk();
        City city = new City(test.mineCity, "City 2", test.joserobjr, spawn);

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

    @Test
    public void testClaimIsland() throws Exception
    {
        City farCity = new City(test.mineCity, "FarCity", test.joserobjr, new BlockPos(test.overWorld, 655,55,488));

        BlockPos far = new BlockPos(test.overWorld, -4847,44,688);
        assertThrown(()-> farCity.claim(far.getChunk(), false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not touching");

        farCity.claim(far.getChunk(), true);
        assertEquals(2, farCity.islands().size());
    }
}