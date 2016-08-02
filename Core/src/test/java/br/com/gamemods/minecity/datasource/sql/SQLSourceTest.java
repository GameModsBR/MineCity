package br.com.gamemods.minecity.datasource.sql;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.MineCityConfig;
import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.datasource.api.DataSourceException;
import br.com.gamemods.minecity.datasource.test.TestData;
import br.com.gamemods.minecity.structure.City;
import br.com.gamemods.minecity.structure.ClaimedChunk;
import br.com.gamemods.minecity.structure.Island;
import br.com.gamemods.minecity.structure.Nature;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;

import static com.github.kolorobot.exceptions.java8.AssertJThrowableAssert.assertThrown;
import static org.junit.Assert.*;

public class SQLSourceTest
{
    MineCityConfig config;
    MineCity mineCity;
    @SuppressWarnings("SpellCheckingInspection")
    WorldDim overworld = new WorldDim(0, ".");
    WorldDim nether = new WorldDim(-1, "DIM-1", "Nether");
    WorldDim custom = new WorldDim(0, "custom");
    PlayerID joserobjr = new PlayerID(UUID.fromString("185e2176-0095-4ff8-a201-6f2aed9a032a"), "joserobjr");
    Nature overNature;
    Nature netherNature;
    Nature customNature;

    @Before
    public void setUp() throws Exception
    {
        config = new MineCityConfig();
        config.dbUrl = "jdbc:mysql://localhost/minecity_test";
        config.dbUser = "unit_test";
        config.dbPass = "unit_test".getBytes();

        try(Connection connection = DriverManager.getConnection(config.dbUrl, config.dbUser, new String(config.dbPass)))
        {
            Assume.assumeTrue(connection.isValid(2000));
        }
        catch(SQLException e)
        {
            Assume.assumeNoException(e);
        }

        SQLConnection connection = new SQLConnection(config.dbUrl, config.dbUser, config.dbPass.clone());
        try(Connection connect = connection.connect())
        {
            assertTrue(connect.isValid(2000));
            Connection connect2 = connection.connect();
            try
            {
                assertEquals(connect, connect2);
            }
            catch(Exception e)
            {
                connect2.close();
                throw e;
            }

            try(Connection transaction = connection.transaction())
            {
                assertNotEquals(connect, transaction);
                assertFalse(transaction.getAutoCommit());

                connection.disconnect();
                assertTrue(transaction.isValid(2000));
                assertFalse(connect.isValid(2000));

                try
                {
                    connect2 = connection.connect();
                    assertNotEquals(connect, connect2);
                    assertTrue(connect2.isValid(2000));
                }
                catch(Exception e)
                {
                    connect2.close();
                }
            }
        }

        try(Connection connect = connection.connect())
        {
            assertFalse(connect.isClosed());
            assertTrue(connect.isValid(2000));

            connection.close();
            assertTrue(connect.isClosed());
            assertFalse(connect.isValid(2000));

            assertThrown(connection::connect)
                    .isInstanceOf(SQLException.class).hasCauseInstanceOf(IllegalStateException.class);
            assertThrown(connection::transaction)
                    .isInstanceOf(SQLException.class).hasCauseInstanceOf(IllegalStateException.class);
        }

        try(Connection conn = DriverManager.getConnection(config.dbUrl, config.dbUser, new String(config.dbPass)))
        {
            conn.setAutoCommit(false);
            String[] tables = new String[]{"chunks","city","islands","players","setup","world","entities","groups",
                    "group_players","group_entities", "city_perm_player", "city_perm_entity", "city_perm_group",
                    "city_perm_defaults"
            };
            try(Statement stm =  conn.createStatement())
            {
                stm.execute("SET FOREIGN_KEY_CHECKS = 0;");
                for(String table : tables)
                    stm.execute("DROP TABLE IF EXISTS minecity_"+table);
                stm.execute("SET FOREIGN_KEY_CHECKS = 1;");

                conn.commit();
            }
            catch(SQLException e)
            {
                conn.rollback();
                throw e;
            }
        }

        mineCity = new MineCity(new TestData(), config);
        mineCity.lazyReloads = false;
        mineCity.dataSource.initDB();
    }

    private void reload() throws DataSourceException, IOException
    {
        mineCity.dataSource.close();
        config.dbPass = "unit_test".getBytes();

        mineCity = new MineCity(new TestData(), config);
        mineCity.lazyReloads = false;
        mineCity.dataSource.initDB();

        overworld = new WorldDim(0, ".");
        nether = new WorldDim(-1, "DIM-1", "Nether");
        custom = new WorldDim(0, "custom");
        joserobjr = new PlayerID(UUID.fromString("185e2176-0095-4ff8-a201-6f2aed9a032a"), "joserobjr");

        overNature = mineCity.loadNature(overworld);
        netherNature = mineCity.loadNature(nether);
        customNature = mineCity.loadNature(custom);

        assertNotNull(overNature);
        assertNotNull(netherNature);
        assertNotNull(customNature);
        assertNotEquals(overNature, customNature);
    }

    @Test
    public void testCity() throws Exception
    {
        reload();

        BlockPos spawn = new BlockPos(overworld, 540, 32, -14000);
        ClaimedChunk claim = mineCity.loadChunk(spawn.getChunk());
        assertEquals(overNature, claim.owner);

        new City(mineCity, "Test City", joserobjr, spawn);

        reload();
        spawn = new BlockPos(overworld, spawn.x, spawn.y, spawn.z);
        claim = mineCity.loadChunk(spawn.getChunk());
        assertNotEquals(overNature, claim.owner);
        City city = claim.getCity().get();
        assertNotNull(city);
        //noinspection SpellCheckingInspection
        assertEquals("testcity", city.getIdentityName());
        assertEquals("Test City", city.getName());
        assertEquals(spawn, city.getSpawn());
        assertEquals(joserobjr, city.getOwner());
        assertEquals(1, city.getChunkCount());
        assertEquals(1, city.getSizeX());
        assertEquals(1, city.getSizeZ());

        reload();
        spawn = new BlockPos(overworld, spawn.x, spawn.y, spawn.z);
        claim = mineCity.loadChunk(spawn.getChunk());
        assertNotNull(claim.getCity());
        assertEquals(city.getId(), claim.getCity().get().getId());

        city = claim.getCity().get();
        ChunkPos chunk = spawn.getChunk();
        city.claim(chunk.add(Direction.NORTH), true);
        city.claim(chunk.add(Direction.NORTH, 2), true);
        city.claim(chunk.add(Direction.NORTH_EAST), true);
        city.claim(chunk.add(Direction.NORTH, 2).add(Direction.EAST), true);
        city.claim(chunk.add(Direction.NORTH_WEST), true);
        city.claim(chunk.add(Direction.WEST, 2).add(Direction.NORTH), true);
        /**
         *   XX
         * XXDX
         *   S
         */
        reload();
        spawn = new BlockPos(overworld, spawn.x, spawn.y, spawn.z);
        chunk = spawn.getChunk();
        claim = mineCity.loadChunk(chunk);
        city = claim.getCity().get();
        assertNotNull(city);
        assertEquals(7, city.getChunkCount());
        assertEquals(4, city.getSizeX());
        assertEquals(3, city.getSizeZ());
        assertEquals(1, city.islands().size());

        Island main = city.islands().iterator().next();
        Collection<Island> islands = city.disclaim(chunk.add(Direction.NORTH), true);
        assertEquals(2, islands.size());
        assertEquals(6, city.getChunkCount());
        assertEquals(5, city.getSizeX());
        assertEquals(4, city.getSizeZ());
        assertEquals(3, city.islands().size());
        Comparator<Island> chunkSize = (a, b) -> a.getChunkCount() - b.getChunkCount();
        Island min = islands.stream().min(chunkSize).get();
        Island med = islands.stream().max(chunkSize).get();
        assertEquals(1, min.getChunkCount());
        assertEquals(1, min.getSizeX());
        assertEquals(1, min.getSizeZ());

        assertEquals(2, med.getChunkCount());
        assertEquals(2, med.getSizeX());
        assertEquals(1, med.getSizeZ());

        assertEquals(3, main.getChunkCount());
        assertEquals(2, main.getSizeX());
        assertEquals(2, main.getSizeZ());

        reload();
        spawn = new BlockPos(overworld, spawn.x, spawn.y, spawn.z);
        chunk = spawn.getChunk();
        claim = mineCity.loadChunk(chunk);
        city = claim.getCity().get();
        assertNotNull(city);
        assertEquals(6, city.getChunkCount());
        assertEquals(5, city.getSizeX());
        assertEquals(4, city.getSizeZ());
        assertEquals(3, city.islands().size());

        islands = city.islands();
        main = islands.stream().max(chunkSize).get();
        assertEquals(3, main.getChunkCount());
        assertEquals(2, main.getSizeX());
        assertEquals(2, main.getSizeZ());

        min = islands.stream().min(chunkSize).get();
        assertEquals(1, min.getChunkCount());
        assertEquals(1, min.getSizeX());
        assertEquals(1, min.getSizeZ());

        med = islands.stream().filter(i->i.getChunkCount()==2).findAny().orElse(null);
        assertNotNull(med);
        assertEquals(2, med.getChunkCount());
        assertEquals(2, med.getSizeX());
        assertEquals(1, med.getSizeZ());

        BlockPos cross = new BlockPos(custom, spawn.x, spawn.y, spawn.z);
        Island i2 = city.claim(cross.getChunk(), true);
        assertEquals(7, city.getChunkCount());
        assertEquals(6, city.getSizeX());
        assertEquals(5, city.getSizeZ());
        assertEquals(4, city.islands().size());

        reload();
        spawn = new BlockPos(overworld, spawn.x, spawn.y, spawn.z);
        chunk = spawn.getChunk();
        claim = mineCity.loadChunk(chunk);
        city = claim.getCity().get();
        assertNotNull(city);
        main = city.getIsland(main.getId());
        min = city.getIsland(min.getId());
        med = city.getIsland(med.getId());
        i2 = city.getIsland(i2.getId());

        assertEquals(7, city.getChunkCount());
        assertEquals(6, city.getSizeX());
        assertEquals(5, city.getSizeZ());
        assertEquals(4, city.islands().size());

        assertNotNull(main);
        assertEquals(3, main.getChunkCount());
        assertEquals(2, main.getSizeX());
        assertEquals(2, main.getSizeZ());

        assertNotNull(min);
        assertEquals(1, min.getChunkCount());
        assertEquals(1, min.getSizeX());
        assertEquals(1, min.getSizeZ());

        assertNotNull(med);
        assertEquals(2, med.getChunkCount());
        assertEquals(2, med.getSizeX());
        assertEquals(1, med.getSizeZ());

        assertNotNull(i2);
        assertEquals(1, i2.getChunkCount());
        assertEquals(1, i2.getSizeX());
        assertEquals(1, i2.getSizeZ());

        cross = new BlockPos(custom, cross.x, cross.y, cross.z);
        city.disclaim(cross.getChunk(), false);
        assertEquals(0, i2.getChunkCount());
        assertEquals(0, i2.getSizeX());
        assertEquals(0, i2.getSizeZ());
        assertEquals(3, city.islands().size());

        reload();
        chunk = new ChunkPos(overworld, chunk.x, chunk.z);
        claim = mineCity.loadChunk(chunk);
        city = claim.getCity().get();
        assertNotNull(city);

        PlayerID random = new PlayerID(UUID.randomUUID(), "Randy");
        city.setOwner(random);

        reload();
        random = new PlayerID(random.uniqueId, "Randy");
        chunk = new ChunkPos(overworld, chunk.x, chunk.z);
        claim = mineCity.loadChunk(chunk);
        city = claim.getCity().get();
        assertNotNull(city);
        assertEquals(random, city.getOwner());

        BlockPos pos = new BlockPos(overworld, spawn.x, spawn.y, spawn.z).getChunk().add(Direction.NORTH, 2).getMaxBlock();
        city.setSpawn(pos);

        reload();
        chunk = new ChunkPos(overworld, chunk.x, chunk.z);
        claim = mineCity.loadChunk(chunk);
        city = claim.getCity().get();
        assertNotNull(city);
        assertEquals(random, city.getOwner());
        assertEquals(pos, city.getSpawn());
    }

    @Test
    public void testReserve() throws Exception
    {
        ChunkPos spawnA = new ChunkPos(nether, 400,400);
        long start = System.currentTimeMillis();
        City cityA = new City(mineCity, "First", joserobjr, spawnA.getMaxBlock());
        long end = System.currentTimeMillis();
        System.out.println("DEBUG: CityA creation: "+(end-start)+"ms");

        start = System.currentTimeMillis();
        Island islandA = cityA.claim(spawnA.add(Direction.NORTH), false);
        end = System.currentTimeMillis();
        System.out.println("DEBUG: CityA claim 1: "+(end-start)+"ms");

        ClaimedChunk claim = mineCity.loadChunk(spawnA.add(Direction.NORTH, 2));
        assertTrue(claim.reserve);
        assertEquals(islandA, claim.getIsland().orElse(null));

        claim = mineCity.loadChunk(spawnA.add(Direction.SOUTH));
        assertTrue(claim.reserve);
        assertEquals(islandA, claim.getIsland().orElse(null));

        claim = mineCity.loadChunk(spawnA.add(Direction.EAST));
        assertFalse(claim.reserve);
        assertEquals(Optional.empty(), claim.getIsland());

        cityA.claim(spawnA.add(Direction.EAST), false);
        claim = mineCity.loadChunk(spawnA.add(Direction.EAST, 2));
        assertTrue(claim.reserve);
        assertEquals(islandA, claim.getIsland().orElse(null));

        claim = mineCity.loadChunk(spawnA.add(Direction.NORTH_EAST));
        assertTrue(claim.reserve);
        assertEquals(islandA, claim.getIsland().orElse(null));

        assertThrown(()-> cityA.claim(spawnA.add(Direction.NORTH, 3), false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("The chunk ChunkPos{world=WorldDim{dim=-1, dir='DIM-1'}, x=400, z=403} is not touching an island owned by city")
                ;

        cityA.claim(spawnA.add(Direction.NORTH, 2), false);
        cityA.claim(spawnA.add(Direction.NORTH, 3), false);

        ChunkPos spawnB = spawnA.add(Direction.NORTH, 5);
        claim = mineCity.loadChunk(spawnB);
        assertTrue(claim.reserve);
        assertEquals(islandA, claim.getIsland().orElse(null));
        spawnB = spawnB.add(Direction.WEST, 4);
        assertFalse(mineCity.loadChunk(spawnB.add(Direction.EAST,6)).reserve);
        assertFalse(mineCity.loadChunk(spawnB.add(Direction.EAST,2)).reserve);


        City cityB = new City(mineCity, "Second", joserobjr, spawnB.getMaxBlock());
        Island islandB = cityB.claim(spawnB.add(Direction.WEST), false);
        claim = mineCity.loadChunk(spawnB.add(Direction.EAST));
        assertTrue(claim.reserve);
        assertEquals(islandB, claim.getIsland().orElse(null));

        cityB.claim(spawnB.add(Direction.WEST, 2), false);
        cityB.claim(spawnB.add(Direction.WEST, 3), false);
        claim = mineCity.loadChunk(spawnB.add(Direction.EAST,2 ));
        assertTrue(claim.reserve);
        assertEquals(islandB, claim.getIsland().orElse(null));

        cityB.claim(spawnB.add(Direction.WEST, 4), false);
        cityB.claim(spawnB.add(Direction.WEST, 5), false);
        claim = mineCity.loadChunk(spawnB.add(Direction.EAST,3 ));
        assertTrue(claim.reserve);
        assertEquals(islandA, claim.getIsland().orElse(null));

        cityB.claim(spawnB.add(Direction.WEST, 6), false);
        cityB.claim(spawnB.add(Direction.WEST, 7), false);
        claim = mineCity.loadChunk(spawnB.add(Direction.EAST,4 ));
        assertTrue(claim.reserve);
        assertEquals(islandA, claim.getIsland().orElse(null));

        cityB.claim(spawnB.add(Direction.WEST, 8), false);
        cityB.claim(spawnB.add(Direction.WEST, 9), false);
        claim = mineCity.loadChunk(spawnB.add(Direction.EAST,5 ));
        assertTrue(claim.reserve);
        assertEquals(islandA, claim.getIsland().orElse(null));

        cityB.claim(spawnB.add(Direction.WEST, 10), false);
        cityB.claim(spawnB.add(Direction.WEST, 11), false);
        claim = mineCity.loadChunk(spawnB.add(Direction.EAST,6 ));
        assertFalse(claim.reserve);
        assertEquals(Optional.empty(), claim.getIsland());

        cityB.claim(spawnB.add(Direction.WEST, 12), false);
        cityB.claim(spawnB.add(Direction.WEST, 13), false);
        claim = mineCity.loadChunk(spawnB.add(Direction.EAST,7 ));
        assertFalse(claim.reserve);
        assertEquals(Optional.empty(), claim.getIsland());
    }
}