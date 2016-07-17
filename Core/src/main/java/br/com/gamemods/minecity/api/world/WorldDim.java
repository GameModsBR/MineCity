package br.com.gamemods.minecity.api.world;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

/**
 * A world in a dimension, a dimension can have multiple distinct worlds depending on the server implementation.
 */
public class WorldDim implements Serializable
{
    private static final long serialVersionUID = 4760576989452111571L;
    private int dataSourceId;

    /**
     * The path to the world folder, it must be relative to the world container
     */
    @NotNull
    public final String dir;

    /**
     * The dimension/environment ID used on this world
     */
    public final int dim;

    /**
     * The name displayed to the players
     */
    @Nullable
    public String name;

    public WorldDim(int dim, @NotNull String dir)
    {
        this.dim = dim;
        this.dir = dir;
    }

    public WorldDim(int dim, @NotNull String dir, @Nullable String name)
    {
        this.dim = dim;
        this.dir = dir;
        this.name = name;
    }

    public WorldDim(int dataSourceId, int dim, @NotNull String dir, @Nullable String name)
    {
        this.dim = dim;
        this.dir = dir;
        this.name = name;
        setDataSourceId(dataSourceId);
    }

    @NotNull
    public String name()
    {
        String name = this.name;
        if(name == null) return dir;
        return name;
    }

    public int getDataSourceId()
    {
        return dataSourceId;
    }

    public void setDataSourceId(int dataSourceId) throws IllegalStateException, IllegalArgumentException
    {
        if(dataSourceId < 0) throw new IllegalArgumentException();
        if(this.dataSourceId > 0 && this.dataSourceId != dataSourceId) throw new IllegalStateException();
        this.dataSourceId = dataSourceId;
    }

    @Override
    public String toString()
    {
        return "WorldDim{" +
                "dim=" + dim +
                ", dir='" + dir + '\'' +
                '}';
    }
}
