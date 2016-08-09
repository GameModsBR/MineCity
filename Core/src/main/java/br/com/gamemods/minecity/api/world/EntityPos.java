package br.com.gamemods.minecity.api.world;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

public final class EntityPos
{
    @NotNull
    public final WorldDim world;
    public final double x, y, z;
    public final float pitch, yaw;

    BlockPos block;

    public EntityPos(@NotNull WorldDim world, double x, double y, double z)
    {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        pitch = yaw = 0;
    }

    public EntityPos(@NotNull WorldDim world, double x, double y, double z, float pitch, float yaw)
    {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.yaw = yaw;
    }

    public EntityPos(BlockPos block, float pitch, float yaw)
    {
        this.world = block.world;
        this.x = block.x + 0.5;
        this.y = block.y + 0.5;
        this.z = block.z + 0.5;
        this.pitch = pitch;
        this.yaw = yaw;
        this.block = block;
    }

    public Direction getCardinalDirection()
    {
        double d = (double)((yaw + 180.0F)*8.0F/360.0F) + 0.5D;
        int i = (int) d;
        return Direction.cardinal8.get((d < (double)i ? i - 1 : i) & 7);
    }

    @NotNull
    public <T> EntityPos apply(@Nullable T x, @Nullable T y, @Nullable T z, @NotNull BiFunction<Double, T, Double> op)
    {
        return new EntityPos(world, op.apply(this.x, x), op.apply(this.y, y), op.apply(this.z, z));
    }

    @NotNull
    public EntityPos apply(@NotNull Direction direction, double multiplier, @NotNull BiFunction<Double, Double, Double> op)
    {
        return apply(direction.x*multiplier, direction.y*multiplier, direction.z*multiplier, op);
    }

    @NotNull
    public EntityPos applyI(@NotNull Direction direction, int multiplier, @NotNull BiFunction<Double, Double, Double> op)
    {
        return apply((double)direction.x*multiplier, (double)direction.y*multiplier, (double)direction.z*multiplier, op);
    }

    @NotNull
    public EntityPos add(@NotNull Direction direction, double multiplier)
    {
        return apply(direction, multiplier, (a,b)-> a+b);
    }

    @NotNull
    public EntityPos subtract(@NotNull Direction direction, double multiplier)
    {
        return apply(direction, multiplier, (a,b)-> a-b);
    }


    @NotNull
    public EntityPos add(@NotNull Direction direction, int multiplier)
    {
        return applyI(direction, multiplier, (a,b)-> a+b);
    }

    @NotNull
    public EntityPos subtract(@NotNull Direction direction, int multiplier)
    {
        return applyI(direction, multiplier, (a,b)-> a-b);
    }

    @NotNull
    public EntityPos add(@NotNull Direction direction)
    {
        return add(direction.x, direction.y, direction.z);
    }

    @NotNull
    public EntityPos subtract(@NotNull Direction direction)
    {
        return subtract(direction.x, direction.y, direction.z);
    }

    @NotNull
    public EntityPos add(double x, double y, double z)
    {
        return new EntityPos(world, this.x+x, this.y+y, this.z+z);
    }

    @NotNull
    public EntityPos subtract(double x, double y, double z)
    {
        return new EntityPos(world, this.x-x, this.y-y, this.z-z);
    }

    @NotNull
    public EntityPos multiply(double x, double y, double z)
    {
        return new EntityPos(world, this.x*x, this.y*y, this.z*z);
    }

    @NotNull
    public EntityPos divide(double x, double y, double z)
    {
        return new EntityPos(world, this.x/x, this.y/y, this.z/z);
    }

    public BlockPos getBlock()
    {
        if(block != null)
            return block;

        return block = new BlockPos(world, (int)x, (int)y, (int)z);
    }

    public ChunkPos getChunk()
    {
        return getBlock().getChunk();
    }

    public RegionPos getRegion()
    {
        return getBlock().getRegion();
    }



    @Override
    @SuppressWarnings("SimplifiableIfStatement")
    public boolean equals(Object o)
    {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        EntityPos entityPos = (EntityPos) o;

        if(Double.compare(entityPos.x, x) != 0) return false;
        if(Double.compare(entityPos.y, y) != 0) return false;
        if(Double.compare(entityPos.z, z) != 0) return false;
        if(Float.compare(entityPos.pitch, pitch) != 0) return false;
        if(Float.compare(entityPos.yaw, yaw) != 0) return false;
        return world.equals(entityPos.world);

    }

    @Override
    public int hashCode()
    {
        int result;
        long temp;
        result = world.hashCode();
        temp = Double.doubleToLongBits(x);
        result = 31*result + (int) (temp^(temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = 31*result + (int) (temp^(temp >>> 32));
        temp = Double.doubleToLongBits(z);
        result = 31*result + (int) (temp^(temp >>> 32));
        result = 31*result + (pitch != +0.0f? Float.floatToIntBits(pitch) : 0);
        result = 31*result + (yaw != +0.0f? Float.floatToIntBits(yaw) : 0);
        return result;
    }
}
