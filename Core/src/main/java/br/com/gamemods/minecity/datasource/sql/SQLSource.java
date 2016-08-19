package br.com.gamemods.minecity.datasource.sql;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.MineCityConfig;
import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.Slow;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.*;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.datasource.api.CityCreationResult;
import br.com.gamemods.minecity.datasource.api.DataSourceException;
import br.com.gamemods.minecity.datasource.api.IDataSource;
import br.com.gamemods.minecity.datasource.api.unchecked.DBSupplier;
import br.com.gamemods.minecity.datasource.api.unchecked.UncheckedDataSourceException;
import br.com.gamemods.minecity.structure.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static br.com.gamemods.minecity.api.StringUtil.identity;

public class SQLSource implements IDataSource
{
    @NotNull
    public final MineCity mineCity;
    @NotNull
    private final SQLConnection connection;
    @NotNull
    private final SQLCityStorage cityStorage;
    @NotNull
    private final SQLPermStorage permStorage;
    @NotNull
    final Map<Integer, City> cityMap = new HashMap<>();
    final Map<Integer, WorldDim> worldDimMap = new ConcurrentHashMap<>(3);
    final Set<String> cityNames = new HashSet<>();
    final Map<String, Set<String>> groupNames = new HashMap<>();

    public SQLSource(@NotNull MineCity mineCity, @NotNull MineCityConfig config)
    {
        this.mineCity = mineCity;
        connection = new SQLConnection(config.dbUrl, config.dbUser, config.dbPass != null? config.dbPass.clone() : null);
        if(config.dbPass != null)
            Arrays.fill(config.dbPass, (byte) 0);

        permStorage = new SQLPermStorage(this, connection);
        cityStorage = new SQLCityStorage(this, connection, permStorage);
    }

    @Slow
    private Collection<Island> loadIslands(Connection connection, int cityId) throws SQLException, DataSourceException
    {
        try(PreparedStatement pst = connection.prepareStatement(
                "SELECT c.island_id, MIN(x), MAX(x), MIN(z), MAX(z), COUNT(*), i.world_id, w.dim, w.world, w.`name` " +
                "FROM minecity_chunks c " +
                "INNER JOIN minecity_islands AS i ON c.island_id=i.island_id " +
                "INNER JOIN minecity_world AS w ON i.world_id=w.world_id " +
                "WHERE i.city_id = ? AND c.reserve=0 " +
                "GROUP BY c.island_id"
        ))
        {
            pst.setInt(1, cityId);
            ArrayList<Island> islands = new ArrayList<>(3);
            ResultSet result = pst.executeQuery();
            while(result.next())
            {
                WorldDim world = world(result.getInt(7), ()->result.getInt(8), ()->result.getString(9), ()->result.getString(10));
                islands.add(new SQLIsland(cityStorage, permStorage,
                        result.getInt(1), result.getInt(2), result.getInt(3),
                        result.getInt(4), result.getInt(5), result.getInt(6), world
                ));
            }
            islands.trimToSize();
            return islands;
        }
    }

    protected WorldDim world(int id, DBSupplier<Integer> dim, DBSupplier<String> dir, DBSupplier<String> name)
    {
        WorldDim world = worldDimMap.get(id);
        if(world != null)
            return world;

        int dimI = dim.get();
        String dirS = dir.get();
        world = mineCity.worldProvider.map(p-> p.getWorld(dimI, dirS)).orElseGet(()-> new WorldDim(dimI, dirS));

        world.setDataSourceId(id);
        worldDimMap.put(id, world);

        if(name != null)
            world.name = name.get();

        return world;
    }

    @Slow
    private Optional<City> loadCity(Connection connection, int id, @Nullable String identity) throws SQLException, DataSourceException
    {
        synchronized(cityMap)
        {
            try(PreparedStatement pst = connection.prepareStatement(
                    "SELECT `c`.`name`, `owner`, `o`.`player_uuid`, `o`.`player_name`, `spawn_world`, `spawn_x`, `spawn_y`, `spawn_z`, " +
                        "`w`.`dim`, `w`.`world`, `w`.`name`, `display_name`, c.`perm_denial_message`, `city_id` " +
                    "FROM `minecity_city` AS `c` " +
                        "LEFT JOIN `minecity_players` AS `o` ON `owner` = `o`.`player_id` "+
                        "LEFT JOIN `minecity_world` AS `w` ON `spawn_world` = `w`.`world_id` "+
                    " WHERE "+(identity == null?"`city_id`=?":"`c`.`name`=?")
            ))
            {
                if(identity == null)
                    pst.setInt(1, id);
                else
                    pst.setString(1, identity);

                ResultSet result = pst.executeQuery();
                if(!result.next())
                    return Optional.empty();

                PlayerID owner;
                int ownerId = result.getInt(2);
                if(ownerId <= 0) owner = null;
                else
                    owner = new PlayerID(ownerId, uuid(result.getBytes(3)), result.getString(4));

                WorldDim world = world(result.getInt(5), ()->result.getInt(9), ()->result.getString(10), ()->result.getString(11));
                BlockPos spawn = new BlockPos(world, result.getInt(6), result.getInt(7), result.getInt(8));

                String name = result.getString(1);
                String displayName = result.getString(12);

                String str = result.getString("perm_denial_message");
                Message message = str == null? null : new Message("", "${msg}", new Object[]{"msg",str});
                id = result.getInt("city_id");
                pst.close();

                Collection<Island> islands = loadIslands(connection, id);
                City city = new City(mineCity, name, displayName, owner, spawn, islands, id, cityStorage, permStorage, message);
                islands.forEach(i-> ((SQLIsland)i).city = city);
                cityMap.put(id, city);
                return Optional.of(city);
            }
        }
    }

    void executeUpdate(PreparedStatement pst, int expected) throws DataSourceException, SQLException
    {
        int changes = pst.executeUpdate();
        if(changes != expected)
            throw new DataSourceException("Expected "+expected+" but got "+changes);
    }

    private int insertWorld(Connection connection, WorldDim world) throws SQLException
    {
        try(PreparedStatement pst = connection.prepareStatement(
                "INSERT INTO `minecity_world`(`dim`,`world`,`name`) VALUES(?,?,?)",
                Statement.RETURN_GENERATED_KEYS
        ))
        {
            pst.setInt(1, world.dim);
            pst.setString(2, world.dir);
            setNullableString(pst, 3, world.name);
            pst.executeUpdate();
            ResultSet keys = pst.getGeneratedKeys();
            keys.next();
            return keys.getInt(1);
        }
    }

    @Slow
    int worldId(Connection connection, WorldDim world) throws DataSourceException
    {
        int dataSourceId = world.getDataSourceId();
        if(dataSourceId > 0) return dataSourceId;

        int id = 0;
        try
        {
            try(PreparedStatement pst = connection.prepareStatement(
                    "SELECT `world_id` FROM `minecity_world` WHERE `dim`=? AND `world`=?"
            ))
            {
                pst.setInt(1, world.dim);
                pst.setString(2, world.dir);
                ResultSet result = pst.executeQuery();
                if(result.next())
                    world.setDataSourceId(id = result.getInt(1));
            }

            if(id <= 0)
                insertWorld(connection, world);

            world.setDataSourceId(id);
            worldDimMap.putIfAbsent(id, world);
            return id;
        }
        catch(SQLException e)
        {
            throw new DataSourceException(e);
        }
    }

    @Contract("null->null;!null->!null")
    byte[] uuid(UUID uuid)
    {
        if(uuid == null)
            return null;

        byte[] bytes = new byte[16];
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        return bytes;
    }

    @Contract("null->null;!null->!null")
    UUID uuid(byte[] bytes) throws DataSourceException
    {
        if(bytes == null)
            return null;

        try
        {
            ByteBuffer bb = ByteBuffer.wrap(bytes);
            long firstLong = bb.getLong();
            long secondLong = bb.getLong();
            return new UUID(firstLong, secondLong);
        }
        catch(Exception e)
        {
            throw new DataSourceException("Bad UUID", e);
        }
    }

    void setNullableInt(PreparedStatement pst, int field, int val) throws SQLException
    {
        if(val == 0)
            pst.setNull(field, Types.INTEGER);
        else
            pst.setInt(field, val);
    }

    void setNullableString(PreparedStatement pst, int field, String val) throws SQLException
    {
        if(val == null)
            pst.setNull(field, Types.VARCHAR);
        else
            pst.setString(field, val);
    }

    @Slow
    int playerId(Connection connection, @Nullable OptionalPlayer player) throws DataSourceException
    {
        PlayerID playerId;
        if(player == null || (playerId = player.player()) == null) return 0;
        int id = player.getDataSourceId();
        if(id > 0) return id;

        try
        {
            try(PreparedStatement pst = connection.prepareStatement(
                    "SELECT `player_id` FROM `minecity_players` WHERE `player_uuid`=?"
            ))
            {
                pst.setBytes(1, uuid(playerId.uniqueId));
                ResultSet result = pst.executeQuery();
                if(result.next())
                {
                    id = result.getInt(1);
                    playerId.setDataSourceId(id);
                    return id;
                }
            }

            try(PreparedStatement pst = connection.prepareStatement(
                    "INSERT INTO `minecity_players`(`player_uuid`, `player_name`) VALUES(?,?)"
                , Statement.RETURN_GENERATED_KEYS
            ))
            {
                pst.setBytes(1, uuid(playerId.uniqueId));
                pst.setString(2, playerId.getName());
                pst.executeUpdate();
                ResultSet keys = pst.getGeneratedKeys();
                keys.next();
                id = keys.getInt(1);
                playerId.setDataSourceId(id);
                return id;
            }
        }
        catch(SQLException e)
        {
            throw new DataSourceException(e);
        }
    }

    @Slow
    int entityId(Connection connection, @Nullable EntityID entity) throws DataSourceException
    {
        if(entity == null) return 0;
        int id = entity.getDataSourceId();
        if(id > 0) return id;

        try
        {
            try(PreparedStatement pst = connection.prepareStatement(
                    "SELECT `entity_id` FROM `minecity_entities` WHERE `entity_uuid`=?"
            ))
            {
                pst.setBytes(1, uuid(entity.uniqueId));
                ResultSet result = pst.executeQuery();
                if(result.next())
                {
                    id = result.getInt(1);
                    entity.setDataSourceId(id);
                    return id;
                }
            }

            try(PreparedStatement pst = connection.prepareStatement(
                    "INSERT INTO `minecity_entities`(`entity_uuid`, `entity_name`, entity_type) VALUES(?,?,?)"
                    , Statement.RETURN_GENERATED_KEYS
            ))
            {
                pst.setBytes(1, uuid(entity.uniqueId));
                pst.setString(2, entity.getName());
                pst.setString(3, entity.getEntityType().name());
                pst.executeUpdate();
                ResultSet keys = pst.getGeneratedKeys();
                keys.next();
                id = keys.getInt(1);
                entity.setDataSourceId(id);
                return id;
            }
        }
        catch(SQLException e)
        {
            throw new DataSourceException(e);
        }
    }

    @Slow
    private City city(Connection connection, int cityId) throws SQLException, DataSourceException
    {
        synchronized(cityMap)
        {
            City city = cityMap.get(cityId);
            if(city != null)
                return city;

            return loadCity(connection, cityId, null).orElseThrow(()-> new DataSourceException("City ID "+cityId+" not found"));
        }
    }

    @Slow
    @Nullable
    @Override
    public ClaimedChunk getCityChunk(@NotNull ChunkPos pos) throws DataSourceException
    {
        try
        {
            Connection connection = this.connection.connect();
            try(PreparedStatement pst = connection.prepareStatement(
                    "SELECT `i`.`city_id`, `i`.`island_id`, reserve FROM `minecity_chunks` AS `c` " +
                        "INNER JOIN `minecity_world` AS `w` ON `c`.`world_id`=`w`.`world_id` " +
                        "INNER JOIN `minecity_islands` AS `i` ON `c`.`island_id`=`i`.`island_id` "+
                    "WHERE `w`.`dim`=? AND `w`.`world`=? AND `c`.`x`=? AND `c`.`z`=?;"
            ))
            {
                pst.setInt(1, pos.world.dim);
                pst.setString(2, pos.world.dir);
                pst.setInt(3, pos.x);
                pst.setInt(4, pos.z);
                ResultSet result = pst.executeQuery();
                if(!result.next())
                    return null;
                int cityId = result.getInt(1);
                int islandId = result.getInt(2);
                boolean reserve = result.getBoolean(3);
                pst.close();

                City city = city(connection, cityId);
                Island island = city.getIsland(islandId);
                return new ClaimedChunk(island != null? island : Inconsistency.INSTANCE, pos, reserve);
            }
        }
        catch(SQLException e)
        {
            throw new DataSourceException(e);
        }
        catch(UncheckedDataSourceException e)
        {
            throw e.getCause();
        }
    }

    @Slow
    int createIsland(Connection transaction, int cityId, WorldDim world) throws DataSourceException, SQLException
    {
        int worldId = worldId(transaction, world);
        int islandId;
        try(PreparedStatement pst = transaction.prepareStatement(
                "INSERT INTO `minecity_islands`(world_id, city_id) VALUES(?,?)",
                Statement.RETURN_GENERATED_KEYS
        ))
        {
            pst.setInt(1, worldId);
            pst.setInt(2, cityId);
            pst.executeUpdate();
            ResultSet keys = pst.getGeneratedKeys();
            keys.next();
            islandId = keys.getInt(1);
        }

        return islandId;
    }

    @Slow
    void createClaim(Connection connection, int islandId, ChunkPos chunk) throws SQLException, DataSourceException
    {
        int worldId = worldId(connection, chunk.world);
        try(PreparedStatement pst = connection.prepareStatement(
                "DELETE FROM minecity_chunks WHERE world_id=? AND x=? AND z=? AND reserve=1"
        ))
        {
            pst.setInt(1, worldId);
            pst.setInt(2, chunk.x);
            pst.setInt(3, chunk.z);
            pst.executeUpdate();
        }
        try(PreparedStatement pst = connection.prepareStatement(
                "INSERT INTO `minecity_chunks`(world_id, x, z, island_id, reserve) VALUES(?,?,?,?,0)"
        ))
        {
            pst.setInt(1, worldId);
            pst.setInt(2, chunk.x);
            pst.setInt(3, chunk.z);
            pst.setInt(4, islandId);
            if(pst.executeUpdate() <= 0)
                throw new DataSourceException("Failed to claim the spawn chunk");
        }
    }

    @Nullable
    @Override
    public String checkNameConflict(@NotNull String name)
    {
        name = identity(name);
        for(String cityName : cityNames)
            if(identity(cityName).equals(name))
                return cityName;

        return null;
    }

    @Slow
    @NotNull
    @Override
    public CityCreationResult createCity(@NotNull City city) throws DataSourceException, IllegalStateException
    {
        if(city.getId() > 0)
            throw new IllegalStateException();

        try
        {
            BlockPos spawn = city.getSpawn();
            ChunkPos spawnChunk = spawn.getChunk();
            ClaimedChunk claim = getCityChunk(spawnChunk);
            if(claim != null)
                throw new IllegalStateException("The chunk " + spawnChunk + " is already claimed: " + claim);

            int islandId;
            try(Connection connection = this.connection.transaction())
            {
                try
                {
                    int worldId = worldId(connection, spawn.world);
                    int cityId;
                    try(PreparedStatement pst = connection.prepareStatement(
                        "INSERT INTO `minecity_city`(name, owner, spawn_world, spawn_x, spawn_y, spawn_z, display_name) " +
                                            "VALUES (  ? ,   ?  ,      ?     ,    ?   ,    ?    ,   ?   ,     ?       )",
                            Statement.RETURN_GENERATED_KEYS
                    ))
                    {
                        pst.setString(1, city.getIdentityName());
                        setNullableInt(pst, 2, playerId(connection, city.owner()));
                        pst.setInt(3, worldId);
                        pst.setInt(4, spawn.x);
                        //noinspection SuspiciousNameCombination
                        pst.setInt(5, spawn.y);
                        pst.setInt(6, spawn.z);
                        pst.setString(7, city.getName());
                        pst.executeUpdate();
                        ResultSet keys = pst.getGeneratedKeys();
                        keys.next();
                        cityId = keys.getInt(1);
                        city.setId(cityId);
                        cityMap.put(cityId, city);
                    }

                    islandId = createIsland(connection, cityId, spawnChunk.world);
                    createClaim(connection, islandId, spawnChunk);
                    connection.commit();
                    cityNames.add(city.getName());
                }
                catch(Exception e)
                {
                    connection.rollback();
                    throw e;
                }
            }

            return new CityCreationResult(cityStorage, permStorage,
                    new SQLIsland(cityStorage, permStorage, islandId, spawnChunk, city),
                    Collections.emptyList()
            );
        }
        catch(SQLException e)
        {
            throw new DataSourceException(e);
        }
    }

    @Slow
    @Override
    public void initDB() throws DataSourceException, IOException
    {
        try(Connection transaction = connection.transaction())
        {
            try(Statement stm = transaction.createStatement())
            {
                ResultSet result;
                int version;
                try
                {
                    result = stm.executeQuery("SELECT `value` FROM `minecity_setup` WHERE `property`='version';");
                    result.next();
                    version = result.getInt(1);
                    result.close();
                }
                catch(SQLException e)
                {
                    ScriptRunner runner = new ScriptRunner(transaction, false, true);
                    runner.setLogWriter(null);
                    runner.runScript(new InputStreamReader(
                            getClass().getResourceAsStream("/assets/minecity/db/setup.sql"), "UTF-8"
                    ));
                    transaction.commit();
                    return;
                }

                if(version != 1)
                    throw new DataSourceException("Unsupported database version: "+version);

                result = stm.executeQuery("SELECT `display_name` FROM `minecity_city ");
                while(result.next())
                    cityNames.add(result.getString(1));
                result.close();

                result = stm.executeQuery("SELECT c.name, g.name FROM minecity_groups g INNER JOIN minecity_city c ON c.city_id = g.city_id");
                while(result.next())
                    groupNames.computeIfAbsent(result.getString(1), n-> new HashSet<>(1)).add(result.getString(2));
            }
            catch(Exception e)
            {
                transaction.rollback();
                throw e;
            }
        }
        catch(SQLException e)
        {
            throw new DataSourceException(e);
        }
    }

    @Slow
    @NotNull
    @Override
    public Optional<City> getCityByName(@NotNull String name) throws DataSourceException
    {
        name = identity(name);
        if(name.length() < 3)
            return Optional.empty();

        synchronized(cityMap)
        {
            for(City city : cityMap.values())
                if(city.getIdentityName().equals(name))
                    return Optional.of(city);

            try
            {
                return loadCity(connection.connect(), 0, name);
            }
            catch(SQLException e)
            {
                throw new DataSourceException(e);
            }
        }
    }

    @NotNull
    @Override
    public Set<GroupID> getEntityGroups(Identity<?> identity) throws DataSourceException
    {
        try
        {
            switch(identity.getType())
            {
                case PLAYER:
                    Connection connection = this.connection.connect();
                    try(PreparedStatement pst = connection.prepareStatement(
                            "SELECT g.group_id, g.city_id, g.display_name, c.display_name AS home " +
                            "FROM minecity_group_players gp " +
                                "INNER JOIN minecity_groups g ON g.group_id = gp.group_id " +
                                "INNER JOIN minecity_city c ON c.city_id = g.city_id " +
                            "WHERE gp.player_id=?"
                    ))
                    {
                        pst.setInt(1, playerId(connection, (PlayerID) identity));
                        ResultSet result = pst.executeQuery();
                        Set<GroupID> set = new HashSet<>(2);
                        while(result.next())
                        {
                            int groupId = result.getInt(1);
                            int cityId = result.getInt(2);
                            String name = result.getString(3);
                            String home = result.getString(4);
                            GroupID group = Optional.ofNullable(cityMap.get(cityId))
                                    .map(c-> c.getGroup(groupId)).map(Group::getIdentity)
                                    .orElseGet(()-> new GroupID(groupId, name, home, cityId))
                                    ;
                            set.add(group);
                        }

                        return set;
                    }

                case ENTITY:
                    connection = this.connection.connect();
                    int id = identity.getDataSourceId();
                    try(PreparedStatement pst = connection.prepareStatement(
                            "SELECT g.group_id, g.city_id, g.display_name, c.display_name AS home " +
                                    "FROM minecity_group_entities ge " +
                                    "INNER JOIN minecity_groups g ON g.group_id = ge.group_id " +
                                    "INNER JOIN minecity_city c ON c.city_id = g.city_id " +
                                    (id > 0 ? "WHERE ge.entity_id = ?" :
                                    "INNER JOIN minecity_entities e ON ge.entity_id = e.entity_id " +
                                            "WHERE e.entity_uuid = ?")
                    ))
                    {
                        if(id > 0)
                            pst.setInt(1, id);
                        else
                            pst.setBytes(1, uuid(((EntityID)identity).getUniqueId()));

                        ResultSet result = pst.executeQuery();
                        Set<GroupID> set = new HashSet<>(2);
                        while(result.next())
                        {
                            int groupId = result.getInt(1);
                            int cityId = result.getInt(2);
                            String name = result.getString(3);
                            String home = result.getString(4);
                            GroupID group = Optional.ofNullable(cityMap.get(cityId))
                                    .map(c-> c.getGroup(groupId)).map(Group::getIdentity)
                                    .orElseGet(()-> new GroupID(groupId, name, home, cityId))
                                    ;
                            set.add(group);
                        }

                        return set;
                    }

                default:
                    return Collections.emptySet();
            }
        }
        catch(SQLException e)
        {
            throw new DataSourceException(e);
        }
    }

    @Slow
    @NotNull
    @Override
    public Optional<PlayerID> getPlayer(@NotNull String name) throws DataSourceException
    {
        try(PreparedStatement pst = connection.connect().prepareStatement(
                "SELECT player_id, player_uuid, player_name FROM minecity_players WHERE player_name=?"
        ))
        {
            pst.setString(1, name);
            ResultSet result = pst.executeQuery();
            if(!result.next())
                return Optional.empty();
            return Optional.of(new PlayerID(result.getInt(1), uuid(result.getBytes(2)), result.getString(3)));
        }
        catch(SQLException e)
        {
            throw new DataSourceException(e);
        }
    }

    @Slow
    @NotNull
    @Override
    public IslandArea getArea(@NotNull Island island)
            throws DataSourceException, ClassCastException, IllegalArgumentException
    {
        SQLIsland sqlIsland = (SQLIsland) island;

        try
        {
            Connection connection = this.connection.connect();
            try(PreparedStatement pst = connection.prepareStatement(
                    "SELECT x, z FROM minecity_chunks WHERE island_id=? AND world_id=? AND reserve=0"
            ))
            {
                pst.setInt(1, sqlIsland.id);
                pst.setInt(2, worldId(connection, sqlIsland.world));
                ResultSet result = pst.executeQuery();
                List<ChunkPos> list = new ArrayList<>();
                while(result.next())
                    list.add(new ChunkPos(sqlIsland.world, result.getInt(1), result.getInt(2)));

                return new IslandArea(island, list);
            }
        }
        catch(SQLException e)
        {
            throw new DataSourceException(e);
        }
    }

    @Slow
    @NotNull
    @Override
    public Nature getNature(@NotNull WorldDim world) throws DataSourceException
    {
        try
        {
            int id = world.getDataSourceId();
            Connection connection = this.connection.connect();
            try(PreparedStatement pst = connection.prepareStatement(
                    "SELECT world_id, `name`, city_creations, perm_denial_message " +
                    "FROM minecity_world " +
                    "WHERE "+(id > 0? "world_id=?":"dim=? AND world=?")
            ))
            {
                if(id > 0)
                    pst.setInt(1, id);
                else
                {
                    pst.setInt(1, world.dim);
                    pst.setString(2, world.dir);
                }

                ResultSet result = pst.executeQuery();
                if(result.next())
                {
                    if(id == 0)
                        world.setDataSourceId(result.getInt(1));

                    world.name = result.getString(2);

                    boolean cityCreation = result.getBoolean(3);

                    String str = result.getString(4);
                    Message message;
                    if(str == null)
                        message = null;
                    else
                        message = new Message(str);
                    pst.close();

                    return new Nature(mineCity, world, message, permStorage, permStorage, !cityCreation);
                }
            }

            if(id == 0)
                world.setDataSourceId(insertWorld(connection, world));

            return new Nature(mineCity, world, permStorage, permStorage);
        }
        catch(SQLException e)
        {
            throw new DataSourceException(e);
        }
    }

    @NotNull
    @Override
    public Supplier<Stream<String>> cityNameSupplier()
    {
        return cityNames::stream;
    }

    @NotNull
    @Override
    public Optional<Set<String>> getGroupNames(@NotNull String cityName)
    {
        Set<String> set = groupNames.get(identity(cityName));
        if(set == null)
            return Optional.empty();
        return Optional.of(Collections.unmodifiableSet(set));
    }

    @NotNull
    @Override
    public Map<String, Set<String>> getGroups()
    {
        return Collections.unmodifiableMap(groupNames);
    }

    @Slow
    @Override
    public void close() throws DataSourceException
    {
        try
        {
            connection.close();
        }
        catch(SQLException e)
        {
            throw new DataSourceException(e);
        }
    }
}
