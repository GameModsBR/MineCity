package br.com.gamemods.minecity.api.world;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

public enum Direction
{
    NORTH(0,0,-1, 1),
    SOUTH(0,0,1, 0),
    EAST(1,0,0, 3),
    WEST(-1,0,0, 2),
    UP(0,1,0, 5),
    DOWN(0,-1,0, 4),
    NORTH_EAST(1,0,-1, 8),
    SOUTH_EAST(1,0,1, 9),
    SOUTH_WEST(-1,0,1, 6),
    NORTH_WEST(-1,0,-1, 7),

    @Deprecated
    NONE(0,0,0, 10),
    ;
    public final int x, y, z;
    private final byte oppositeIndex;
    private Direction opposite;

    public static final List<Direction> cardinal = Arrays.asList(NORTH, EAST, SOUTH, WEST);
    public static final List<Direction> block = Arrays.asList(UP, DOWN, NORTH, EAST, SOUTH, WEST);
    public static final List<Direction> intermediate = Arrays.asList(NORTH_EAST, SOUTH_EAST, SOUTH_WEST, NORTH_WEST);
    public static final List<Direction> cardinal8 = Arrays.asList(NORTH, NORTH_EAST, EAST, SOUTH_EAST, SOUTH, SOUTH_WEST, WEST, NORTH_WEST);

    public static Direction get(int x, int y, int z) throws NoSuchElementException
    {
        if(y < 0)
        {
            if(x != 0 || z != 0)
                throw new NoSuchElementException();
            return DOWN;
        }
        else if(y > 0)
        {
            if(x != 0 || z != 0)
                throw new NoSuchElementException();
            return UP;
        }
        if(z < 0)
        {
            if(x == 0)
                return NORTH;
            else if(x < 0)
                return NORTH_WEST;
            else
                return NORTH_EAST;
        }
        else if(z > 0)
        {
            if(x == 0)
                return SOUTH;
            else if(x < 0)
                return SOUTH_WEST;
            else
                return SOUTH_EAST;
        }
        else if(x < 0)
            return WEST;
        else
            return EAST;
    }

    public Direction left()
    {
        switch(this)
        {
            case NORTH: return EAST;
            case EAST: return SOUTH;
            case SOUTH: return WEST;
            case WEST: return NORTH;
            default: throw new NoSuchElementException(name());
        }
    }

    public Direction right()
    {
        switch(this)
        {
            case NORTH: return WEST;
            case WEST: return SOUTH;
            case SOUTH: return EAST;
            case EAST: return NORTH;
            default: throw new NoSuchElementException(name());
        }
    }

    Direction(int x, int y, int z, int oppositeIndex)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.oppositeIndex = (byte)oppositeIndex;
    }

    public Direction getOpposite()
    {
        return opposite != null? opposite : (opposite = values()[oppositeIndex]);
    }
}
