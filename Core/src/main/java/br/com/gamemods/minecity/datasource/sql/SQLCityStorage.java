package br.com.gamemods.minecity.datasource.sql;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.datasource.api.DataSourceException;
import br.com.gamemods.minecity.datasource.api.ICityStorage;
import br.com.gamemods.minecity.datasource.api.unchecked.DBConsumer;
import br.com.gamemods.minecity.structure.City;
import br.com.gamemods.minecity.structure.Island;
import br.com.gamemods.minecity.structure.IslandArea;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class SQLCityStorage implements ICityStorage
{
    @NotNull
    private SQLSource source;

    @NotNull
    private SQLConnection connection;

    SQLCityStorage(@NotNull SQLSource source, @NotNull SQLConnection connection)
    {
        this.source = source;
        this.connection = connection;
    }

    @Override
    public void setOwner(@NotNull City city, @Nullable PlayerID owner) throws DataSourceException, IllegalStateException
    {
        if(Objects.equals(city.getOwner(), owner))
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
                    throw new DataSourceException("Tried to change the owner of "+ cityId+" from "+city.getOwner()+" to "+owner+" but nothing changed");
            }
        }
        catch(SQLException e)
        {
            throw new DataSourceException(e);
        }
    }

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
            int changes = pst.executeUpdate();
            if(enforce && changes != 1)
                throw new DataSourceException("Expecting 1 change, "+changes+" changed");
        }
    }

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
            }

            transaction.commit();

            sqlIsland.chunkCount = sqlIsland.maxX = sqlIsland.minX = sqlIsland.maxZ = sqlIsland.minZ = 0;
            source.mineCity.loadedChunks().values().stream().filter(c-> island.equals(c.getIsland()))
                    .forEach(c -> {
                        try
                        {
                            source.mineCity.reloadChunk(c.chunk);
                        }
                        catch(DataSourceException e)
                        {
                            e.printStackTrace();
                        }
                    });
        }
        catch(SQLException e)
        {
            throw new DataSourceException(e);
        }
    }

    private void updateCount(Connection transaction, SQLIsland sqlIsland) throws SQLException
    {
        try(PreparedStatement pst = transaction.prepareStatement(
                "SELECT MIN(x), MAX(x), MIN(z), MAX(z), COUNT(*) FROM minecity_chunks WHERE island_id=?"
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

    @Override
    public void disclaim(@NotNull ChunkPos chunk, @NotNull Island island)
            throws DataSourceException, IllegalArgumentException
    {
        SQLIsland sqlIsland = (SQLIsland) island;
        if(sqlIsland.chunkCount == 0) throw new IllegalArgumentException();

        try(Connection transaction = connection.transaction())
        {
            disclaim(transaction, chunk, sqlIsland.id, true);
            transaction.commit();

            updateCount(transaction, sqlIsland);
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

    @NotNull
    @Override
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
            disclaim(transaction, chunk, sqlIsland.id, true);
            int worldId = source.worldId(transaction, sqlIsland.world);

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
                              "WHERE world_id="+worldId+" AND island_id="+sqlIsland.id+" AND ("+sb+");"
                );

                SQLIsland newIsland = new SQLIsland(islandId, minX, maxX, minZ, maxZ, group.size(), sqlIsland.world);
                expected[i++] = newIsland.chunkCount;
                islands.add(newIsland);
            }

            int[] result = stm.executeBatch();
            if(!Arrays.equals(expected, result))
            {
                throw new DataSourceException("Unexpected result after reclaiming to new islands. " +
                        "Expected: "+Arrays.toString(expected)+" Result: "+Arrays.toString(result));
            }

            transaction.commit();

            updateCount(transaction, sqlIsland);
            return islands;
        }
        catch(SQLException e)
        {
            throw new DataSourceException(e);
        }
    }

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
                    "SELECT x, z FROM minecity_chunks WHERE island_id=? AND world_id=?"
            ))
            {
                pst.setInt(1, sqlIsland.id);
                pst.setInt(2, source.worldId(connection, sqlIsland.world));
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

    @Override
    public void setName(@NotNull City city, @NotNull String identity, @NotNull String name) throws DataSourceException
    {
        try(Connection transaction = connection.transaction())
        {
            if(identity.equals(city.getIdentityName()))
                try(PreparedStatement pst = transaction.prepareStatement(
                        "UPDATE minecity_city SET `display_name`=? WHERE city_id=?"
                ))
                {
                    pst.setString(1, name);
                    pst.setInt(2, city.getId());
                    int changes = pst.executeUpdate();
                    if(changes != 1)
                        throw new DataSourceException("Changes: "+changes+" Expected: 1");
                }
            else
                try(PreparedStatement pst = transaction.prepareStatement(
                        "UPDATE minecity_city SET `name`=?, display_name=? WHERE city_id=?"
                ))
                {
                    pst.setString(1, identity);
                    pst.setString(2, name);
                    pst.setInt(3, city.getId());
                    int changes = pst.executeUpdate();
                    if(changes != 1)
                        throw new DataSourceException("Changes: "+changes+" Expected: 1");
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
    public Island createIsland(@NotNull City city, @NotNull ChunkPos chunk)
            throws DataSourceException, IllegalStateException
    {
        int cityId = city.getId();
        if(cityId <= 0) throw new IllegalStateException("The city is not registered");

        try(Connection transaction = connection.transaction())
        {
            SQLIsland island = new SQLIsland(source.createIsland(transaction, cityId, chunk.world), chunk);
            source.createClaim(transaction, island.id, chunk);
            island.city = city;

            transaction.commit();

            source.mineCity.reloadChunk(chunk);
            return island;
        }
        catch(SQLException e)
        {
            throw new DataSourceException(e);
        }
    }

    @Override
    public void claim(@NotNull Island island, @NotNull ChunkPos chunk) throws DataSourceException
    {
        SQLIsland sqlIsland = (SQLIsland) island;
        try
        {
            source.createClaim(connection.connect(), sqlIsland.id, chunk);
            sqlIsland.add(chunk);

            source.mineCity.reloadChunk(chunk);
        }
        catch(SQLException e)
        {
            throw new DataSourceException(e);
        }
    }

    @NotNull
    @Override
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
                    int worldId = result.getInt(1);
                    WorldDim world = source.worldDimMap.get(worldId);
                    if(world == null)
                    {
                        world = new WorldDim(worldId, result.getInt(4), result.getString(5), result.getString(6));
                        source.worldDimMap.put(worldId, world);
                    }

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

            chunksToUpdate.stream().forEach((DBConsumer<ChunkPos>) pos-> source.mineCity.reloadChunk(pos));
            return mainIsland;
        }
        catch(SQLException e)
        {
            throw new DataSourceException(e);
        }
    }

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
}
