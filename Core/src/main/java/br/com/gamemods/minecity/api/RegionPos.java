package br.com.gamemods.minecity.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.BiFunction;

public final class RegionPos
{
    @NotNull
    private final WorldDim world;
    private final int x, z;

    public RegionPos(@NotNull WorldDim world, int x, int z)
    {
        this.world = world;
        this.x = x;
        this.z = z;
    }

    @NotNull
    public RegionPos add(int x, int z)
    {
        return new RegionPos(world, this.x+x, this.z+z);
    }

    @NotNull
    public RegionPos subtract(int x, int z)
    {
        return new RegionPos(world, this.x-x, this.z-z);
    }

    @NotNull
    public RegionPos multiply(int x, int z)
    {
        return new RegionPos(world, this.x*x, this.z*z);
    }

    @NotNull
    public RegionPos divide(int x, int z)
    {
        return new RegionPos(world, this.x/x, this.z/z);
    }

    @NotNull
    public <T> RegionPos apply(@Nullable T x, @Nullable T z, @NotNull BiFunction<T, Integer, Integer> op)
    {
        return new RegionPos(world, op.apply(x, this.x), op.apply(z, this.z));
    }

    /**
     * The minimum block position stored in this region file
     */
    @NotNull
    public BlockPos getMinBlock()
    {
        return new BlockPos(world, x<<9, 1, z<<9);
    }

    /**
     * The maximum block position stored in this region file
     */
    @NotNull
    public BlockPos getMaxBlock()
    {
        return new BlockPos(world, (x<<9)+511, 256, (z<<9)+511);
    }

    /**
     * The minimum chunk position stored in this region file
     */
    @NotNull
    public ChunkPos getMinChunk()
    {
        return new ChunkPos(world, x<<5, z<<5);
    }

    /**
     * The maximum chunk position stored in this region file
     */
    @NotNull
    public ChunkPos getMaxChunk()
    {
        return new ChunkPos(world, (x<<5)+31, (z<<5)+31);
    }

    @NotNull
    public String getFileName()
    {
        return "r."+x+"."+z+".mca";
    }

    /**
     * The path is relative to the server's world container
     */
    @NotNull
    public Path getFilePath()
    {
        return Paths.get(world.dir, "region", getFileName());
    }

    @Override
    public boolean equals(@Nullable Object o)
    {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        RegionPos regionPos = (RegionPos) o;

        return x == regionPos.x && z == regionPos.z && world.equals(regionPos.world);
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
        return "RegionPos{" +
                "world=" + world +
                ", x=" + x +
                ", z=" + z +
                '}';
    }
}
