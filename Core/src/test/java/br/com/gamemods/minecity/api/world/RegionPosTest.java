package br.com.gamemods.minecity.api.world;

import org.junit.Test;

import java.nio.file.Paths;

import static org.junit.Assert.*;

public class RegionPosTest
{
    private final WorldDim world = new WorldDim(0, "world");
    private final RegionPos a = new RegionPos(world, 10, 10);
    private final RegionPos b = new RegionPos(world, 17, -5);

    @Test
    public void testAdd() throws Exception
    {
        assertEquals(new RegionPos(world, 10, 11),   a.add(Direction.NORTH));
        assertEquals(new RegionPos(world, 10, 9),    a.add(Direction.SOUTH));
        assertEquals(new RegionPos(world, 11, 10),   a.add(Direction.EAST));
        assertEquals(new RegionPos(world, 9, 10),    a.add(Direction.WEST));
        assertEquals(new RegionPos(world, 10, 12),   a.add(Direction.NORTH, 2));
        assertEquals(new RegionPos(world, 10, 12),   a.add(Direction.NORTH, 2.8));
        assertEquals(new RegionPos(world, 12, 10),   a.add(Direction.EAST, 2));
        assertEquals(new RegionPos(world, 12, 10),   a.add(Direction.EAST, 2.8));
        assertEquals(new RegionPos(world, 12, 7),    a.add(2, -3));
    }

    @Test
    public void testSubtract() throws Exception
    {
        assertEquals(new RegionPos(world, 10, 9),   a.subtract(Direction.NORTH));
        assertEquals(new RegionPos(world, 10, 11),  a.subtract(Direction.SOUTH));
        assertEquals(new RegionPos(world, 9, 10),   a.subtract(Direction.EAST));
        assertEquals(new RegionPos(world, 11, 10),  a.subtract(Direction.WEST));
        assertEquals(new RegionPos(world, 10, 8),   a.subtract(Direction.NORTH, 2));
        assertEquals(new RegionPos(world, 10, 7),   a.subtract(Direction.NORTH, 2.8));
        assertEquals(new RegionPos(world, 8, 10),   a.subtract(Direction.EAST, 2));
        assertEquals(new RegionPos(world, 7, 10),   a.subtract(Direction.EAST, 2.8));
        assertEquals(new RegionPos(world, 8, 13),   a.subtract(2, -3));
    }

    @Test
    public void testMultiply() throws Exception
    {
        assertEquals(new RegionPos(world, 20, -30), a.multiply(2, -3));
    }

    @Test
    public void testDivide() throws Exception
    {
        assertEquals(new RegionPos(world, 5, -3),   a.divide(2, -3));
    }

    @Test
    public void testApply() throws Exception
    {
        assertEquals(new RegionPos(world, 10, 33), a.apply(Direction.NORTH, 2.8, (a,b)-> b > 0? (int) Math.round(a*b +5) : a));
        assertEquals(new RegionPos(world, 10, 25), a.apply(Direction.NORTH, 2, (a,b)-> b > 0? (int) Math.round(a*b +5) : a));
        assertEquals(new RegionPos(world, 12, 13), a.apply("1.55", "3", (a,b) -> Math.round(a+Float.valueOf(b))));
    }

    @Test
    public void testGetMinBlock() throws Exception
    {
        assertEquals(new BlockPos(world, 5120, 0, 5120), a.getMinBlock());
        assertEquals(new BlockPos(world, 8704, 0, -2560), b.getMinBlock());
    }

    @Test
    public void testGetMaxBlock() throws Exception
    {
        assertEquals(new BlockPos(world, 5631, 255, 5631), a.getMaxBlock());
        assertEquals(new BlockPos(world, 9215, 255, -2049), b.getMaxBlock());
    }

    @Test
    public void testGetMinChunk() throws Exception
    {
        assertEquals(new ChunkPos(world, 320, 320), a.getMinChunk());
        assertEquals(new ChunkPos(world, 544, -160), b.getMinChunk());
    }

    @Test
    public void testGetMaxChunk() throws Exception
    {
        assertEquals(new ChunkPos(world, 351, 351), a.getMaxChunk());
        assertEquals(new ChunkPos(world, 575, -129), b.getMaxChunk());
    }

    @Test
    public void testGetFileName() throws Exception
    {
        assertEquals("r.10.10.mca", a.getFileName());
        assertEquals("r.17.-5.mca", b.getFileName());
    }

    @Test
    public void testGetFilePath() throws Exception
    {
        assertEquals(Paths.get("world", "region", "r.10.10.mca"), a.getFilePath());
        assertEquals(Paths.get("world", "region", "r.17.-5.mca"), b.getFilePath());
    }
}