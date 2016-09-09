package br.com.gamemods.minecity.forge.base;

import br.com.gamemods.minecity.api.world.Direction;

import java.util.NoSuchElementException;

public class ForgeUtil
{
    public static Direction toDirection(int blockFace)
    {
        switch(blockFace)
        {
            case 0: return Direction.DOWN;
            case 1: return Direction.UP;
            case 2: return Direction.NORTH;
            case 3: return Direction.SOUTH;
            case 4: return Direction.WEST;
            case 5: return Direction.EAST;
            default: throw new NoSuchElementException(Integer.toString(blockFace));
        }
    }
}
