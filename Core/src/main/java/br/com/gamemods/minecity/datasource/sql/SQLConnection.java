package br.com.gamemods.minecity.datasource.sql;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLConnection
{
    @NotNull
    private String url;
    @Nullable
    private String user;
    @Nullable
    private byte[] pass;
    private Connection connection;
    public int checkTimeout = 50;
    public int minCheckInterval = 5000;
    private long lastCheck;

    public SQLConnection(@NotNull String url, @Nullable String user, @Nullable byte[] passwd)
    {
        this.url = url;
        this.user = user;
        this.pass = passwd;
    }

    public synchronized Connection connect() throws SQLException
    {
        if(connection != null)
        {
            try
            {
                if(!connection.isClosed())
                {
                    if(System.currentTimeMillis() - lastCheck < minCheckInterval)
                        return connection;
                    else if(connection.isValid(checkTimeout))
                    {
                        lastCheck = System.currentTimeMillis();
                        return connection;
                    }
                }
            }
            catch(NullPointerException | SQLException ignored)
            {}
        }

        return connection = DriverManager.getConnection(url, user, pass == null? null : new String(pass));
    }

    public synchronized void disconnect()
    {
        try
        {
            if(connection != null)
                connection.close();
        }
        catch(SQLException e)
        {
            connection = null;
            System.err.println("[MineCity][SQL] Failed to close SQL connection");
            e.printStackTrace(System.err);
        }
    }

    public Connection transaction() throws SQLException
    {
        Connection connection = DriverManager.getConnection(url, user, pass == null? null : new String(pass));
        connection.setAutoCommit(false);
        return connection;
    }
}
