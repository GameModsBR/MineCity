package br.com.gamemods.minecity.api.world;

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
    public Object instance;

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
    public <T> ChunkPos apply(@Nullable T x, @Nullable T z, @NotNull BiFunction<Integer, T, Integer> op)
    {
        return new ChunkPos(world, op.apply(this.x, x), op.apply(this.z, z));
    }

    public ChunkPos apply(@NotNull Direction direction, double multiplier, @NotNull BiFunction<Integer, Double, Integer> op)
    {
        return apply(direction.x*multiplier, direction.z*multiplier, op);
    }

    public ChunkPos applyI(@NotNull Direction direction, int multiplier, @NotNull BiFunction<Integer, Integer, Integer> op)
    {
        return apply(direction.x*multiplier, direction.z*multiplier, op);
    }

    public ChunkPos add(@NotNull Direction direction, double multiplier)
    {
        return apply(direction, multiplier, (a,b)-> (int)(a+b));
    }

    public ChunkPos subtract(@NotNull Direction direction, double multiplier)
    {
        return apply(direction, multiplier, (a,b)-> (int)(a-b));
    }

    public ChunkPos add(@NotNull Direction direction, int multiplier)
    {
        return applyI(direction, multiplier, (a,b)-> a+b);
    }

    public ChunkPos subtract(@NotNull Direction direction, int multiplier)
    {
        return applyI(direction, multiplier, (a,b)-> a-b);
    }

    public ChunkPos add(@NotNull Direction direction)
    {
        return add(direction.x, direction.z);
    }

    public ChunkPos subtract(@NotNull Direction direction)
    {
        return subtract(direction.x, direction.z);
    }

    /**
     * The minimum block position stored in this chunk
     */
    @NotNull
    public BlockPos getMinBlock()
    {
        return new BlockPos(world, x << 4, 0, z << 4);
    }

    /**
     * The maximum block position stored in this chunk
     */
    @NotNull
    public BlockPos getMaxBlock()
    {
        return new BlockPos(world, (x << 4)+15, 255, (z << 4)+15);
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

    public float distance(@NotNull ChunkPos pos)
    {
        int dx = x - pos.x;
        int dz = z - pos.z;
        return (float) Math.sqrt(dx*dx + dz*dz);
    }
}
