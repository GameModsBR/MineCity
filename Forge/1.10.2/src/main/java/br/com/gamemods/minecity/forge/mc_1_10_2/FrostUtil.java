package br.com.gamemods.minecity.forge.mc_1_10_2;

import br.com.gamemods.minecity.api.world.Direction;
import net.minecraft.util.EnumFacing;
import org.jetbrains.annotations.Nullable;

public class FrostUtil
{
    @Nullable
    public static EnumFacing toFace(Direction direction)
    {
        switch(direction)
        {
            case NORTH: return EnumFacing.NORTH;
            case SOUTH: return EnumFacing.SOUTH;
            case EAST: return EnumFacing.EAST;
            case WEST: return EnumFacing.WEST;
            case UP: return EnumFacing.UP;
            case DOWN: return EnumFacing.DOWN;
            default: return null;
        }
    }
}
