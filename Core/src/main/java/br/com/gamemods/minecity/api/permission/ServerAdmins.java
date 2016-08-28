package br.com.gamemods.minecity.api.permission;

import java.io.Serializable;

public class ServerAdmins<T extends Serializable> extends OptionalPlayer<T>
{
    public static final ServerAdmins<?> INSTANCE = new ServerAdmins<>(Boolean.FALSE, "Server Admins");

    protected ServerAdmins(T id, String name)
    {
        super(id, name);
    }

    @Override
    public Type getType()
    {
        return Type.ADMINS;
    }

    @Override
    public int getDataSourceId()
    {
        return -1;
    }

    @Override
    public void setDataSourceId(int id) throws IllegalStateException, IllegalArgumentException
    {
        throw new IllegalStateException();
    }

    @Override
    public String toString()
    {
        return "ServerAdmins{}";
    }
}
