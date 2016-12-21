package br.com.gamemods.minecity.forge.mc_1_10_2;

import br.com.gamemods.minecity.api.world.Direction;
import net.minecraft.util.EnumFacing;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.NoSuchElementException;

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

    @NotNull
    public static Direction toDirection(EnumFacing face)
    {
        if(face == null)
            return Direction.NONE;

        switch(face)
        {
            case NORTH: return Direction.NORTH;
            case SOUTH: return Direction.SOUTH;
            case EAST: return Direction.EAST;
            case WEST: return Direction.WEST;
            case UP: return Direction.UP;
            case DOWN: return Direction.DOWN;
            default: throw new NoSuchElementException(face.getName());
        }
    }
}
