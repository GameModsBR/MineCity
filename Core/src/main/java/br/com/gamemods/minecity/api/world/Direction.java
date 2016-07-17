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
    DOWN(0,-1,0, 4)
    ;
    public final int x, y, z;
    private final byte oppositeIndex;
    private Direction opposite;

    public static final List<Direction> cardinal = Arrays.asList(NORTH, EAST, SOUTH, WEST);
    public static final List<Direction> block = Arrays.asList(UP, DOWN, NORTH, EAST, SOUTH, WEST);

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
