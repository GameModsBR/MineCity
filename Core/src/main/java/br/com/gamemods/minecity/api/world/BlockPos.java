package br.com.gamemods.minecity.api.world;

import br.com.gamemods.minecity.api.shape.Point;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.function.BiFunction;

/**
 * A block in a world
 */
public final class BlockPos extends Point implements Serializable
{
    private static final long serialVersionUID = 3427762977942769143L;

    @NotNull
    public final WorldDim world;

    private ChunkPos chunk;

    public BlockPos(@NotNull WorldDim world, int x, int y, int z)
    {
        super(x, y, z);
        this.world = world;
    }

    private BlockPos(@NotNull BlockPos old, int x, int y, int z)
    {
        this(old.world, x, y, z);
        if(x>>4 == old.x>>4 && z>>4 == old.z>>4)
            chunk = old.chunk;
    }

    @NotNull
    @Override
    public <T> BlockPos apply(@Nullable T x, @Nullable T y, @Nullable T z, @NotNull BiFunction<Integer, T, Integer> op)
    {
        return new BlockPos(this, op.apply(this.x, x), op.apply(this.y, y), op.apply(this.z, z));
    }

    @NotNull
    @Override
    public BlockPos apply(@NotNull Direction direction, double multiplier, @NotNull BiFunction<Integer, Double, Integer> op)
    {
        return apply(direction.x*multiplier, direction.y*multiplier, direction.z*multiplier, op);
    }

    @NotNull
    @Override
    public BlockPos applyI(@NotNull Direction direction, int multiplier, @NotNull BiFunction<Integer, Integer, Integer> op)
    {
        return apply(direction.x*multiplier, direction.y*multiplier, direction.z*multiplier, op);
    }

    @NotNull
    @Override
    public BlockPos add(@NotNull Direction direction, double multiplier)
    {
        return apply(direction, multiplier, (a,b)-> (int)(a+b));
    }

    @NotNull
    @Override
    public BlockPos subtract(@NotNull Direction direction, double multiplier)
    {
        return apply(direction, multiplier, (a,b)-> (int)(a-b));
    }


    @NotNull
    @Override
    public BlockPos add(@NotNull Direction direction, int multiplier)
    {
        return applyI(direction, multiplier, (a,b)-> a+b);
    }

    @NotNull
    @Override
    public BlockPos subtract(@NotNull Direction direction, int multiplier)
    {
        return applyI(direction, multiplier, (a,b)-> a-b);
    }

    @NotNull
    @Override
    public BlockPos add(@NotNull Direction direction)
    {
        return add(direction.x, direction.y, direction.z);
    }

    @NotNull
    @Override
    public BlockPos subtract(@NotNull Direction direction)
    {
        return subtract(direction.x, direction.y, direction.z);
    }

    @NotNull
    @Override
    public BlockPos add(int x, int y, int z)
    {
        return new BlockPos(this, this.x+x, this.y+y, this.z+z);
    }

    @NotNull
    @Override
    public BlockPos subtract(int x, int y, int z)
    {
        return new BlockPos(this, this.x-x, this.y-y, this.z-z);
    }

    @NotNull
    @Override
    public BlockPos multiply(int x, int y, int z)
    {
        return new BlockPos(this, this.x*x, this.y*y, this.z*z);
    }

    @NotNull
    @Override
    public BlockPos divide(int x, int y, int z)
    {
        return new BlockPos(this, this.x/x, this.y/y, this.z/z);
    }

    public EntityPos toEntity(float pitch, float yaw)
    {
        EntityPos pos = new EntityPos(this, pitch, yaw);
        pos.block = this;
        return pos;
    }

    public EntityPos toEntity()
    {
        return toEntity(0,0);
    }

    /**
     * The chunk position that stores this block
     */
    @NotNull
    public ChunkPos getChunk()
    {
        if(chunk == null)
            chunk = new ChunkPos(world, x >> 4, z >> 4);
        return chunk;
    }

    public void setChunk(@NotNull ChunkPos chunk) throws IllegalArgumentException
    {
        if(!world.equals(chunk.world) || x >> 4 != chunk.x || z >> 4 != chunk.z)
            throw new IllegalArgumentException("The chunk "+chunk+" does not stores the block "+this);
        
        this.chunk = chunk;
    }

    /**
     * The region position that stores this block
     */
    @NotNull
    public RegionPos getRegion()
    {
        return new RegionPos(world, x >> 9, z >> 9);
    }

    @Override
    public boolean equals(Object o)
    {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        BlockPos blockPos = (BlockPos) o;
        return x == blockPos.x && y == blockPos.y && z == blockPos.z && world.equals(blockPos.world);
    }

    @Override
    public int hashCode()
    {
        int result = world.hashCode();
        result = 31*result + x;
        result = 31*result + y;
        result = 31*result + z;
        return result;
    }

    @Override
    public String toString()
    {
        return "BlockPos{" +
                "world=" + world +
                ", x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
}
