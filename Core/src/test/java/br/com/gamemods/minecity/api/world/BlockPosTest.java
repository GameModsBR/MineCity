package br.com.gamemods.minecity.api.world;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BlockPosTest
{
    private final WorldDim world = new WorldDim(0, "world");
    private final BlockPos a = new BlockPos(world, 10, 10, 10);
    private final BlockPos b = new BlockPos(world, 5598, 64, -59879);

    @Test
    public void testApply() throws Exception
    {
        //TODO Make a test here
    }

    @Test
    public void testAdd() throws Exception
    {
        assertEquals(new BlockPos(world, 10, 10, 11),   a.add(Direction.NORTH));
        assertEquals(new BlockPos(world, 10, 10, 9),    a.add(Direction.SOUTH));
        assertEquals(new BlockPos(world, 11, 10, 10),   a.add(Direction.EAST));
        assertEquals(new BlockPos(world, 9, 10, 10),    a.add(Direction.WEST));
        assertEquals(new BlockPos(world, 10, 10, 12),   a.add(Direction.NORTH, 2));
        assertEquals(new BlockPos(world, 10, 10, 12),   a.add(Direction.NORTH, 2.8));
        assertEquals(new BlockPos(world, 12, 10, 10),   a.add(Direction.EAST, 2));
        assertEquals(new BlockPos(world, 12, 10, 10),   a.add(Direction.EAST, 2.8));
        assertEquals(new BlockPos(world, 10, 11, 10),   a.add(Direction.UP));
        assertEquals(new BlockPos(world, 10, 9, 10),    a.add(Direction.DOWN));
        assertEquals(new BlockPos(world, 12, 18, 7),    a.add(2, 8, -3));
    }

    @Test
    public void testSubtract() throws Exception
    {
        assertEquals(new BlockPos(world, 10, 10, 9),   a.subtract(Direction.NORTH));
        assertEquals(new BlockPos(world, 10, 10, 11),  a.subtract(Direction.SOUTH));
        assertEquals(new BlockPos(world, 9, 10, 10),   a.subtract(Direction.EAST));
        assertEquals(new BlockPos(world, 11, 10, 10),  a.subtract(Direction.WEST));
        assertEquals(new BlockPos(world, 10, 10, 8),   a.subtract(Direction.NORTH, 2));
        assertEquals(new BlockPos(world, 10, 10, 7),   a.subtract(Direction.NORTH, 2.8));
        assertEquals(new BlockPos(world, 8, 10, 10),   a.subtract(Direction.EAST, 2));
        assertEquals(new BlockPos(world, 7, 10, 10),   a.subtract(Direction.EAST, 2.8));
        assertEquals(new BlockPos(world, 10, 9, 10),   a.subtract(Direction.UP));
        assertEquals(new BlockPos(world, 10, 11, 10),  a.subtract(Direction.DOWN));
        assertEquals(new BlockPos(world, 8, 2, 13),    a.subtract(2, 8, -3));
    }

    @Test
    public void testMultiply() throws Exception
    {
        assertEquals(new BlockPos(world, 20, 40, -30), a.multiply(2, 4, -3));
    }

    @Test
    public void testDivide() throws Exception
    {
        assertEquals(new BlockPos(world, 5, 2, -3),   a.divide(2, 4, -3));
    }

    @Test
    public void testGetChunk() throws Exception
    {
        assertEquals(new ChunkPos(world, 0, 0), a.getChunk());
        assertEquals(new ChunkPos(world, 349, -3743), b.getChunk());
    }

    @Test
    public void testGetRegion() throws Exception
    {
        assertEquals(new RegionPos(world, 0, 0), a.getRegion());
        assertEquals(new RegionPos(world, 10, -117), b.getRegion());
    }
}