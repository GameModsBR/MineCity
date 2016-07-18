package br.com.gamemods.minecity.api.world;

import java.util.Arrays;
import java.util.List;

public enum Direction
{
    NORTH(0,0,1, 1),
    SOUTH(0,0,-1, 0),
    EAST(1,0,0, 3),
    WEST(-1,0,0, 2),
    UP(0,1,0, 5),
    DOWN(0,-1,0, 4),
    NORTH_EAST(1,0,1, 8),
    SOUTH_EAST(1,0,-1, 9),
    SOUTH_WEST(-1,0,-1, 6),
    NORTH_WEST(-1,0,1, 7),
    ;
    public final int x, y, z;
    private final byte oppositeIndex;
    private Direction opposite;

    public static final List<Direction> cardinal = Arrays.asList(NORTH, EAST, SOUTH, WEST);
    public static final List<Direction> block = Arrays.asList(UP, DOWN, NORTH, EAST, SOUTH, WEST);
    public static final List<Direction> intermediate = Arrays.asList(NORTH_EAST, SOUTH_EAST, SOUTH_WEST, NORTH_WEST);
    public static final List<Direction> cardinal8 = Arrays.asList(NORTH, NORTH_EAST, EAST, SOUTH_EAST, SOUTH, SOUTH_WEST, WEST, NORTH_WEST);

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
