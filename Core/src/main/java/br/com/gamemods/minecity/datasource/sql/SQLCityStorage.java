package br.com.gamemods.minecity.datasource.sql;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.BlockPos;
import br.com.gamemods.minecity.datasource.api.DataSourceException;
import br.com.gamemods.minecity.datasource.api.ICityStorage;
import br.com.gamemods.minecity.structure.City;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;

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
