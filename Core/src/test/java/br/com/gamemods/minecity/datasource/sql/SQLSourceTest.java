package br.com.gamemods.minecity.datasource.sql;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.MineCityConfig;
import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.EntityID;
import br.com.gamemods.minecity.api.permission.FlagHolder;
import br.com.gamemods.minecity.api.permission.Group;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.shape.Cuboid;
import br.com.gamemods.minecity.api.world.*;
import br.com.gamemods.minecity.datasource.api.DataSourceException;
import br.com.gamemods.minecity.datasource.test.TestData;
import br.com.gamemods.minecity.structure.*;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import static com.github.kolorobot.exceptions.java8.AssertJThrowableAssert.assertThrown;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings({"OptionalGetWithoutIsPresent", "unused"})
public class SQLSourceTest
{
    MineCityConfig config;
    MineCity mineCity;
    @SuppressWarnings("SpellCheckingInspection")
    WorldDim overworld;
    WorldDim nether;
    WorldDim custom;
    PlayerID joserobjr;
    Nature overNature;
    Nature netherNature;
    Nature customNature;

    @Before
    public void setUp() throws Exception
    {
        overworld = new WorldDim(0, ".");
        nether = new WorldDim(-1, "DIM-1", "Nether");
        custom = new WorldDim(0, "custom");
        joserobjr = new PlayerID(UUID.fromString("185e2176-0095-4ff8-a201-6f2aed9a032a"), "joserobjr");

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
                    "city_perm_defaults", "group_managers", "plot_perm_defaults", "plot_perm_player",
                    "plot_perm_entity", "plot_perm_group", "plots", "world_perm_defaults"
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

        overNature = mineCity.loadNature(overworld);
        netherNature = mineCity.loadNature(nether);
        customNature = mineCity.loadNature(custom);
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
    public void testSplitPlot() throws Exception
    {
        BlockPos spawnPos = new BlockPos(overworld, 44, 55, 32);
        City city = new City(mineCity, "Split", joserobjr, spawnPos);
        Island initialIsland = city.islands().iterator().next();
        ChunkPos initialChunk = spawnPos.getChunk();
        Map<Direction, Plot> plotMap = new HashMap<>(4);
        for(Direction dir: Direction.cardinal)
        {
            ChunkPos chunk = initialChunk.add(dir);
            Island island = city.claim(chunk, false);

            Cuboid cuboid = new Cuboid(chunk.getMinBlock().add(5, 32, 7), chunk.getMaxBlock().subtract(3, 200, 2));
            plotMap.put(dir, island.createPlot(dir.name(), null, (BlockPos) cuboid.min.add(2,2,2), cuboid));

            if(dir == Direction.NORTH)
                city.setSpawn((BlockPos) cuboid.min.add(-1, 1, 0));
        }

        Cuboid cuboid = new Cuboid(initialChunk.getMinBlock().add(5, 32, 7), initialChunk.getMaxBlock().subtract(3, 200, 2));
        Plot initialPlot = initialIsland.createPlot("spawn", null, (BlockPos) cuboid.min.add(2, 2, 2), cuboid);

        assertThrown(()-> city.disclaim(initialChunk, true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("because it contains plots");

        initialPlot.delete();
        Collection<Island> newIslands = city.disclaim(initialChunk, true);
        for(Island island: newIslands)
        {
            ChunkPos chunk = island.getArea().claims().findFirst().get();
            int rx = chunk.x - initialChunk.x;
            int rz = chunk.z - initialChunk.z;
            Direction dir = Direction.get(rx, 0, rz);

            Plot plot = plotMap.remove(dir);
            assertEquals(island, plot.getIsland());
            assertEquals(Optional.of(plot), island.getPlot(plot.getName()));
        }
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
        assertEquals(joserobjr, city.owner());
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
        /*
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
        assertEquals(random, city.owner());

        BlockPos pos = new BlockPos(overworld, spawn.x, spawn.y, spawn.z).getChunk().add(Direction.NORTH, 2).getMaxBlock();
        city.setSpawn(pos);

        reload();
        chunk = new ChunkPos(overworld, chunk.x, chunk.z);
        claim = mineCity.loadChunk(chunk);
        city = claim.getCity().get();
        assertNotNull(city);
        assertEquals(random, city.owner());
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
        assertEquals(null, claim.getIsland().orElse(null));
        assertEquals(islandA, claim.getIslandAcceptingReserve().orElse(null));

        claim = mineCity.loadChunk(spawnA.add(Direction.SOUTH));
        assertTrue(claim.reserve);
        assertEquals(null, claim.getIsland().orElse(null));
        assertEquals(islandA, claim.getIslandAcceptingReserve().orElse(null));

        claim = mineCity.loadChunk(spawnA.add(Direction.EAST));
        assertFalse(claim.reserve);
        assertEquals(Optional.empty(), claim.getIsland());

        cityA.claim(spawnA.add(Direction.EAST), false);
        claim = mineCity.loadChunk(spawnA.add(Direction.EAST, 2));
        assertTrue(claim.reserve);
        assertEquals(null, claim.getIsland().orElse(null));
        assertEquals(islandA, claim.getIslandAcceptingReserve().orElse(null));

        claim = mineCity.loadChunk(spawnA.add(Direction.NORTH_EAST));
        assertTrue(claim.reserve);
        assertEquals(null, claim.getIsland().orElse(null));
        assertEquals(islandA, claim.getIslandAcceptingReserve().orElse(null));

        assertThrown(()-> cityA.claim(spawnA.add(Direction.NORTH, 3), false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("The chunk ChunkPos{world=WorldDim{dim=-1, dir='DIM-1'}, x=400, z=397} is not touching an island owned by city")
                ;

        cityA.claim(spawnA.add(Direction.NORTH, 2), false);
        cityA.claim(spawnA.add(Direction.NORTH, 3), false);

        ChunkPos spawnB = spawnA.add(Direction.NORTH, 5);
        claim = mineCity.loadChunk(spawnB);
        assertTrue(claim.reserve);
        assertEquals(null, claim.getIsland().orElse(null));
        assertEquals(islandA, claim.getIslandAcceptingReserve().orElse(null));
        spawnB = spawnB.add(Direction.WEST, 4);
        assertFalse(mineCity.loadChunk(spawnB.add(Direction.EAST,6)).reserve);
        assertFalse(mineCity.loadChunk(spawnB.add(Direction.EAST,2)).reserve);


        City cityB = new City(mineCity, "Second", joserobjr, spawnB.getMaxBlock());
        Island islandB = cityB.claim(spawnB.add(Direction.WEST), false);
        claim = mineCity.loadChunk(spawnB.add(Direction.EAST));
        assertTrue(claim.reserve);
        assertEquals(null, claim.getIsland().orElse(null));
        assertEquals(islandB, claim.getIslandAcceptingReserve().orElse(null));

        cityB.claim(spawnB.add(Direction.WEST, 2), false);
        cityB.claim(spawnB.add(Direction.WEST, 3), false);
        claim = mineCity.loadChunk(spawnB.add(Direction.EAST,2 ));
        assertTrue(claim.reserve);
        assertEquals(islandB, claim.getIslandAcceptingReserve().orElse(null));

        cityB.claim(spawnB.add(Direction.WEST, 4), false);
        cityB.claim(spawnB.add(Direction.WEST, 5), false);
        claim = mineCity.loadChunk(spawnB.add(Direction.EAST,3 ));
        assertTrue(claim.reserve);
        assertEquals(islandA, claim.getIslandAcceptingReserve().orElse(null));

        cityB.claim(spawnB.add(Direction.WEST, 6), false);
        cityB.claim(spawnB.add(Direction.WEST, 7), false);
        claim = mineCity.loadChunk(spawnB.add(Direction.EAST,4 ));
        assertTrue(claim.reserve);
        assertEquals(islandA, claim.getIslandAcceptingReserve().orElse(null));

        cityB.claim(spawnB.add(Direction.WEST, 8), false);
        cityB.claim(spawnB.add(Direction.WEST, 9), false);
        claim = mineCity.loadChunk(spawnB.add(Direction.EAST,5 ));
        assertTrue(claim.reserve);
        assertEquals(islandA, claim.getIslandAcceptingReserve().orElse(null));

        cityB.claim(spawnB.add(Direction.WEST, 10), false);
        cityB.claim(spawnB.add(Direction.WEST, 11), false);
        claim = mineCity.loadChunk(spawnB.add(Direction.EAST,6 ));
        assertFalse(claim.reserve);
        assertEquals(Optional.empty(), claim.getIslandAcceptingReserve());

        cityB.claim(spawnB.add(Direction.WEST, 12), false);
        cityB.claim(spawnB.add(Direction.WEST, 13), false);
        claim = mineCity.loadChunk(spawnB.add(Direction.EAST,7 ));
        assertFalse(claim.reserve);
        assertEquals(Optional.empty(), claim.getIslandAcceptingReserve());
    }

    @Test
    public void testPermissions() throws Exception
    {
        // Prepare
        mineCity.defaultCityFlags.deny(PermissionFlag.ENTER);

        MinecraftEntity playerJose = mockPlayer(joserobjr);

        PlayerID random = new PlayerID(UUID.randomUUID(), "Random");
        MinecraftEntity playerRandom = mockPlayer(random);

        BlockPos spawn = new BlockPos(overworld, 4546487, 44, 47879784);
        City city = new City(mineCity, "Perm", joserobjr, spawn);

        // Check defaults
        assertEquals(FlagHolder.DEFAULT_DENIAL_MESSAGE,
                city.getDefaultMessage());

        assertFalse(city.can(playerJose, PermissionFlag.ENTER).isPresent());
        assertEquals(FlagHolder.DEFAULT_DENIAL_MESSAGE,
                city.can(playerRandom, PermissionFlag.ENTER).get());

        city.setDefaultMessage(new Message("","Test"));
        assertEquals(new Message("","Test"),
                city.getDefaultMessage());

        // Check persistence
        reload();
        city = mineCity.dataSource.getCityByName("Perm").get();
        assertEquals("Test", city.getDefaultMessage().toString());

        city.setDefaultMessage(FlagHolder.DEFAULT_DENIAL_MESSAGE);
        assertEquals(FlagHolder.DEFAULT_DENIAL_MESSAGE, city.getDefaultMessage());
        reload();
        city = mineCity.dataSource.getCityByName("Perm").get();
        assertEquals(FlagHolder.DEFAULT_DENIAL_MESSAGE, city.getDefaultMessage());

        // Check group creation
        PlayerID playerA = new PlayerID(UUID.randomUUID(), "Grouped");
        PlayerID playerB = new PlayerID(UUID.randomUUID(), "Grouped2");

        Group groupA = city.createGroup("a");
        groupA.addMember(playerA);
        assertTrue(groupA.isMember(playerA));

        Group groupB = city.createGroup("b");
        groupB.addMember(playerB);
        assertTrue(groupB.isMember(playerB));

        assertFalse(groupB.isMember(playerA));
        assertFalse(groupA.isMember(playerB));

        // Check group persistence
        reload();
        city = mineCity.dataSource.getCityByName("Perm").get();
        playerA = mineCity.dataSource.getPlayer(playerA.getName()).get();
        playerB = mineCity.dataSource.getPlayer(playerB.getName()).get();
        groupA = city.getGroup("a");
        groupB = city.getGroup("b");

        assertNotNull(groupA);
        assertNotNull(groupB);
        assertTrue(groupA.isMember(playerA));
        assertTrue(groupB.isMember(playerB));

        assertFalse(groupB.isMember(playerA));
        assertFalse(groupA.isMember(playerB));

        assertFalse(groupB.isMember(random));
        assertFalse(groupA.isMember(random));

        // Check group permissions
        city.allow(PermissionFlag.ENTER, groupA.getIdentity());
        assertFalse(city.can(playerJose, PermissionFlag.ENTER).isPresent());
        assertTrue(city.can(playerRandom, PermissionFlag.ENTER).isPresent());
        assertTrue(city.can(groupB.getIdentity(), PermissionFlag.ENTER).isPresent());
        assertFalse(city.can(groupA.getIdentity(), PermissionFlag.ENTER).isPresent());

        reload();
        city = mineCity.dataSource.getCityByName("Perm").get();
        groupA = city.getGroup("a");
        groupB = city.getGroup("b");
        assertNotNull(groupA);
        assertNotNull(groupB);

        assertFalse(city.can(playerJose, PermissionFlag.ENTER).isPresent());
        assertTrue(city.can(playerRandom, PermissionFlag.ENTER).isPresent());
        assertTrue(city.can(groupB.getIdentity(), PermissionFlag.ENTER).isPresent());
        assertFalse(city.can(groupA.getIdentity(), PermissionFlag.ENTER).isPresent());

        city.deny(PermissionFlag.ENTER, groupA.getIdentity(), new Message("", "Group A is out!"));
        assertFalse(city.can(playerJose, PermissionFlag.ENTER).isPresent());
        assertEquals(FlagHolder.DEFAULT_DENIAL_MESSAGE, city.can(playerRandom, PermissionFlag.ENTER).get());
        assertEquals(FlagHolder.DEFAULT_DENIAL_MESSAGE, city.can(groupB.getIdentity(), PermissionFlag.ENTER).get());
        assertEquals(new Message("", "Group A is out!"), city.can(groupA.getIdentity(), PermissionFlag.ENTER).get());

        reload();
        city = mineCity.dataSource.getCityByName("Perm").get();
        groupA = city.getGroup("a");
        groupB = city.getGroup("b");
        assertNotNull(groupA);
        assertNotNull(groupB);

        assertFalse(city.can(playerJose, PermissionFlag.ENTER).isPresent());
        assertEquals(FlagHolder.DEFAULT_DENIAL_MESSAGE, city.can(playerRandom, PermissionFlag.ENTER).get());
        assertEquals(FlagHolder.DEFAULT_DENIAL_MESSAGE, city.can(groupB.getIdentity(), PermissionFlag.ENTER).get());
        assertEquals("Group A is out!", city.can(groupA.getIdentity(), PermissionFlag.ENTER).get().toString());

        city.reset(PermissionFlag.ENTER, groupA.getIdentity());
        assertEquals(FlagHolder.DEFAULT_DENIAL_MESSAGE, city.can(groupA.getIdentity(), PermissionFlag.ENTER).get());
        reload();
        city = mineCity.dataSource.getCityByName("Perm").get();
        groupA = city.getGroup("a");
        groupB = city.getGroup("b");
        assertNotNull(groupA);
        assertNotNull(groupB);
        assertEquals(FlagHolder.DEFAULT_DENIAL_MESSAGE, city.can(groupA.getIdentity(), PermissionFlag.ENTER).get());

        city.setDefaultMessage(new Message("", "New Message"));
        assertEquals("New Message", city.can(groupA.getIdentity(), PermissionFlag.ENTER).get().toString());
        assertEquals("New Message", city.can(groupB.getIdentity(), PermissionFlag.ENTER).get().toString());

        city.allow(PermissionFlag.ENTER, random);
        assertFalse(city.can(random, PermissionFlag.ENTER).isPresent());
        reload();
        city = mineCity.dataSource.getCityByName("Perm").get();
        groupA = city.getGroup("a");
        groupB = city.getGroup("b");
        assertNotNull(groupA);
        assertNotNull(groupB);
        assertFalse(city.can(random, PermissionFlag.ENTER).isPresent());
        assertEquals("New Message", city.can(groupA.getIdentity(), PermissionFlag.ENTER).get().toString());

        city.resetAll(random);
        assertEquals("New Message", city.can(random, PermissionFlag.ENTER).get().toString());
        reload();
        city = mineCity.dataSource.getCityByName("Perm").get();
        groupA = city.getGroup("a");
        groupB = city.getGroup("b");
        assertNotNull(groupA);
        assertNotNull(groupB);

        assertEquals("New Message", city.can(random, PermissionFlag.ENTER).get().toString());

        city.allow(PermissionFlag.ENTER);
        assertFalse(city.can(groupA.getIdentity(), PermissionFlag.ENTER).isPresent());
        assertFalse(city.can(groupB.getIdentity(), PermissionFlag.ENTER).isPresent());
        reload();
        city = mineCity.dataSource.getCityByName("Perm").get();
        groupA = city.getGroup("a");
        groupB = city.getGroup("b");
        assertNotNull(groupA);
        assertNotNull(groupB);
        assertFalse(city.can(groupA.getIdentity(), PermissionFlag.ENTER).isPresent());
        assertFalse(city.can(groupB.getIdentity(), PermissionFlag.ENTER).isPresent());
        assertFalse(city.can(random, PermissionFlag.ENTER).isPresent());
        assertFalse(city.can(random, PermissionFlag.ENTER).isPresent());

        EntityID entity = new EntityID(MinecraftEntity.Type.STORAGE, UUID.randomUUID(), "ArmorStand");
        city.deny(PermissionFlag.ENTER, entity, new Message("", "No Armor stands!"));
        assertEquals("No Armor stands!", city.can(entity, PermissionFlag.ENTER).get().toString());
        city.setDefaultMessage(FlagHolder.DEFAULT_DENIAL_MESSAGE);
        reload();
        entity = new EntityID(MinecraftEntity.Type.STORAGE, entity.uniqueId, "ArmorStand");
        city = mineCity.dataSource.getCityByName("Perm").get();
        groupA = city.getGroup("a");
        groupB = city.getGroup("b");
        assertNotNull(groupA);
        assertNotNull(groupB);
        assertEquals("No Armor stands!", city.can(entity, PermissionFlag.ENTER).get().toString());
        city.reset(PermissionFlag.ENTER, entity);

        assertFalse(city.can(entity, PermissionFlag.ENTER).isPresent());
        city.allowAll(PermissionFlag.ENTER);
        assertFalse(city.can(entity, PermissionFlag.ENTER).isPresent());
        city.denyAll(PermissionFlag.MODIFY);

        assertFalse(city.can(joserobjr, PermissionFlag.MODIFY).isPresent());
        assertEquals(FlagHolder.DEFAULT_DENIAL_MESSAGE, city.can(random, PermissionFlag.MODIFY).get());
        assertEquals(FlagHolder.DEFAULT_DENIAL_MESSAGE, city.can(entity, PermissionFlag.MODIFY).get());
        assertEquals(FlagHolder.DEFAULT_DENIAL_MESSAGE, city.can(groupA.getIdentity(), PermissionFlag.MODIFY).get());
        assertEquals(FlagHolder.DEFAULT_DENIAL_MESSAGE, city.can(groupB.getIdentity(), PermissionFlag.MODIFY).get());
        reload();
        city = mineCity.dataSource.getCityByName("Perm").get();
        groupA = city.getGroup("a");
        groupB = city.getGroup("b");
        assertNotNull(groupA);
        assertNotNull(groupB);

        assertFalse(city.can(joserobjr, PermissionFlag.MODIFY).isPresent());
        assertEquals(FlagHolder.DEFAULT_DENIAL_MESSAGE, city.can(random, PermissionFlag.MODIFY).get());
        assertEquals(FlagHolder.DEFAULT_DENIAL_MESSAGE, city.can(entity, PermissionFlag.MODIFY).get());
        assertEquals(FlagHolder.DEFAULT_DENIAL_MESSAGE, city.can(groupA.getIdentity(), PermissionFlag.MODIFY).get());
        assertEquals(FlagHolder.DEFAULT_DENIAL_MESSAGE, city.can(groupB.getIdentity(), PermissionFlag.MODIFY).get());
    }

    private MinecraftEntity mockPlayer(PlayerID player)
    {
        MinecraftEntity entity = mock(MinecraftEntity.class);
        when(entity.getIdentity()).thenReturn(player);
        when(entity.getName()).thenReturn(player.getName());
        when(entity.getUniqueId()).thenReturn(player.getUniqueId());
        when(entity.getType()).thenReturn(MinecraftEntity.Type.PLAYER);
        when(entity.isGroupLoaded()).thenReturn(true);
        when(entity.getGroupIds()).thenReturn(Collections.emptySet());
        return entity;
    }
}
