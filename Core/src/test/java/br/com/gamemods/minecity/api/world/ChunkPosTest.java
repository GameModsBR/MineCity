package br.com.gamemods.minecity.api.world;

import org.junit.Test;

import static org.junit.Assert.*;

public class ChunkPosTest
{
    private final WorldDim world = new WorldDim(0, "world");
    private final ChunkPos a = new ChunkPos(world, 10, 10);

    @Test
    public void testAdd() throws Exception
    {
        assertEquals(new ChunkPos(world, 10, 11),   a.add(Direction.NORTH));
        assertEquals(new ChunkPos(world, 10, 9),    a.add(Direction.SOUTH));
        assertEquals(new ChunkPos(world, 11, 10),   a.add(Direction.EAST));
        assertEquals(new ChunkPos(world, 9, 10),    a.add(Direction.WEST));
        assertEquals(new ChunkPos(world, 10, 12),   a.add(Direction.NORTH, 2));
        assertEquals(new ChunkPos(world, 10, 12),   a.add(Direction.NORTH, 2.8));
        assertEquals(new ChunkPos(world, 12, 10),   a.add(Direction.EAST, 2));
        assertEquals(new ChunkPos(world, 12, 10),   a.add(Direction.EAST, 2.8));
        assertEquals(new ChunkPos(world, 12, 7),    a.add(2, -3));
    }

    @Test
    public void testSubtract() throws Exception
    {
        assertEquals(new ChunkPos(world, 10, 9),   a.subtract(Direction.NORTH));
        assertEquals(new ChunkPos(world, 10, 11),  a.subtract(Direction.SOUTH));
        assertEquals(new ChunkPos(world, 9, 10),   a.subtract(Direction.EAST));
        assertEquals(new ChunkPos(world, 11, 10),  a.subtract(Direction.WEST));
        assertEquals(new ChunkPos(world, 10, 8),   a.subtract(Direction.NORTH, 2));
        assertEquals(new ChunkPos(world, 10, 7),   a.subtract(Direction.NORTH, 2.8));
        assertEquals(new ChunkPos(world, 8, 10),   a.subtract(Direction.EAST, 2));
        assertEquals(new ChunkPos(world, 7, 10),   a.subtract(Direction.EAST, 2.8));
        assertEquals(new ChunkPos(world, 8, 13),   a.subtract(2, -3));
    }

    @Test
    public void testMultiply() throws Exception
    {
        assertEquals(new ChunkPos(world, 20, -30), a.multiply(2, -3));
    }

    @Test
    public void testDivide() throws Exception
    {
        assertEquals(new ChunkPos(world, 5, -3),   a.divide(2, -3));
    }

    @Test
    public void testApply() throws Exception
    {
        assertEquals(new ChunkPos(world, 10, 33), a.apply(Direction.NORTH, 2.8, (a,b)-> b > 0? (int) Math.round(a*b +5) : a));
        assertEquals(new ChunkPos(world, 10, 25), a.apply(Direction.NORTH, 2, (a,b)-> b > 0? (int) Math.round(a*b +5) : a));
        assertEquals(new ChunkPos(world, 12, 13), a.apply("1.55", "3", (a,b) -> Math.round(a+Float.valueOf(b))));
    }

    @Test
    public void testGetMinBlock() throws Exception
    {
        assertEquals(new BlockPos(world, 160, 0, 160), a.getMinBlock());
    }

    @Test
    public void testGetMaxBlock() throws Exception
    {
        assertEquals(new BlockPos(world, 175, 255, 175), a.getMaxBlock());
    }

    @Test
    public void testGetRegion() throws Exception
    {
        assertEquals(new RegionPos(world, 0, 0), a.getRegion());
        assertEquals(new RegionPos(world, 17, -5), new ChunkPos(world, 558, -148).getRegion());
    }
}