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
    private boolean closed;

    public SQLConnection(@NotNull String url, @Nullable String user, @Nullable byte[] passwd)
    {
        this.url = url;
        this.user = user;
        this.pass = passwd;
    }

    public synchronized Connection connect() throws SQLException
    {
        if(closed)
            throw new SQLException(new IllegalStateException("The SQL provider is closed"));

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

    public synchronized void disconnect() throws SQLException
    {
        try
        {
            if(connection != null)
                connection.close();
        }
        catch(SQLException e)
        {
            connection = null;
            throw e;
        }
    }

    public Connection transaction() throws SQLException
    {
        if(closed)
            throw new SQLException(new IllegalStateException("The SQL provider is closed"));

        Connection connection = DriverManager.getConnection(url, user, pass == null? null : new String(pass));
        connection.setAutoCommit(false);
        return connection;
    }

    public void close() throws SQLException
    {
        closed = true;
        disconnect();
    }
}
