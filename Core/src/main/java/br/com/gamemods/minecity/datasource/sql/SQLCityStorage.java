package br.com.gamemods.minecity.datasource.sql;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.datasource.api.DataSourceException;
import br.com.gamemods.minecity.datasource.api.ICityStorage;
import br.com.gamemods.minecity.datasource.api.unchecked.DBConsumer;
import br.com.gamemods.minecity.structure.City;
import br.com.gamemods.minecity.structure.ClaimedChunk;
import br.com.gamemods.minecity.structure.Island;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.*;
import java.util.stream.Stream;

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

    @NotNull
    @Override
    public Island createIsland(@NotNull City city, @NotNull ChunkPos chunk)
            throws DataSourceException, IllegalStateException
    {
        int cityId = city.getId();
        if(cityId <= 0) throw new IllegalStateException("The city is not registered");

        try(Connection transaction = connection.transaction())
        {
            SQLIsland island = new SQLIsland(source.createIsland(transaction, cityId, chunk), chunk);
            island.city = city;

            source.createClaim(transaction, island.id, chunk);
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

        Stream<SQLIsland> sqlIslands = islands.stream().map(island -> (SQLIsland) island);
        SQLIsland mainIsland = sqlIslands.max((a, b) -> a.getChunkCount() - b.getChunkCount()).get();
        Stream<SQLIsland> merge = sqlIslands.filter(island -> island != mainIsland);
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
