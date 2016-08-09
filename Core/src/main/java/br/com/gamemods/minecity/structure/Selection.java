package br.com.gamemods.minecity.structure;

import br.com.gamemods.minecity.api.shape.Cuboid;
import br.com.gamemods.minecity.api.shape.Shape;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.WorldDim;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Selection
{
    @NotNull
    public final WorldDim world;

    @Nullable
    public BlockPos a;

    @Nullable
    public BlockPos b;

    public Selection(@NotNull WorldDim world)
    {
        this.world = world;
    }

    public void normalize()
    {
        if(a == null || b == null || (a.x <= b.x && a.y <= b.y && a.z <= b.z))
            return;

        BlockPos min = a.apply(b.x, b.y, b.z, Math::min);
        BlockPos max = a.apply(b.x, b.y, b.z, Math::max);
        a = min;
        b = max;
    }

    public void select(BlockPos point)
    {
        if(a == null)
        {
            a = point;
            if(b != null)
                normalize();
        }
        else if(b == null)
        {
            b = point;
            normalize();
        }
        else
        {
            normalize();
            BlockPos min = a.apply(point.x, point.y, point.z, Math::min);
            BlockPos max = b.apply(point.x, point.y, point.z, Math::max);
            a = min;
            b = max;
        }
    }

    public Shape toShape()
    {
        if(isIncomplete())
            throw new IllegalStateException("The selection is not completed");

        return new Cuboid(a, b);
    }

    public boolean isIncomplete()
    {
        return a == null || b == null;
    }
}
