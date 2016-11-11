package br.com.gamemods.minecity.datasource.sql;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.Slow;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.EntityID;
import br.com.gamemods.minecity.api.permission.Group;
import br.com.gamemods.minecity.api.permission.Identity;
import br.com.gamemods.minecity.api.permission.OptionalPlayer;
import br.com.gamemods.minecity.api.shape.Shape;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.api.world.MinecraftEntity;
import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.datasource.api.DataSourceException;
import br.com.gamemods.minecity.datasource.api.ICityStorage;
import br.com.gamemods.minecity.datasource.api.unchecked.DBConsumer;
import br.com.gamemods.minecity.economy.Tax;
import br.com.gamemods.minecity.structure.City;
import br.com.gamemods.minecity.structure.Island;
import br.com.gamemods.minecity.structure.IslandArea;
import br.com.gamemods.minecity.structure.Plot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SQLCityStorage implements ICityStorage
{
    @NotNull
    private final SQLSource source;

    @NotNull
    private final SQLConnection connection;

    @NotNull
    private final SQLPermStorage permStorage;

    SQLCityStorage(@NotNull SQLSource source, @NotNull SQLConnection connection, @NotNull SQLPermStorage permStorage)
    {
        this.source = source;
        this.connection = connection;
        this.permStorage = permStorage;
    }

    @Override
    public void deleteCity(@NotNull City city) throws DataSourceException
    {
        int cityId = city.getId();
        if(cityId <= 0)
            throw new IllegalStateException("cityId = "+cityId);

        try(Connection transaction = connection.transaction())
        {
            try(PreparedStatement pst = transaction.prepareStatement(
                    "DELETE FROM minecity_city WHERE city_id=?"
            ))
            {
                pst.setInt(1, cityId);
                source.executeUpdate(pst, 1);
                transaction.commit();
                source.cityNames.remove(city.getName());
                source.groupNames.remove(city.getIdentityName());
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
    @Override
    public void setOwner(@NotNull City city, @NotNull OptionalPlayer owner) throws DataSourceException, IllegalStateException
    {
        if(city.owner().equals(owner))
            return;

        int cityId = city.getId();
        if(cityId <= 0) throw new IllegalStateException("The city is not registered");

        try
        {
            Connection connection = this.connection.connect();
            int ownerId = source.playerId(connection, owner);

            try(PreparedStatement pst = connection.prepareStatement(
                    "UPDATE `minecity_city` SET `owner`=? WHERE `city_id`=?"
            ))
            {
                source.setNullableInt(pst, 1, ownerId);
                pst.setInt(2, cityId);
                if(pst.executeUpdate() <= 0)
                    throw new DataSourceException("Tried to change the owner of "+ cityId+" from "+city.owner()+" to "+owner+" but nothing changed");
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
    public Collection<ChunkPos> reserve(@NotNull IslandArea reserve) throws DataSourceException
    {
        SQLIsland sqlIsland = (SQLIsland) reserve.island;
        StringBuilder sbx = new StringBuilder(), sbz = new StringBuilder();
        Runnable build = ()->
        {
            sbx.setLength(0); sbz.setLength(0);
            reserve.claims().forEach(c->{ sbx.append(c.x).append(','); sbz.append(c.z).append(','); });
            if(sbx.length() > 0)
                sbx.setLength(sbx.length() - 1);
            if(sbz.length() > 0)
                sbz.setLength(sbz.length() - 1);
        };
        build.run();

        try(Connection transaction = connection.transaction(); Statement stm = transaction.createStatement())
        {
            try
            {
                int worldId = source.worldId(transaction, sqlIsland.world);
                Collection<ChunkPos> chunks = new HashSet<>();
                String x = sbx.toString(), z = sbz.toString();

                if(!x.isEmpty())
                {

                    ResultSet results = stm.executeQuery("SELECT x, z FROM minecity_chunks WHERE world_id="+worldId+" AND island_id!="+sqlIsland.id+" AND x IN("+x+") AND z IN("+z+");");
                    while(results.next())
                        reserve.setClaimed(results.getInt(1), results.getInt(2), false);
                    results.close();

                    results = stm.executeQuery("SELECT x,z FROM minecity_chunks WHERE island_id="+sqlIsland.id+" LIMIT 1");
                    results.next();
                    ChunkPos pos = new ChunkPos(sqlIsland.world, results.getInt(1), results.getInt(2));
                    results.close();

                    Set<ChunkPos> valid = reserve.contiguous(pos);
                    reserve.claims().filter(c-> !valid.contains(c)).forEach(c-> reserve.setClaimed(c, false));
                    build.run();
                    x = sbx.toString(); z = sbz.toString();
                }


                String deleteCond = "island_id="+sqlIsland.id+" AND reserve=1";
                if(!x.isEmpty())
                    deleteCond+= " AND x NOT IN ("+x+") AND z NOT IN ("+z+")";

                ResultSet results = stm.executeQuery("SELECT x,z FROM minecity_chunks WHERE " + deleteCond+";");
                while(results.next())
                    chunks.add(new ChunkPos(sqlIsland.world, results.getInt(1), results.getInt(2)));
                results.close();

                stm.executeUpdate("DELETE FROM minecity_chunks WHERE "+deleteCond+";");

                if(!x.isEmpty())
                {
                    results = stm.executeQuery("SELECT x, z FROM minecity_chunks WHERE world_id="+worldId+" AND x IN("+x+") AND z IN("+z+");");
                    while(results.next())
                        reserve.setClaimed(results.getInt(1), results.getInt(2), false);
                    results.close();

                    sbx.setLength(0);
                    List<ChunkPos> insert = reserve.claims().collect(Collectors.toList());
                    chunks.addAll(insert);
                    insert.forEach(c->
                            sbx.append('(').append(worldId).append(',').append(c.x).append(',').append(c.z).append(',')
                               .append(sqlIsland.id).append(",1),")
                    );
                    if(sbx.length() > 0)
                    {
                        sbx.setLength(sbx.length()-1);
                        stm.executeUpdate("INSERT INTO minecity_chunks(world_id,x,z,island_id,reserve) VALUES "+sbx+";");
                    }
                }
                transaction.commit();
                return chunks;
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
    private void disclaim(Connection transaction, ChunkPos chunk, int islandId, boolean enforce) throws DataSourceException, SQLException
    {
        try(PreparedStatement pst = transaction.prepareStatement(
                "DELETE FROM `minecity_chunks` WHERE `world_id`=? AND `x`=? AND `z`=? AND `island_id`=?"
        ))
        {
            pst.setInt(1, source.worldId(transaction, chunk.world));
            pst.setInt(2, chunk.x);
            pst.setInt(3, chunk.z);
            pst.setInt(4, islandId);
            if(enforce)
                source.executeUpdate(pst, 1);
            else
                pst.executeUpdate();
        }
    }

    @Slow
    @Override
    public void deleteIsland(@NotNull Island island) throws DataSourceException, IllegalArgumentException
    {
        SQLIsland sqlIsland = (SQLIsland) island;
        if(sqlIsland.chunkCount == 0) throw new IllegalArgumentException();

        try(Connection transaction = this.connection.transaction())
        {
            try(PreparedStatement pst = transaction.prepareStatement(
                    "DELETE FROM `minecity_islands` WHERE `island_id`=?"
            ))
            {
                pst.setInt(1, sqlIsland.id);
                int i = pst.executeUpdate();
                if(i != 1)
                    throw new DataSourceException("Expecting 1 change, "+i+" changed");

                transaction.commit();
            }
            catch(Exception e)
            {
                transaction.rollback();
                throw e;
            }

            sqlIsland.chunkCount = sqlIsland.maxX = sqlIsland.minX = sqlIsland.maxZ = sqlIsland.minZ = 0;
            source.mineCity.reloadChunksUnchecked(c-> c.getIsland().filter(island::equals).isPresent());
        }
        catch(SQLException e)
        {
            throw new DataSourceException(e);
        }
    }

    @Slow
    private void updateCount(Connection transaction, SQLIsland sqlIsland) throws SQLException
    {
        try(PreparedStatement pst = transaction.prepareStatement(
                "SELECT MIN(x), MAX(x), MIN(z), MAX(z), COUNT(*) FROM minecity_chunks WHERE island_id=? AND reserve=0"
        ))
        {
            pst.setInt(1, sqlIsland.id);
            ResultSet result = pst.executeQuery();
            result.next();
            sqlIsland.minX = result.getInt(1);
            sqlIsland.maxX = result.getInt(2);
            sqlIsland.minZ = result.getInt(3);
            sqlIsland.maxZ = result.getInt(4);
            sqlIsland.chunkCount = result.getInt(5);
        }
    }

    @Slow
    @Override
    public void disclaim(@NotNull ChunkPos chunk, @NotNull Island island)
            throws DataSourceException, IllegalArgumentException
    {
        SQLIsland sqlIsland = (SQLIsland) island;
        if(sqlIsland.chunkCount == 0) throw new IllegalArgumentException();

        try(Connection transaction = connection.transaction())
        {
            try
            {
                disclaim(transaction, chunk, sqlIsland.id, true);
                transaction.commit();

                updateCount(transaction, sqlIsland);
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

        try
        {
            source.mineCity.reloadChunk(chunk);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    @Slow
    @NotNull
    @Override
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public Collection<Island> disclaim(@NotNull ChunkPos chunk, @NotNull Island island, @NotNull Set<Set<ChunkPos>> groups)
            throws DataSourceException, IllegalStateException, NoSuchElementException, ClassCastException, IllegalArgumentException
    {
        SQLIsland sqlIsland = (SQLIsland) island;
        if(sqlIsland.chunkCount == 0) throw new IllegalArgumentException();
        int cityId = sqlIsland.city.getId();

        Set<ChunkPos> mainGroup = groups.stream().max((a,b)-> a.size()-b.size() ).get();
        groups = groups.stream().filter(s-> s != mainGroup).collect(Collectors.toSet());

        try(Connection transaction = connection.transaction(); Statement stm = transaction.createStatement())
        {
            try
            {
                disclaim(transaction, chunk, sqlIsland.id, true);
                int worldId = source.worldId(transaction, sqlIsland.world);


                stm.executeUpdate("DELETE FROM minecity_chunks WHERE island_id="+sqlIsland.id+" AND reserve=1;");

                List<Island> islands = new ArrayList<>(groups.size());
                int[] expected = new int[groups.size()]; int i = 0;
                for(Set<ChunkPos> group : groups)
                {
                    int islandId = source.createIsland(transaction, cityId, sqlIsland.world);
                    StringBuilder sb = new StringBuilder();
                    int minX = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
                    for(ChunkPos pos : group)
                    {
                        minX = Math.min(minX, pos.x);
                        minZ = Math.min(minZ, pos.z);
                        maxX = Math.max(maxX, pos.x);
                        maxZ = Math.max(maxZ, pos.z);
                        sb.append("(x=").append(pos.x).append(" AND z=").append(pos.z).append(") OR");
                    }

                    sb.setLength(sb.length() - 3);

                    stm.addBatch("UPDATE minecity_chunks SET island_id="+islandId+" " +
                                  "WHERE world_id="+worldId+" AND island_id="+sqlIsland.id+" AND reserve=0 AND ("+sb+");"
                    );

                    SQLIsland newIsland = new SQLIsland(this, permStorage, islandId, minX, maxX, minZ, maxZ, group.size(), sqlIsland.world, Collections.emptySet());
                    newIsland.city = sqlIsland.city;
                    expected[i++] = newIsland.chunkCount;
                    islands.add(newIsland);
                }

                int[] result = stm.executeBatch();
                if(!Arrays.equals(expected, result))
                {
                    throw new DataSourceException("Unexpected result after reclaiming to new islands. " +
                            "Expected: "+Arrays.toString(expected)+" Result: "+Arrays.toString(result));
                }

                Collection<Plot> plots = sqlIsland.getPlots();
                List<Runnable> afterCommit = new ArrayList<>(plots.size());
                if(!plots.isEmpty())
                {
                    try(PreparedStatement select = transaction.prepareStatement(
                            "SELECT island_id FROM minecity_chunks WHERE world_id=? AND x=? AND z=? AND reserve=0"
                    ); PreparedStatement update = transaction.prepareStatement(
                            "UPDATE minecity_plots SET island_id=? WHERE plot_id=?"
                    ))
                    {
                        for(Plot plot: plots)
                        {
                            ChunkPos spawn = plot.getSpawn().getChunk();
                            select.setInt(1, worldId);
                            select.setInt(2, spawn.x);
                            select.setInt(3, spawn.z);
                            ResultSet selectRes = select.executeQuery();
                            if(!selectRes.next())
                                throw new DataSourceException("The plot id:"+plot.id+" name:"+plot.getIdentityName()+" is not in an island!");
                            int newIslandId = selectRes.getInt(1);
                            selectRes.close();

                            if(plot.getIsland().id == newIslandId)
                                continue;

                            SQLIsland newIsland = null;
                            for(Island possible : islands)
                            {
                                if(possible.id == newIslandId)
                                {
                                    newIsland = (SQLIsland) possible;
                                    break;
                                }
                            }

                            if(newIsland == null)
                                throw new DataSourceException("The plot id:"+plot.id+" name:"+plot.getIdentityName()+
                                        " would be moved to the island:"+newIslandId+" but that island wasn't created by " +
                                        "this disclaim!");

                            update.setInt(1, newIslandId);
                            update.setInt(2, plot.id);
                            source.executeUpdate(update, 1);

                            SQLIsland finalNewIsland = newIsland;
                            afterCommit.add(()-> sqlIsland.relocate(plot, finalNewIsland));
                        }
                    }
                }

                updateCount(transaction, sqlIsland);
                transaction.commit();
                afterCommit.forEach(Runnable::run);

                return islands;
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

    @Override
    public double invested(City city, double value) throws DataSourceException
    {
        try(Connection transaction = connection.transaction())
        {
            try
            {
                try(PreparedStatement pst = transaction.prepareStatement(
                        "UPDATE minecity_city SET `investment`=`investment`+? WHERE city_id=?"
                ))
                {
                    pst.setDouble(1, value);
                    pst.setInt(2, city.getId());
                    source.executeUpdate(pst, 1);
                }

                double investment;
                try(PreparedStatement pst = transaction.prepareStatement(
                        "SELECT `investment` FROM `minecity_city` WHERE `city_id`=?"
                ))
                {
                    pst.setInt(1, city.getId());
                    ResultSet result = pst.executeQuery();
                    result.next();
                    investment = result.getDouble(1);
                }

                transaction.commit();
                return investment;
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

    @Override
    public double invested(Plot plot, double value) throws DataSourceException
    {
        try(Connection transaction = connection.transaction())
        {
            try
            {
                try(PreparedStatement pst = transaction.prepareStatement(
                        "UPDATE minecity_plots SET `investment`=`investment`+? WHERE plot_id=?"
                ))
                {
                    pst.setDouble(1, value);
                    pst.setInt(2, plot.id);
                    source.executeUpdate(pst, 1);
                }

                double investment;
                try(PreparedStatement pst = transaction.prepareStatement(
                        "SELECT `investment` FROM `minecity_plots` WHERE `plot_id`=?"
                ))
                {
                    pst.setInt(1, plot.id);
                    ResultSet result = pst.executeQuery();
                    result.next();
                    investment = result.getDouble(1);
                }

                transaction.commit();
                return investment;
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
    @Override
    public void setName(@NotNull City city, @NotNull String identity, @NotNull String name) throws DataSourceException
    {
        try(Connection transaction = connection.transaction())
        {
            String previous = city.getName();
            try
            {
                if(identity.equals(city.getIdentityName()))
                    try(PreparedStatement pst = transaction.prepareStatement(
                            "UPDATE minecity_city SET `display_name`=? WHERE city_id=?"
                    ))
                    {
                        pst.setString(1, name);
                        pst.setInt(2, city.getId());
                        source.executeUpdate(pst, 1);
                    }
                else
                    try(PreparedStatement pst = transaction.prepareStatement(
                            "UPDATE minecity_city SET `name`=?, display_name=? WHERE city_id=?"
                    ))
                    {
                        pst.setString(1, identity);
                        pst.setString(2, name);
                        pst.setInt(3, city.getId());
                        source.executeUpdate(pst, 1);
                    }

                transaction.commit();
            }
            catch(Exception e)
            {
                transaction.rollback();
                throw e;
            }
            source.cityNames.remove(previous);
            source.cityNames.add(name);
            Set<String> groups = source.groupNames.remove(identity);
            if(groups != null)
                source.groupNames.put(city.getIdentityName(), groups);
        }
        catch(SQLException e)
        {
            throw new DataSourceException(e);
        }
    }

    @Slow
    @NotNull
    @Override
    public Group createGroup(@NotNull City city, @NotNull String id, @NotNull String name) throws DataSourceException
    {
        try(Connection transaction = connection.transaction())
        {
            try(PreparedStatement pst = transaction.prepareStatement(
                    "INSERT INTO minecity_groups(city_id,name,display_name) VALUES(?,?,?)"
                , PreparedStatement.RETURN_GENERATED_KEYS
            ))
            {
                pst.setInt(1, city.getId());
                pst.setString(2, id);
                pst.setString(3, name);
                pst.executeUpdate();
                ResultSet generatedKeys = pst.getGeneratedKeys();
                generatedKeys.next();
                int groupId = generatedKeys.getInt(1);

                transaction.commit();
                source.groupNames.computeIfAbsent(city.getIdentityName(), i-> new HashSet<>(1)).add(name);
                return new Group(this, groupId, city, id, name, Collections.emptySet(), Collections.emptySet());
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
    @Override
    public void setName(@NotNull Group group, @NotNull String identity, @NotNull String name) throws DataSourceException
    {
        try(Connection transaction = connection.connect())
        {
            try(PreparedStatement pst = transaction.prepareStatement(
                    "UPDATE minecity_groups SET `name`=?, display_name=? WHERE group_id=?"
            ))
            {
                pst.setString(1, identity);
                pst.setString(2, name);
                pst.setInt(3, group.id);
                int count = pst.executeUpdate();
                if(count != 1)
                    throw new DataSourceException("Expected 1 change but got "+count+" changes");

                Set<String> groups = source.groupNames.computeIfAbsent(group.home.getIdentityName(), i-> new HashSet<>(1));
                groups.remove(group.getName());
                groups.add(name);
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
    @Override
    public void deleteGroup(@NotNull Group group) throws DataSourceException
    {
        try(Connection transaction = connection.transaction())
        {
            try(PreparedStatement pst = transaction.prepareStatement(
                    "DELETE FROM minecity_groups WHERE group_id=?"
            ))
            {
                pst.setInt(1, group.id);
                source.executeUpdate(pst, 1);
                transaction.commit();
                source.groupNames.computeIfAbsent(group.home.getIdentityName(), i-> new HashSet<>(0)).remove(group.getName());
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
    @Override
    public void addMember(@NotNull Group group, @NotNull Identity<?> member)
            throws DataSourceException, UnsupportedOperationException
    {
        try(Connection transaction = this.connection.transaction(); Statement stm = transaction.createStatement())
        {
            try
            {
                switch(member.getType())
                {
                    case PLAYER:
                    {
                        int playerId = source.playerId(transaction, (PlayerID) member);
                        stm.executeUpdate("INSERT INTO minecity_group_players(group_id,player_id) VALUES("+group.id+","+playerId+");");
                        break;
                    }
                    case ENTITY:
                    {
                        int entityId = source.entityId(transaction, (EntityID) member);
                        stm.executeUpdate("INSERT INTO minecity_group_entities(group_id,entity_id) VALUES("+group.id+","+entityId+");");
                        break;
                    }
                    default:
                        throw new UnsupportedOperationException("Unsupported identity type: "+member.getType());
                }

                transaction.commit();
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
        catch(ClassCastException e)
        {
            throw new UnsupportedOperationException(e);
        }
    }

    @Slow
    @Override
    public void addManager(@NotNull Group group, @NotNull PlayerID manager)
            throws DataSourceException, UnsupportedOperationException
    {
        try(Connection transaction = this.connection.transaction(); Statement stm = transaction.createStatement())
        {
            try
            {
                int playerId = source.playerId(transaction, manager);
                stm.executeUpdate("INSERT INTO minecity_group_managers(group_id,player_id) VALUES("+group.id+","+playerId+");");

                transaction.commit();
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
        catch(ClassCastException e)
        {
            throw new UnsupportedOperationException(e);
        }
    }

    @Slow
    @Override
    public void removeMember(@NotNull Group group, @NotNull Identity<?> member)
            throws DataSourceException, UnsupportedOperationException
    {
        try(Connection transaction = this.connection.transaction(); Statement stm = transaction.createStatement())
        {
            try
            {
                switch(member.getType())
                {
                    case PLAYER:
                    {
                        int playerId = source.playerId(transaction, (PlayerID) member);
                        int changes = stm.executeUpdate("DELETE FROM minecity_group_players WHERE group_id="+group.id+" AND player_id="+playerId+";");
                        if(changes != 1)
                            throw new DataSourceException("Expected 1 change, got "+changes+" changes");
                        break;
                    }
                    case ENTITY:
                    {
                        int entityId = source.entityId(transaction, (EntityID) member);
                        int changes = stm.executeUpdate("INSERT INTO minecity_group_entities WHERE group_id="+group.id+" AND entity_id="+entityId+";");
                        if(changes != 1)
                            throw new DataSourceException("Expected 1 change, got "+changes+" changes");
                        break;
                    }
                    default:
                        throw new UnsupportedOperationException("Unsupported identity type: "+member.getType());
                }

                transaction.commit();
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
        catch(ClassCastException e)
        {
            throw new UnsupportedOperationException(e);
        }
    }

    @Slow
    @Override
    public void removeManager(@NotNull Group group, @NotNull PlayerID manager)
            throws DataSourceException, UnsupportedOperationException
    {
        try(Connection transaction = this.connection.transaction(); Statement stm = transaction.createStatement())
        {
            try
            {
                int playerId = source.playerId(transaction, manager);
                int changes = stm.executeUpdate("DELETE FROM minecity_group_managers WHERE group_id="+group.id+" AND player_id="+playerId+";");
                if(changes != 1)
                    throw new DataSourceException("Expected 1 change, got "+changes+" changes");

                transaction.commit();
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
        catch(ClassCastException e)
        {
            throw new UnsupportedOperationException(e);
        }
    }

    @Slow
    @NotNull
    @Override
    public Collection<Group> loadGroups(@NotNull City city) throws DataSourceException
    {
        try
        {
            Connection connection = this.connection.connect();
            try(PreparedStatement groupPst = connection.prepareStatement(
                    "SELECT group_id, name, display_name FROM minecity_groups WHERE city_id=?"
                );
                PreparedStatement playerPst = connection.prepareStatement(
                    "SELECT p.player_id, player_uuid, player_name " +
                            "FROM minecity_group_players gp " +
                            "INNER JOIN minecity_players p ON p.player_id = gp.player_id " +
                            "WHERE gp.group_id = ?"
                );
                PreparedStatement entityPst = connection.prepareStatement(
                        "SELECT e.entity_id, entity_uuid, entity_name, entity_type " +
                            "FROM minecity_group_entities ge " +
                            "INNER JOIN minecity_entities e ON e.entity_id = ge.entity_id " +
                            "WHERE ge.group_id = ?"
                );
                PreparedStatement managersPst = connection.prepareStatement(
                        "SELECT p.player_id, player_uuid, player_name " +
                                "FROM minecity_group_managers gm " +
                                "INNER JOIN minecity_players p ON p.player_id = gm.player_id " +
                                "WHERE gm.group_id = ?"
                )
            )
            {
                groupPst.setInt(1, city.getId());
                ResultSet groupResult = groupPst.executeQuery();
                List<Group> groups = new ArrayList<>();
                while(groupResult.next())
                {
                    int groupId = groupResult.getInt(1);
                    playerPst.setInt(1, groupId);
                    entityPst.setInt(1, groupId);
                    managersPst.setInt(1, groupId);

                    List<PlayerID> managers = new ArrayList<>();
                    List<Identity<?>> members = new ArrayList<>();

                    try(ResultSet managerResult = managersPst.executeQuery())
                    {
                        while(managerResult.next())
                            members.add(new PlayerID(managerResult.getInt("player_id"),
                                    source.uuid(managerResult.getBytes("player_uuid")),
                                    managerResult.getString("player_name")
                            ));
                    }

                    try(ResultSet memberResult = playerPst.executeQuery())
                    {
                        while(memberResult.next())
                            members.add(new PlayerID(memberResult.getInt("player_id"),
                                    source.uuid(memberResult.getBytes("player_uuid")),
                                    memberResult.getString("player_name")
                            ));
                    }

                    try(ResultSet memberResult = entityPst.executeQuery())
                    {
                        while(memberResult.next())
                            members.add(new EntityID(memberResult.getInt("entity_id"),
                                    MinecraftEntity.Type.valueOf(memberResult.getString("entity_type")),
                                    source.uuid(memberResult.getBytes("entity_uuid")),
                                    memberResult.getString("entity_name")
                            ));
                    }

                    groups.add(new Group(this, groupId, city, groupResult.getString(2), groupResult.getString(3), members, managers));
                }

                return groups;
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
    public Island createIsland(@NotNull City city, @NotNull ChunkPos chunk)
            throws DataSourceException, IllegalStateException
    {
        int cityId = city.getId();
        if(cityId <= 0) throw new IllegalStateException("The city is not registered");

        try(Connection transaction = connection.transaction())
        {
            try
            {
                SQLIsland island = new SQLIsland(this, permStorage, source.createIsland(transaction, cityId, chunk.world), chunk, city);
                source.createClaim(transaction, island.id, chunk);
                island.city = city;

                transaction.commit();

                return island;
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
    @Override
    public void claim(@NotNull Island island, @NotNull ChunkPos chunk) throws DataSourceException
    {
        SQLIsland sqlIsland = (SQLIsland) island;
        try(Connection transaction = connection.transaction())
        {
            try
            {
                source.createClaim(connection.connect(), sqlIsland.id, chunk);
            }
            catch(Exception e)
            {
                transaction.rollback();
                throw e;
            }
            sqlIsland.add(chunk);

            source.mineCity.reloadChunk(chunk);
        }
        catch(SQLException e)
        {
            throw new DataSourceException(e);
        }
    }

    @Slow
    @NotNull
    @Override
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public Island claim(@NotNull Set<Island> islands, @NotNull ChunkPos chunk)
            throws DataSourceException, IllegalStateException, NoSuchElementException
    {
        if(islands.size() == 1)
        {
            Island island = islands.iterator().next();
            claim(island, chunk);
            return island;
        }

        List<SQLIsland> sqlIslands = islands.stream().map(island -> (SQLIsland) island).collect(Collectors.toList());
        SQLIsland mainIsland = sqlIslands.stream().max((a, b) -> a.getChunkCount() - b.getChunkCount()).get();
        List<SQLIsland> merge = sqlIslands.stream().filter(island -> island != mainIsland).collect(Collectors.toList());
        try(Connection transaction = connection.transaction(); Statement stm = transaction.createStatement())
        {
            try
            {
                StringBuilder sb = new StringBuilder();
                merge.forEach(island -> sb.append(island.id).append(", "));
                sb.setLength(sb.length()-2);
                String array = sb.toString();

                List<ChunkPos> chunksToUpdate = new ArrayList<>();
                try(ResultSet result = stm.executeQuery("SELECT c.world_id, c.x, c.z, w.dim, w.world, w.name " +
                        "FROM minecity_chunks c INNER JOIN minecity_world w ON c.world_id = w.world_id " +
                        "WHERE c.island_id IN("+array+");")
                ){
                    while(result.next())
                    {
                        WorldDim world = source.world(result.getInt(1),()->result.getInt(4),()->result.getString(5),()->result.getString(6));
                        chunksToUpdate.add(new ChunkPos(world, result.getInt(2), result.getInt(3)));
                    }
                }

                stm.executeUpdate("UPDATE `minecity_chunks` SET `island_id`="+mainIsland.id+" WHERE `island_id` IN("+array+");");
                stm.executeUpdate("DELETE FROM `minecity_islands` WHERE `island_id` IN("+array+");");
                source.createClaim(transaction, mainIsland.id, chunk);

                transaction.commit();

                mainIsland.add(chunk);

                merge.forEach(island -> {
                    mainIsland.minX = Math.min(mainIsland.minX, island.minX);
                    mainIsland.maxX = Math.max(mainIsland.maxX, island.maxX);
                    mainIsland.minZ = Math.min(mainIsland.minZ, island.minZ);
                    mainIsland.maxZ = Math.max(mainIsland.maxZ, island.maxZ);
                    mainIsland.chunkCount += island.chunkCount;

                    island.minX = island.maxX = island.minZ = island.maxZ = island.chunkCount = 0;
                });

                chunksToUpdate.forEach((DBConsumer<ChunkPos>) source.mineCity::reloadChunk);
                return mainIsland;
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
    @Override
    public void setSpawn(@NotNull City city, @NotNull BlockPos spawn) throws DataSourceException, IllegalStateException
    {
        if(city.getSpawn().equals(spawn))
            return;

        int cityId = city.getId();
        if(cityId <= 0) throw new IllegalStateException("The city is not registered");

        try
        {
            Connection connection = this.connection.connect();
            int worldId = source.worldId(connection, spawn.world);

            try(PreparedStatement pst = connection.prepareStatement(
                    "UPDATE `minecity_city` SET `spawn_world`=?, `spawn_x`=?, `spawn_y`=?, `spawn_z`=? WHERE `city_id`=?"
            ))
            {
                pst.setInt(1, worldId);
                pst.setInt(2, spawn.x);
                //noinspection SuspiciousNameCombination
                pst.setInt(3, spawn.y);
                pst.setInt(4, spawn.z);
                pst.setInt(5, cityId);
                if(pst.executeUpdate() <= 0)
                    throw new DataSourceException("Tried to change spawn of "+ cityId+" from "+city.getSpawn()+" to "+spawn+" but nothing changed");
            }
        }
        catch(SQLException e)
        {
            throw new DataSourceException(e);
        }
    }

    @Slow
    @Override
    @SuppressWarnings("SuspiciousNameCombination")
    public int createPlot(Plot plot) throws DataSourceException
    {
        try(Connection transaction = connection.transaction())
        {
            try
            {
                int plotId;
                try(PreparedStatement pst = transaction.prepareStatement(
                        "INSERT INTO minecity_plots(island_id,name,display_name,owner,spawn_x,spawn_y,spawn_z,shape,tax_accepted_flat,tax_accepted_percent,tax_applied_flat,tax_applied_percent,investment) " +
                        "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS
                ))
                {
                    pst.setInt(1, plot.getIsland().getId());
                    pst.setString(2, plot.getIdentityName());
                    pst.setString(3, plot.getName());
                    source.setNullableInt(pst, 4, source.playerId(transaction, plot.getOwner().orElse(null)));
                    BlockPos spawn = plot.getSpawn();
                    pst.setInt(5, spawn.x);
                    pst.setInt(6, spawn.y);
                    pst.setInt(7, spawn.z);
                    pst.setBytes(8, plot.getShape().serializeBytes());
                    pst.setDouble(9, plot.getAcceptedTax().getFlat());
                    pst.setDouble(10, plot.getAcceptedTax().getPercent());
                    pst.setDouble(11, plot.getAppliedTax().getFlat());
                    pst.setDouble(12, plot.getAppliedTax().getPercent());
                    pst.setDouble(13, plot.getInvestment());
                    pst.executeUpdate();

                    ResultSet result = pst.getGeneratedKeys();
                    result.next();
                    plotId = result.getInt(1);
                }

                transaction.commit();
                return plotId;
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
    @Override
    public void setOwner(@NotNull Plot plot, @Nullable PlayerID owner) throws DataSourceException, IllegalStateException
    {
        try(Connection transaction = connection.transaction())
        {
            try
            {
                try(PreparedStatement pst = transaction.prepareStatement(
                        "UPDATE minecity_plots SET owner=? WHERE plot_id=?"
                ))
                {
                    source.setNullableInt(pst, 1, source.playerId(transaction, owner));
                    pst.setInt(2, plot.id);
                    source.executeUpdate(pst, 1);
                }

                transaction.commit();
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
    @Override
    public void setShape(@NotNull Plot plot, @NotNull Shape shape, BlockPos spawn, @NotNull Island newIsland) throws DataSourceException
    {
        try(Connection transaction = connection.transaction())
        {
            try
            {
                try(PreparedStatement pst = transaction.prepareStatement(
                        "UPDATE minecity_plots SET shape=?, island_id=? WHERE plot_id=?"
                ))
                {
                    pst.setBytes(1, shape.serializeBytes());
                    pst.setInt(2, newIsland.id);
                    pst.setInt(3, plot.id);
                    source.executeUpdate(pst, 1);
                }

                setSpawn(transaction, plot, spawn);

                transaction.commit();
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
    @Override
    public void setName(@NotNull Plot plot, @NotNull String identity, @NotNull String name) throws DataSourceException
    {
        try(Connection transaction = connection.transaction())
        {
            try
            {
                try(PreparedStatement pst = transaction.prepareStatement(
                        "UPDATE minecity_plots SET `name`=?, `display_name`=? WHERE plot_id=?"
                ))
                {
                    pst.setString(1, identity);
                    pst.setString(2, name);
                    pst.setInt(3, plot.id);
                    source.executeUpdate(pst, 1);
                }

                transaction.commit();
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

    @SuppressWarnings("SuspiciousNameCombination")
    private void setSpawn(@NotNull Connection transaction, @NotNull Plot plot, @NotNull BlockPos spawn)
            throws DataSourceException, SQLException
    {
        try(PreparedStatement pst = transaction.prepareStatement(
                "UPDATE minecity_plots SET spawn_x=?, spawn_y=?, spawn_z=? WHERE plot_id=?"
        ))
        {
            pst.setInt(1, spawn.x);
            pst.setInt(2, spawn.y);
            pst.setInt(3, spawn.z);
            pst.setInt(4, plot.id);
            source.executeUpdate(pst, 1);
        }
    }

    @Slow
    @Override
    public void setSpawn(@NotNull Plot plot, @NotNull BlockPos spawn) throws DataSourceException
    {
        if(!spawn.world.equals(plot.getIsland().world))
            throw new IllegalArgumentException("Different world!");

        try(Connection transaction = connection.transaction())
        {
            try
            {
                setSpawn(transaction, plot, spawn);
                transaction.commit();
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
    @Override
    public void deletePlot(@NotNull Plot plot) throws DataSourceException
    {
        try(Connection transaction = connection.transaction())
        {
            try(PreparedStatement pst = transaction.prepareStatement(
                    "DELETE FROM minecity_plots WHERE plot_id=?"
            ))
            {
                pst.setInt(1, plot.id);
                source.executeUpdate(pst, 1);
            }
            catch(Exception e)
            {
                transaction.rollback();
                throw e;
            }

            transaction.commit();
        }
        catch(SQLException e)
        {
            throw new DataSourceException(e);
        }
    }

    @NotNull
    @Override
    public Set<Plot> loadPlots(@NotNull Island island) throws DataSourceException
    {
        try
        {
           try(PreparedStatement pst = connection.connect().prepareStatement(
                   "SELECT plot_id,`name`,display_name,spawn_x,spawn_y,spawn_z,shape, player_id,player_uuid,player_name,perm_denial_message, " +
                           "tax_accepted_flat, tax_accepted_percent, tax_applied_flat, tax_applied_percent, investment " +
                   "FROM minecity_plots LEFT JOIN minecity_players ON player_id=owner " +
                   "WHERE island_id=?"
           ))
           {
               pst.setInt(1, island.id);
               ResultSet result = pst.executeQuery();
               if(!result.next())
                   return Collections.emptySet();

               // Memory optimization, caching common Tax instances
               HashMap<Tax, Tax> taxCache = new HashMap<>(6);
               Tax t = island.getCity().getAppliedTax();
               taxCache.put(t, t);
               t = island.getCity().mineCity.costs.plotTaxApplied;
               taxCache.put(t, t);
               BiFunction<Double, Double, Tax> tax = (flat, percent)-> taxCache.computeIfAbsent(new Tax(flat, percent), Function.identity());

               HashSet<Plot> plots = new HashSet<>(3);
               do
               {
                   PlayerID owner;
                   int ownerId = result.getInt(8);
                   if(ownerId > 0)
                       owner = new PlayerID(source.uuid(result.getBytes(9)), result.getString(10));
                   else
                       owner = null;

                   Message denial;
                   String str = result.getString(11);
                   if(str == null)
                       denial = null;
                   else
                       denial = new Message("", "${msg}", new Object[]{"msg",str});

                   plots.add(new Plot(source.mineCity, this, permStorage, result.getInt(1), island, result.getString(2), result.getString(3), owner,
                           new BlockPos(island.world, result.getInt(4), result.getInt(5), result.getInt(6)),
                           Shape.deserializeBytes(result.getBytes(7)), denial,
                           tax.apply(result.getDouble("tax_accepted_flat"), result.getDouble("tax_accepted_percent")),
                           tax.apply(result.getDouble("tax_applied_flat"), result.getDouble("tax_applied_percent")),
                           result.getDouble("investment")
                   ));
               } while(result.next());

               return plots;
           }
        }
        catch(SQLException e)
        {
            throw new DataSourceException(e);
        }
    }
}
