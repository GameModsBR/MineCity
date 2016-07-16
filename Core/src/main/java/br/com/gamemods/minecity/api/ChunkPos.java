package br.com.gamemods.minecity.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.function.BiFunction;

/**
 * A chunk in a world
 */
public final class ChunkPos implements Serializable
{
    private static final long serialVersionUID = 2064335262570876482L;

    @NotNull
    public final WorldDim world;
    public final int x, z;

    public ChunkPos(@NotNull WorldDim world, int x, int z)
    {
        this.world = world;
        this.x = x;
        this.z = z;
    }

    @NotNull
    public ChunkPos add(int x, int z)
    {
        return new ChunkPos(world, this.x+x, this.z+z);
    }

    @NotNull
    public ChunkPos subtract(int x, int z)
    {
        return new ChunkPos(world, this.x-x, this.z-z);
    }

    @NotNull
    public ChunkPos multiply(int x, int z)
    {
        return new ChunkPos(world, this.x*x, this.z*z);
    }

    @NotNull
    public ChunkPos divide(int x, int z)
    {
        return new ChunkPos(world, this.x/x, this.z/z);
    }

    @NotNull
    public <T> ChunkPos apply(@Nullable T x, @Nullable T z, BiFunction<T, Integer, Integer> op)
    {
        return new ChunkPos(world, op.apply(x, this.x), op.apply(z, this.z));
    }

    /**
     * The minimum block position stored in this chunk
     */
    @NotNull
    public BlockPos getMinBlock()
    {
        return new BlockPos(world, x << 4, 1, z << 4);
    }

    /**
     * The maximum block position stored in this chunk
     */
    @NotNull
    public BlockPos getMaxBlock()
    {
        return new BlockPos(world, (x << 4)+15, 256, (z << 4)+15);
    }

    /**
     * The region position that stores this chunk
     */
    @NotNull
    public RegionPos getRegion()
    {
        return new RegionPos(world, x >> 5, z >> 5);
    }

    @Override
    public boolean equals(@Nullable Object o)
    {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        ChunkPos chunkPos = (ChunkPos) o;
        return x == chunkPos.x && z == chunkPos.z && world.equals(chunkPos.world);
    }

    @Override
    public int hashCode()
    {
        int result = world.hashCode();
        result = 31*result + x;
        result = 31*result + z;
        return result;
    }

    @NotNull
    @Override
    public String toString()
    {
        return "ChunkPos{" +
                "world=" + world +
                ", x=" + x +
                ", z=" + z +
                '}';
    }
}
