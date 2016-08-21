package br.com.gamemods.minecity.datasource.sql;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.Slow;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.*;
import br.com.gamemods.minecity.api.world.MinecraftEntity;
import br.com.gamemods.minecity.datasource.api.DataSourceException;
import br.com.gamemods.minecity.datasource.api.IExceptPermissionStorage;
import br.com.gamemods.minecity.datasource.api.INatureStorage;
import br.com.gamemods.minecity.structure.City;
import br.com.gamemods.minecity.structure.Nature;
import br.com.gamemods.minecity.structure.Plot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class SQLPermStorage implements IExceptPermissionStorage, INatureStorage
{
    private final SQLConnection connection;
    private final SQLSource source;


    public SQLPermStorage(SQLSource sqlSource, SQLConnection connection)
    {
        this.connection = connection;
        this.source = sqlSource;
    }

    @Slow
    @Override
    public void setDefaultMessage(@NotNull SimpleFlagHolder holder, @Nullable Message message)
            throws DataSourceException
    {
        try
        {
            if(holder instanceof City)
            {
                int cityId = ((City) holder).getId();
                try(Connection transaction = connection.transaction())
                {
                    try
                    {
                        try(PreparedStatement pst = transaction.prepareStatement(
                                "UPDATE minecity_city SET perm_denial_message=? WHERE city_id=?"
                        ))
                        {
                            source.setNullableString(pst, 1, message == null || message.equals(holder.getDefaultMessage())?
                                    null : message.toString()
                            );
                            pst.setInt(2, cityId);
                            source.executeUpdate(pst, 1);
                        }

                        transaction.commit();
                        return;
                    }
                    catch(Exception e)
                    {
                        transaction.rollback();
                        throw e;
                    }
                }
            }
            else if(holder instanceof Plot)
            {
                int plotId = ((Plot) holder).id;
                try(Connection transaction = connection.transaction())
                {
                    try
                    {
                        try(PreparedStatement pst = transaction.prepareStatement(
                                "UPDATE minecity_plots SET perm_denial_message=? WHERE plot_id=?"
                        ))
                        {
                            source.setNullableString(pst, 1, message == null || message.equals(holder.getDefaultMessage())?
                                    null : message.toString()
                            );
                            pst.setInt(2, plotId);
                            source.executeUpdate(pst, 1);
                        }

                        transaction.commit();
                        return;
                    }
                    catch(Exception e)
                    {
                        transaction.rollback();
                        throw e;
                    }
                }
            }
            else if(holder instanceof Nature)
            {
                int worldId = ((Nature) holder).world.getDataSourceId();
                try(Connection transaction = connection.transaction())
                {
                    try
                    {
                        try(PreparedStatement pst = transaction.prepareStatement(
                                "UPDATE minecity_world SET perm_denial_message=? WHERE world_id=?"
                        ))
                        {
                            source.setNullableString(pst, 1, message == null || message.equals(holder.getDefaultMessage())?
                                    null : message.toString()
                            );
                            pst.setInt(2, worldId);
                            source.executeUpdate(pst, 1);
                        }

                        transaction.commit();
                        return;
                    }
                    catch(Exception e)
                    {
                        transaction.rollback();
                        throw e;
                    }
                }
            }
        }
        catch(SQLException e)
        {
            throw new DataSourceException(e);
        }

        throw new DataSourceException("Unsupported flag holder: "+holder);
    }

    @Override
    public void denyAll(SimpleFlagHolder holder, Map<? extends PermissionFlag, ? extends Message> flags)
            throws DataSourceException
    {
        if(flags.isEmpty())
            return;

        try
        {
            if(holder instanceof City)
            {
                int cityId = ((City) holder).getId();
                try(Connection transaction = connection.transaction())
                {
                    int size = flags.size();
                    try
                    {
                        try(PreparedStatement pst = transaction.prepareStatement(
                                "DELETE FROM minecity_city_perm_defaults " +
                                "WHERE city_id=? AND perm IN ("+String.join(",",Collections.nCopies(size,"?"))+")"
                        ))
                        {
                            pst.setInt(1, cityId);
                            int i = 2;
                            for(PermissionFlag flag : flags.keySet())
                                pst.setString(i++, flag.name());

                            pst.executeUpdate();
                        }

                        try(PreparedStatement pst = transaction.prepareStatement(
                                "INSERT INTO minecity_city_perm_defaults(city_id,perm,message) " +
                                "VALUES "+String.join(",",Collections.nCopies(size,"(?,?,?)"))
                        ))
                        {
                            int i = 1;
                            for(Map.Entry<? extends PermissionFlag, ? extends Message> entry : flags.entrySet())
                            {
                                pst.setInt(i++, cityId);
                                PermissionFlag flag = entry.getKey();
                                pst.setString(i++, flag.name());
                                Message message = entry.getValue();
                                source.setNullableString(pst, i++,
                                        message == null || message.equals(holder.getDefaultMessage(flag))?
                                                null: message.toString()
                                );
                            }

                            source.executeUpdate(pst, size);
                        }

                        transaction.commit();
                        return;
                    }
                    catch(Exception e)
                    {
                        transaction.rollback();
                        throw e;
                    }
                }
            }
            else if(holder instanceof Plot)
            {
                int plotId = ((Plot) holder).id;
                try(Connection transaction = connection.transaction())
                {
                    int size = flags.size();
                    try
                    {
                        try(PreparedStatement pst = transaction.prepareStatement(
                                "DELETE FROM minecity_plot_perm_defaults " +
                                        "WHERE plot_id=? AND perm IN ("+String.join(",",Collections.nCopies(size,"?"))+")"
                        ))
                        {
                            pst.setInt(1, plotId);
                            int i = 2;
                            for(PermissionFlag flag : flags.keySet())
                                pst.setString(i++, flag.name());

                            pst.executeUpdate();
                        }

                        try(PreparedStatement pst = transaction.prepareStatement(
                                "INSERT INTO minecity_plot_perm_defaults(plot_id,perm,message) " +
                                        "VALUES "+String.join(",",Collections.nCopies(size,"(?,?,?)"))
                        ))
                        {
                            int i = 1;
                            for(Map.Entry<? extends PermissionFlag, ? extends Message> entry : flags.entrySet())
                            {
                                pst.setInt(i++, plotId);
                                PermissionFlag flag = entry.getKey();
                                pst.setString(i++, flag.name());
                                Message message = entry.getValue();
                                source.setNullableString(pst, i++,
                                        message == null || message.equals(holder.getDefaultMessage(flag))?
                                                null: message.toString()
                                );
                            }

                            source.executeUpdate(pst, size);
                        }

                        transaction.commit();
                        return;
                    }
                    catch(Exception e)
                    {
                        transaction.rollback();
                        throw e;
                    }
                }
            }
            else if(holder instanceof Nature)
            {
                int worldId = ((Nature) holder).world.getDataSourceId();
                try(Connection transaction = connection.transaction())
                {
                    int size = flags.size();
                    try
                    {
                        try(PreparedStatement pst = transaction.prepareStatement(
                                "DELETE FROM minecity_world_perm_defaults " +
                                        "WHERE world_id=? AND perm IN ("+String.join(",",Collections.nCopies(size,"?"))+")"
                        ))
                        {
                            pst.setInt(1, worldId);
                            int i = 2;
                            for(PermissionFlag flag : flags.keySet())
                                pst.setString(i++, flag.name());

                            pst.executeUpdate();
                        }

                        try(PreparedStatement pst = transaction.prepareStatement(
                                "INSERT INTO minecity_world_perm_defaults(world_id,perm,message) " +
                                        "VALUES "+String.join(",",Collections.nCopies(size,"(?,?,?)"))
                        ))
                        {
                            int i = 1;
                            for(Map.Entry<? extends PermissionFlag, ? extends Message> entry : flags.entrySet())
                            {
                                pst.setInt(i++, worldId);
                                PermissionFlag flag = entry.getKey();
                                pst.setString(i++, flag.name());
                                Message message = entry.getValue();
                                source.setNullableString(pst, i++,
                                        message == null || message.equals(holder.getDefaultMessage(flag))?
                                                null: message.toString()
                                );
                            }

                            source.executeUpdate(pst, size);
                        }

                        transaction.commit();
                        return;
                    }
                    catch(Exception e)
                    {
                        transaction.rollback();
                        throw e;
                    }
                }
            }
        }
        catch(SQLException e)
        {
            throw new DataSourceException(e);
        }

        throw new DataSourceException("Unsupported flag holder: "+holder);
    }

    @Slow
    @Override
    public void deny(@NotNull SimpleFlagHolder holder, @NotNull PermissionFlag flag, @Nullable Message message)
            throws DataSourceException
    {
        try
        {
            if(holder instanceof City)
            {
                City city = (City) holder;
                try(Connection transaction = connection.transaction())
                {
                    try
                    {
                        try(PreparedStatement pst = transaction.prepareStatement(
                                "DELETE FROM minecity_city_perm_defaults WHERE city_id=? AND perm=?"
                        ))
                        {
                            pst.setInt(1, city.getId());
                            pst.setString(2, flag.name());
                            pst.executeUpdate();
                        }

                        try(PreparedStatement pst = transaction.prepareStatement(
                                "INSERT INTO minecity_city_perm_defaults(city_id,perm,message) VALUES(?,?,?)"
                        ))
                        {
                            pst.setInt(1, city.getId());
                            pst.setString(2, flag.name());
                            source.setNullableString(pst, 3,
                                    message == null || message.equals(holder.getDefaultMessage(flag))?
                                    null : message.toString()
                            );
                            source.executeUpdate(pst, 1);
                        }

                        transaction.commit();
                        return;
                    }
                    catch(Exception e)
                    {
                        transaction.rollback();
                        throw e;
                    }
                }
            }
            else if(holder instanceof Plot)
            {
                int plotId = ((Plot) holder).id;
                try(Connection transaction = connection.transaction())
                {
                    try
                    {
                        try(PreparedStatement pst = transaction.prepareStatement(
                                "DELETE FROM minecity_plot_perm_defaults WHERE plot_id=? AND perm=?"
                        ))
                        {
                            pst.setInt(1, plotId);
                            pst.setString(2, flag.name());
                            pst.executeUpdate();
                        }

                        try(PreparedStatement pst = transaction.prepareStatement(
                                "INSERT INTO minecity_plot_perm_defaults(plot_id,perm,message) VALUES(?,?,?)"
                        ))
                        {
                            pst.setInt(1, plotId);
                            pst.setString(2, flag.name());
                            source.setNullableString(pst, 3, message == null || message.equals(holder.getDefaultMessage(flag))?
                                    null : message.toString()
                            );
                            source.executeUpdate(pst, 1);
                        }

                        transaction.commit();
                        return;
                    }
                    catch(Exception e)
                    {
                        transaction.rollback();
                        throw e;
                    }
                }
            }
            else if(holder instanceof Nature)
            {
                int worldId = ((Nature) holder).world.getDataSourceId();
                try(Connection transaction = connection.transaction())
                {
                    try
                    {
                        try(PreparedStatement pst = transaction.prepareStatement(
                                "DELETE FROM minecity_world_perm_defaults WHERE world_id=? AND perm=?"
                        ))
                        {
                            pst.setInt(1, worldId);
                            pst.setString(2, flag.name());
                            pst.executeUpdate();
                        }

                        try(PreparedStatement pst = transaction.prepareStatement(
                                "INSERT INTO minecity_world_perm_defaults(world_id,perm,message) VALUES(?,?,?)"
                        ))
                        {
                            pst.setInt(1, worldId);
                            pst.setString(2, flag.name());
                            source.setNullableString(pst, 3, message == null || message.equals(holder.getDefaultMessage(flag))?
                                    null : message.toString()
                            );
                            source.executeUpdate(pst, 1);
                        }

                        transaction.commit();
                        return;
                    }
                    catch(Exception e)
                    {
                        transaction.rollback();
                        throw e;
                    }
                }
            }
        }
        catch(SQLException e)
        {
            throw new DataSourceException(e);
        }

        throw new DataSourceException("Unsupported flag holder: "+holder);
    }

    @Slow
    @Override
    public void allow(@NotNull SimpleFlagHolder holder, @NotNull PermissionFlag flag) throws DataSourceException
    {
        try
        {
            if(holder instanceof City)
            {
                City city = (City) holder;
                try(Connection transaction = connection.transaction())
                {
                    try
                    {
                        try(PreparedStatement pst = transaction.prepareStatement(
                                "DELETE FROM minecity_city_perm_defaults WHERE city_id=? AND perm=?"
                        ))
                        {
                            pst.setInt(1, city.getId());
                            pst.setString(2, flag.name());
                            pst.executeUpdate();
                        }

                        transaction.commit();
                        return;
                    }
                    catch(Exception e)
                    {
                        transaction.rollback();
                        throw e;
                    }
                }
            }
            else if(holder instanceof Plot)
            {
                int plotId = ((Plot) holder).id;
                try(Connection transaction = connection.transaction())
                {
                    try
                    {
                        try(PreparedStatement pst = transaction.prepareStatement(
                                "DELETE FROM minecity_plot_perm_defaults WHERE plot_id=? AND perm=?"
                        ))
                        {
                            pst.setInt(1, plotId);
                            pst.setString(2, flag.name());
                            pst.executeUpdate();
                        }

                        transaction.commit();
                        return;
                    }
                    catch(Exception e)
                    {
                        transaction.rollback();
                        throw e;
                    }
                }
            }
            else if(holder instanceof Nature)
            {
                int worldId = ((Nature) holder).world.getDataSourceId();
                try(Connection transaction = connection.transaction())
                {
                    try
                    {
                        try(PreparedStatement pst = transaction.prepareStatement(
                                "DELETE FROM minecity_world_perm_defaults WHERE world_id=? AND perm=?"
                        ))
                        {
                            pst.setInt(1, worldId);
                            pst.setString(2, flag.name());
                            pst.executeUpdate();
                        }

                        transaction.commit();
                        return;
                    }
                    catch(Exception e)
                    {
                        transaction.rollback();
                        throw e;
                    }
                }
            }
        }
        catch(SQLException e)
        {
            throw new DataSourceException(e);
        }

        throw new DataSourceException("Unsupported flag holder: "+holder);
    }

    @Slow
    @Override
    public void allowAll(@NotNull SimpleFlagHolder holder) throws DataSourceException
    {
        try
        {
            if(holder instanceof City)
            {
                City city = (City) holder;
                try(Connection transaction = connection.transaction())
                {
                    try
                    {
                        try(PreparedStatement pst = transaction.prepareStatement(
                                "DELETE FROM minecity_city_perm_defaults WHERE city_id=?"
                        ))
                        {
                            pst.setInt(1, city.getId());
                            pst.executeUpdate();
                        }

                        transaction.commit();
                        return;
                    }
                    catch(Exception e)
                    {
                        transaction.rollback();
                        throw e;
                    }
                }
            }
            else if(holder instanceof Plot)
            {
                int plotId = ((Plot) holder).id;
                try(Connection transaction = connection.transaction())
                {
                    try
                    {
                        try(PreparedStatement pst = transaction.prepareStatement(
                                "DELETE FROM minecity_plot_perm_defaults WHERE plot_id=?"
                        ))
                        {
                            pst.setInt(1, plotId);
                            pst.executeUpdate();
                        }

                        transaction.commit();
                        return;
                    }
                    catch(Exception e)
                    {
                        transaction.rollback();
                        throw e;
                    }
                }
            }
            else if(holder instanceof Nature)
            {
                int worldId = ((Nature) holder).world.getDataSourceId();
                try(Connection transaction = connection.transaction())
                {
                    try
                    {
                        try(PreparedStatement pst = transaction.prepareStatement(
                                "DELETE FROM minecity_world_perm_defaults WHERE world_id=?"
                        ))
                        {
                            pst.setInt(1, worldId);
                            pst.executeUpdate();
                        }

                        transaction.commit();
                        return;
                    }
                    catch(Exception e)
                    {
                        transaction.rollback();
                        throw e;
                    }
                }
            }
        }
        catch(SQLException e)
        {
            throw new DataSourceException(e);
        }

        throw new DataSourceException("Unsupported flag holder: "+holder);
    }

    private void deleteCityPlayerPerm(Connection transaction, int cityId, int playerId, PermissionFlag flag)
            throws SQLException
    {
        try(PreparedStatement pst = transaction.prepareStatement(
                "DELETE FROM minecity_city_perm_player WHERE city_id=? AND perm=? AND player_id=?"
        ))
        {
            pst.setInt(1, cityId);
            pst.setString(2, flag.name());
            pst.setInt(3, playerId);
            pst.executeUpdate();
        }
    }

    private void deletePlotPlayerPerm(Connection transaction, int plotId, int playerId, PermissionFlag flag)
            throws SQLException
    {
        try(PreparedStatement pst = transaction.prepareStatement(
                "DELETE FROM minecity_plot_perm_player WHERE plot_id=? AND perm=? AND player_id=?"
        ))
        {
            pst.setInt(1, plotId);
            pst.setString(2, flag.name());
            pst.setInt(3, playerId);
            pst.executeUpdate();
        }
    }

    private void deleteCityEntityPerm(Connection transaction, int cityId, int entityId, PermissionFlag flag)
            throws SQLException
    {
        try(PreparedStatement pst = transaction.prepareStatement(
                "DELETE FROM minecity_city_perm_entity WHERE city_id=? AND perm=? AND entity_id=?"
        ))
        {
            pst.setInt(1, cityId);
            pst.setString(2, flag.name());
            pst.setInt(3, entityId);
            pst.executeUpdate();
        }
    }

    private void deletePlotEntityPerm(Connection transaction, int plotId, int entityId, PermissionFlag flag)
            throws SQLException
    {
        try(PreparedStatement pst = transaction.prepareStatement(
                "DELETE FROM minecity_plot_perm_entity WHERE plot_id=? AND perm=? AND entity_id=?"
        ))
        {
            pst.setInt(1, plotId);
            pst.setString(2, flag.name());
            pst.setInt(3, entityId);
            pst.executeUpdate();
        }
    }

    private void deleteCityGroupPerm(Connection transaction, int cityId, int groupId, PermissionFlag flag)
            throws SQLException
    {
        try(PreparedStatement pst = transaction.prepareStatement(
                "DELETE FROM minecity_city_perm_group WHERE city_id=? AND perm=? AND group_id=?"
        ))
        {
            pst.setInt(1, cityId);
            pst.setString(2, flag.name());
            pst.setInt(3, groupId);
            pst.executeUpdate();
        }
    }

    private void deletePlotGroupPerm(Connection transaction, int plotId, int groupId, PermissionFlag flag)
            throws SQLException
    {
        try(PreparedStatement pst = transaction.prepareStatement(
                "DELETE FROM minecity_plot_perm_group WHERE plot_id=? AND perm=? AND group_id=?"
        ))
        {
            pst.setInt(1, plotId);
            pst.setString(2, flag.name());
            pst.setInt(3, groupId);
            pst.executeUpdate();
        }
    }

    @Slow
    @Override
    public void set(@NotNull ExceptFlagHolder holder, @NotNull PermissionFlag flag, boolean allow,
                    @NotNull Identity<?> identity, @Nullable Message message) throws DataSourceException
    {
        try
        {
            if(holder instanceof City)
            {
                int cityId = ((City) holder).getId();
                try(Connection transaction = connection.transaction())
                {
                    try
                    {
                        switch(identity.getType())
                        {
                            case PLAYER:
                                int id = source.playerId(transaction, (PlayerID) identity);
                                deleteCityPlayerPerm(transaction, cityId, id, flag);

                                try(PreparedStatement pst = transaction.prepareStatement(
                                        "INSERT INTO minecity_city_perm_player(city_id,perm,player_id,allow,message) VALUES(?,?,?,?,?)"
                                ))
                                {
                                    pst.setInt(1, cityId);
                                    pst.setString(2, flag.name());
                                    pst.setInt(3, id);
                                    pst.setBoolean(4, allow);
                                    source.setNullableString(pst, 5, allow || message == null || message.equals(holder.getDefaultMessage(flag))?
                                            null : message.toString()
                                    );
                                    source.executeUpdate(pst, 1);
                                }
                                transaction.commit();
                                return;

                            case ENTITY:
                                id = source.entityId(transaction, (EntityID) identity);
                                deleteCityEntityPerm(transaction, cityId, id, flag);

                                try(PreparedStatement pst = transaction.prepareStatement(
                                        "INSERT INTO minecity_city_perm_entity(city_id,perm,entity_id,allow,message) VALUES(?,?,?,?,?)"
                                ))
                                {
                                    pst.setInt(1, cityId);
                                    pst.setString(2, flag.name());
                                    pst.setInt(3, id);
                                    pst.setBoolean(4, allow);
                                    source.setNullableString(pst, 5, allow || message == null || message.equals(holder.getDefaultMessage(flag))?
                                            null : message.toString()
                                    );
                                    source.executeUpdate(pst, 1);
                                }
                                transaction.commit();
                                return;

                            case GROUP:
                                id = identity.getDataSourceId();
                                deleteCityGroupPerm(transaction, cityId, id, flag);

                                try(PreparedStatement pst = transaction.prepareStatement(
                                        "INSERT INTO minecity_city_perm_group(city_id,perm,group_id,allow,message) VALUES(?,?,?,?,?)"
                                ))
                                {
                                    pst.setInt(1, cityId);
                                    pst.setString(2, flag.name());
                                    pst.setInt(3, id);
                                    pst.setBoolean(4, allow);
                                    source.setNullableString(pst, 5, allow || message == null || message.equals(holder.getDefaultMessage(flag))?
                                            null : message.toString()
                                    );
                                    source.executeUpdate(pst, 1);
                                }
                                transaction.commit();
                                return;

                            default:
                                throw new DataSourceException("Unsupported identity type: "+identity);
                        }
                    }
                    catch(Exception e)
                    {
                        transaction.rollback();
                        throw e;
                    }
                }
            }
            else if(holder instanceof Plot)
            {
                int plotId = ((Plot) holder).id;
                try(Connection transaction = connection.transaction())
                {
                    try
                    {
                        switch(identity.getType())
                        {
                            case PLAYER:
                                int id = source.playerId(transaction, (PlayerID) identity);
                                deletePlotPlayerPerm(transaction, plotId, id, flag);

                                try(PreparedStatement pst = transaction.prepareStatement(
                                        "INSERT INTO minecity_plot_perm_player(plot_id,perm,player_id,allow,message) VALUES(?,?,?,?,?)"
                                ))
                                {
                                    pst.setInt(1, plotId);
                                    pst.setString(2, flag.name());
                                    pst.setInt(3, id);
                                    pst.setBoolean(4, allow);
                                    source.setNullableString(pst, 5, allow || message == null || message.equals(holder.getDefaultMessage(flag))?
                                            null : message.toString()
                                    );
                                    source.executeUpdate(pst, 1);
                                }
                                transaction.commit();
                                return;

                            case ENTITY:
                                id = source.entityId(transaction, (EntityID) identity);
                                deletePlotEntityPerm(transaction, plotId, id, flag);

                                try(PreparedStatement pst = transaction.prepareStatement(
                                        "INSERT INTO minecity_plot_perm_entity(plot_id,perm,entity_id,allow,message) VALUES(?,?,?,?,?)"
                                ))
                                {
                                    pst.setInt(1, plotId);
                                    pst.setString(2, flag.name());
                                    pst.setInt(3, id);
                                    pst.setBoolean(4, allow);
                                    source.setNullableString(pst, 5, allow || message == null || message.equals(holder.getDefaultMessage(flag))?
                                            null : message.toString()
                                    );
                                    source.executeUpdate(pst, 1);
                                }
                                transaction.commit();
                                return;

                            case GROUP:
                                id = identity.getDataSourceId();
                                deletePlotGroupPerm(transaction, plotId, id, flag);

                                try(PreparedStatement pst = transaction.prepareStatement(
                                        "INSERT INTO minecity_plot_perm_group(plot_id,perm,group_id,allow,message) VALUES(?,?,?,?,?)"
                                ))
                                {
                                    pst.setInt(1, plotId);
                                    pst.setString(2, flag.name());
                                    pst.setInt(3, id);
                                    pst.setBoolean(4, allow);
                                    source.setNullableString(pst, 5, allow || message == null || message.equals(holder.getDefaultMessage(flag))?
                                            null : message.toString()
                                    );
                                    source.executeUpdate(pst, 1);
                                }
                                transaction.commit();
                                return;

                            default:
                                throw new DataSourceException("Unsupported identity type: "+identity);
                        }
                    }
                    catch(Exception e)
                    {
                        transaction.rollback();
                        throw e;
                    }
                }
            }
        }
        catch(SQLException e)
        {
            throw new DataSourceException(e);
        }

        throw new DataSourceException("Unsupported flag holder: "+holder);
    }

    @Slow
    @Override
    public void remove(@NotNull ExceptFlagHolder holder, @NotNull PermissionFlag flag, @NotNull Identity<?> identity)
            throws DataSourceException
    {
        try
        {
            if(holder instanceof City)
            {
                int cityId = ((City) holder).getId();
                try(Connection transaction = connection.transaction())
                {
                    try
                    {
                        switch(identity.getType())
                        {
                            case PLAYER:
                                int id = source.playerId(transaction, (PlayerID) identity);
                                deleteCityPlayerPerm(transaction, cityId, id, flag);
                                transaction.commit();
                                return;

                            case ENTITY:
                                id = source.entityId(transaction, (EntityID) identity);
                                deleteCityEntityPerm(transaction, cityId, id, flag);
                                transaction.commit();
                                return;

                            case GROUP:
                                id = identity.getDataSourceId();
                                deleteCityGroupPerm(transaction, cityId, id, flag);
                                transaction.commit();
                                return;

                            default:
                                throw new DataSourceException("Unsupported identity type: " + identity);
                        }
                    }
                    catch(Exception e)
                    {
                        transaction.rollback();
                        throw e;
                    }
                }
            }
            else if(holder instanceof Plot)
            {
                int plotId = ((Plot) holder).id;
                try(Connection transaction = connection.transaction())
                {
                    try
                    {
                        switch(identity.getType())
                        {
                            case PLAYER:
                                int id = source.playerId(transaction, (PlayerID) identity);
                                deletePlotPlayerPerm(transaction, plotId, id, flag);
                                transaction.commit();
                                return;

                            case ENTITY:
                                id = source.entityId(transaction, (EntityID) identity);
                                deletePlotEntityPerm(transaction, plotId, id, flag);
                                transaction.commit();
                                return;

                            case GROUP:
                                id = identity.getDataSourceId();
                                deletePlotGroupPerm(transaction, plotId, id, flag);
                                transaction.commit();
                                return;

                            default:
                                throw new DataSourceException("Unsupported identity type: " + identity);
                        }
                    }
                    catch(Exception e)
                    {
                        transaction.rollback();
                        throw e;
                    }
                }
            }
        }
        catch(SQLException e)
        {
            throw new DataSourceException(e);
        }

        throw new DataSourceException("Unsupported flag holder: "+holder);
    }

    @Slow
    @NotNull
    @Override
    public EnumMap<PermissionFlag, Message> loadSimplePermissions(@NotNull SimpleFlagHolder holder)
            throws DataSourceException
    {
        try
        {
            if(holder instanceof City)
            {
                int cityId = ((City) holder).getId();
                try(PreparedStatement pst = connection.connect().prepareStatement(
                        "SELECT perm, message FROM minecity_city_perm_defaults WHERE city_id=?"
                ))
                {
                    pst.setInt(1, cityId);
                    ResultSet result = pst.executeQuery();
                    EnumMap<PermissionFlag, Message> map = new EnumMap<>(PermissionFlag.class);
                    while(result.next())
                    {
                        PermissionFlag perm = PermissionFlag.valueOf(result.getString(1));
                        Message message;
                        String str = result.getString(2);
                        if(str == null)
                            message = holder.getDefaultMessage(perm);
                        else
                            message = Message.string(str);

                        map.put(perm, message);
                    }

                    return map;
                }
            }
            else if(holder instanceof Plot)
            {
                int plotId = ((Plot) holder).id;
                try(PreparedStatement pst = connection.connect().prepareStatement(
                        "SELECT perm, message FROM minecity_plot_perm_defaults WHERE plot_id=?"
                ))
                {
                    pst.setInt(1, plotId);
                    ResultSet result = pst.executeQuery();
                    EnumMap<PermissionFlag, Message> map = new EnumMap<>(PermissionFlag.class);
                    while(result.next())
                    {
                        PermissionFlag perm = PermissionFlag.valueOf(result.getString(1));
                        Message message;
                        String str = result.getString(2);
                        if(str == null)
                            message = holder.getDefaultMessage(perm);
                        else
                            message = Message.string(str);

                        map.put(perm, message);
                    }

                    return map;
                }
            }
            else if(holder instanceof Nature)
            {
                int worldId = ((Nature) holder).world.getDataSourceId();
                try(PreparedStatement pst = connection.connect().prepareStatement(
                        "SELECT perm, message FROM minecity_world_perm_defaults WHERE world_id=?"
                ))
                {
                    pst.setInt(1, worldId);
                    ResultSet result = pst.executeQuery();
                    EnumMap<PermissionFlag, Message> map = new EnumMap<>(PermissionFlag.class);
                    while(result.next())
                    {
                        PermissionFlag perm = PermissionFlag.valueOf(result.getString(1));
                        Message message;
                        String str = result.getString(2);
                        if(str == null)
                            message = holder.getDefaultMessage(perm);
                        else
                            message = Message.string(str);

                        map.put(perm, message);
                    }

                    return map;
                }
            }
        }
        catch(SQLException e)
        {
            throw new DataSourceException(e);
        }

        throw new DataSourceException("Unsupported flag holder: "+holder);
    }

    @Slow
    @NotNull
    @Override
    public Map<PermissionFlag, Map<Identity<?>, Optional<Message>>> loadExceptPermissions(@NotNull ExceptFlagHolder holder)
            throws DataSourceException
    {
        try
        {
            if(holder instanceof City)
            {
                int cityId = ((City) holder).getId();
                Map<PermissionFlag, Map<Identity<?>, Optional<Message>>> map = new HashMap<>();

                Connection connection = this.connection.connect();
                try(PreparedStatement pst = connection.prepareStatement(
                        "SELECT p.player_id, player_uuid, player_name, perm, allow, message " +
                        "FROM minecity_city_perm_player pp INNER JOIN minecity_players p ON p.player_id = pp.player_id " +
                        "WHERE city_id=?"
                ))
                {
                    pst.setInt(1, cityId);
                    ResultSet result = pst.executeQuery();
                    if(result.next())
                    {
                        Map<Integer, PlayerID> playerIds = new HashMap<>();
                        do
                        {
                            int playerId = result.getInt("player_id");
                            PlayerID player = playerIds.get(playerId);
                            if(player == null)
                                playerIds.put(playerId,
                                        player = new PlayerID(playerId,
                                                source.uuid(result.getBytes("player_uuid")),
                                                result.getString("player_name")
                                        )
                                );

                            insertIdentity(result, map, player, holder);
                        } while(result.next());
                    }
                }

                try(PreparedStatement pst = connection.prepareStatement(
                        "SELECT e.entity_id, entity_uuid, entity_name, entity_type, perm, allow, message " +
                        "FROM minecity_city_perm_entity pe INNER JOIN minecity_entities e ON e.entity_id = pe.entity_id " +
                        "WHERE city_id=?"
                ))
                {
                    pst.setInt(1, cityId);
                    ResultSet result = pst.executeQuery();
                    if(result.next())
                    {
                        Map<Integer, EntityID> entityIds = new HashMap<>();
                        do
                        {
                            int entityId = result.getInt("entity_id");
                            EntityID entity = entityIds.get(entityId);
                            if(entity == null)
                                entityIds.put(entityId,
                                        entity = new EntityID(entityId,
                                                MinecraftEntity.Type.valueOf(result.getString("entity_type")),
                                                source.uuid(result.getBytes("entity_uuid")),
                                                result.getString("entity_name")
                                        )
                                );

                            insertIdentity(result, map, entity, holder);
                        } while(result.next());
                    }
                }

                try(PreparedStatement pst = connection.prepareStatement(
                        "SELECT pg.group_id, g.name AS group_name, g.city_id AS home_id, c.name AS home_name, perm, allow, message " +
                        "FROM minecity_city_perm_group pg " +
                                "INNER JOIN minecity_groups g ON g.group_id = pg.group_id " +
                                "INNER JOIN minecity_city c ON c.city_id = g.city_id " +
                        "WHERE pg.city_id=?"
                ))
                {
                    pst.setInt(1, cityId);
                    ResultSet result = pst.executeQuery();
                    if(result.next())
                    {
                        Map<Integer, GroupID> groupIds = new HashMap<>();
                        do
                        {
                            int groupId = result.getInt("group_id");
                            GroupID group = groupIds.get(groupId);
                            if(group == null)
                            {
                                int homeId = result.getInt("home_id");
                                City city = source.cityMap.get(homeId);
                                if(city != null)
                                {
                                    for(Group g : city.getGroups())
                                    {
                                        if(g.id == groupId)
                                        {
                                            group = g.getIdentity();
                                            groupIds.put(groupId, group);
                                            break;
                                        }
                                    }
                                }

                                if(group == null)
                                    groupIds.put(groupId,
                                            group = new GroupID(groupId,
                                                    result.getString("group_name"),
                                                    result.getString("home_name"),
                                                    homeId
                                            )
                                    );
                            }

                            insertIdentity(result, map, group, holder);
                        } while(result.next());
                    }
                }

                return map;
            }
            else if(holder instanceof Plot)
            {
                int plotId = ((Plot) holder).id;
                Map<PermissionFlag, Map<Identity<?>, Optional<Message>>> map = new HashMap<>();

                Connection connection = this.connection.connect();
                try(PreparedStatement pst = connection.prepareStatement(
                        "SELECT p.player_id, player_uuid, player_name, perm, allow, message " +
                                "FROM minecity_plot_perm_player pp INNER JOIN minecity_players p ON p.player_id = pp.player_id " +
                                "WHERE plot_id=?"
                ))
                {
                    pst.setInt(1, plotId);
                    ResultSet result = pst.executeQuery();
                    if(result.next())
                    {
                        Map<Integer, PlayerID> playerIds = new HashMap<>();
                        do
                        {
                            int playerId = result.getInt("player_id");
                            PlayerID player = playerIds.get(playerId);
                            if(player == null)
                                playerIds.put(playerId,
                                        player = new PlayerID(playerId,
                                                source.uuid(result.getBytes("player_uuid")),
                                                result.getString("player_name")
                                        )
                                );

                            insertIdentity(result, map, player, holder);
                        } while(result.next());
                    }
                }

                try(PreparedStatement pst = connection.prepareStatement(
                        "SELECT e.entity_id, entity_uuid, entity_name, entity_type, perm, allow, message " +
                                "FROM minecity_plot_perm_entity pe INNER JOIN minecity_entities e ON e.entity_id = pe.entity_id " +
                                "WHERE plot_id=?"
                ))
                {
                    pst.setInt(1, plotId);
                    ResultSet result = pst.executeQuery();
                    if(result.next())
                    {
                        Map<Integer, EntityID> entityIds = new HashMap<>();
                        do
                        {
                            int entityId = result.getInt("entity_id");
                            EntityID entity = entityIds.get(entityId);
                            if(entity == null)
                                entityIds.put(entityId,
                                        entity = new EntityID(entityId,
                                                MinecraftEntity.Type.valueOf(result.getString("entity_type")),
                                                source.uuid(result.getBytes("entity_uuid")),
                                                result.getString("entity_name")
                                        )
                                );

                            insertIdentity(result, map, entity, holder);
                        } while(result.next());
                    }
                }

                try(PreparedStatement pst = connection.prepareStatement(
                        "SELECT pg.group_id, g.name AS group_name, g.city_id AS home_id, c.name AS home_name, perm, allow, message " +
                                "FROM minecity_plot_perm_group pg " +
                                "INNER JOIN minecity_groups g ON g.group_id = pg.group_id " +
                                "INNER JOIN minecity_city c ON c.city_id = g.city_id " +
                                "WHERE pg.plot_id=?"
                ))
                {
                    pst.setInt(1, plotId);
                    ResultSet result = pst.executeQuery();
                    if(result.next())
                    {
                        Map<Integer, GroupID> groupIds = new HashMap<>();
                        do
                        {
                            int groupId = result.getInt("group_id");
                            GroupID group = groupIds.get(groupId);
                            if(group == null)
                            {
                                int homeId = result.getInt("home_id");
                                City city = source.cityMap.get(homeId);
                                if(city != null)
                                {
                                    for(Group g : city.getGroups())
                                    {
                                        if(g.id == groupId)
                                        {
                                            group = g.getIdentity();
                                            groupIds.put(groupId, group);
                                            break;
                                        }
                                    }
                                }

                                if(group == null)
                                    groupIds.put(groupId,
                                            group = new GroupID(groupId,
                                                    result.getString("group_name"),
                                                    result.getString("home_name"),
                                                    homeId
                                            )
                                    );
                            }

                            insertIdentity(result, map, group, holder);
                        } while(result.next());
                    }
                }

                return map;
            }
        }
        catch(SQLException e)
        {
            throw new DataSourceException(e);
        }

        throw new DataSourceException("Unsupported flag holder: "+holder);
    }

    private void insertIdentity(ResultSet result, Map<PermissionFlag, Map<Identity<?>, Optional<Message>>> map,
                                Identity<?> identity, SimpleFlagHolder holder) throws SQLException
    {
        PermissionFlag flag = PermissionFlag.valueOf(result.getString("perm"));
        Message defaultMessage = holder.getDefaultMessage(flag);
        Map<Identity<?>, Optional<Message>> subMap = map.computeIfAbsent(flag, (f)-> new HashMap<>());

        if(result.getBoolean("allow"))
            subMap.put(identity, Optional.empty());
        else
        {
            String str = result.getString("message");
            Message message;
            if(str == null)
                message = defaultMessage;
            else
                message = Message.string(str);

            subMap.put(identity, Optional.of(message));
        }
    }

    @Override
    public void setCityCreationDenied(@NotNull Nature nature, boolean denied) throws DataSourceException
    {
        try(Connection connection = this.connection.connect())
        {
            try(PreparedStatement pst = connection.prepareStatement(
                "UPDATE minecity_world SET city_creations=? WHERE world_id=?"
            ))
            {
                pst.setBoolean(1, !denied);
                pst.setInt(2, nature.world.getDataSourceId());
                source.executeUpdate(pst, 1);
            }
        }
        catch(SQLException e)
        {
            throw new DataSourceException(e);
        }
    }

    @Override
    public void setName(@NotNull Nature nature, @NotNull String name) throws DataSourceException
    {
        try(Connection connection = this.connection.connect())
        {
            try(PreparedStatement pst = connection.prepareStatement(
                    "UPDATE minecity_world SET name=? WHERE world_id=?"
            ))
            {
                pst.setString(1, name);
                pst.setInt(2, nature.world.getDataSourceId());
                source.executeUpdate(pst, 1);
            }
        }
        catch(SQLException e)
        {
            throw new DataSourceException(e);
        }
    }
}
