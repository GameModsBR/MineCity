package br.com.gamemods.minecity.structure;

import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.datasource.api.ICityStorage;
import br.com.gamemods.minecity.datasource.api.IExceptPermissionStorage;
import br.com.gamemods.minecity.datasource.test.DummyIsland;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class IslandAreaTest
{
    private WorldDim world = new WorldDim(0, "world");
    private ICityStorage fakeStorage = Mockito.mock(ICityStorage.class);
    private IExceptPermissionStorage fakePermissionStorage = Mockito.mock(IExceptPermissionStorage.class);
    /**
     * <pre>
     *     X →
     *     1234567
     * Z 1|XXXX  X|
     * ↓ 2| XX   X|
     *   3|    X X|
     *   4| X   XX|
     *   5| X XXXX|
     * </pre>
     */
    @SuppressWarnings("SpellCheckingInspection")
    private IslandArea area = new IslandArea(new DummyIsland(fakeStorage, fakePermissionStorage, world, null), 0, 0, new boolean[][]{
            {},
            {false,true},
            {false,true,true,false,true,true},
            {false,true,true},
            {false,true,false,false,false,true},
            {false,false,false,true,false,true},
            {false,false,false,false,true,true},
            {false,true,true,true,true,true}
    });


    @Test
    public void testContiguous() throws Exception
    {
        HashSet<ChunkPos> expected = new HashSet<>();
        ChunkPos pos = new ChunkPos(world, 4, 5);
        area.setClaimed(pos, false);
        assertEquals(expected, area.contiguous(new ChunkPos(world, 4,4)));
        area.setClaimed(pos, true);

        pos = new ChunkPos(world, 5,3);
        expected.add(pos);
        assertEquals(expected, area.contiguous(pos));

        pos = new ChunkPos(world, 2,4);
        expected.clear();
        expected.add(pos);
        expected.add(new ChunkPos(world, 2,5));
        assertEquals(expected, area.contiguous(pos));

        pos = new ChunkPos(world, 3,2);
        expected.clear();
        int[][] correct = {{1,1},{2,1},{3,1},{4,1},{2,2},{3,2}};
        for(int[] val : correct) expected.add(new ChunkPos(world, val[0], val[1]));
        assertEquals(expected, area.contiguous(pos));
    }

    @Test
    public void testTouching() throws Exception
    {
        assertEquals(Collections.singleton(new ChunkPos(world, 5,3)), area.touching(new ChunkPos(world, 4,3)));
        assertEquals(new HashSet<>(Arrays.asList(new ChunkPos(world, 2,5), new ChunkPos(world, 4,5))), area.touching(new ChunkPos(world, 3,5)));
    }


    /*
     *     X →
     *     1234567
     * Z 1|XXXX  X|
     * ↓ 2| XX   X|
     *   3|    X X|
     *   4| X   XX|
     *   5| X XXXX|
     */

    @Test
    public void testClaims() throws Exception
    {
        HashSet<ChunkPos> claims = new HashSet<>();
        claims.add(new ChunkPos(world, 1,1));
        claims.add(new ChunkPos(world, 2,1));
        claims.add(new ChunkPos(world, 2,2));
        claims.add(new ChunkPos(world, 2,4));
        claims.add(new ChunkPos(world, 2,5));
        claims.add(new ChunkPos(world, 3,1));
        claims.add(new ChunkPos(world, 3,2));
        claims.add(new ChunkPos(world, 4,1));
        claims.add(new ChunkPos(world, 4,5));
        claims.add(new ChunkPos(world, 5,3));
        claims.add(new ChunkPos(world, 5,5));
        claims.add(new ChunkPos(world, 6,4));
        claims.add(new ChunkPos(world, 6,5));
        for(int i = 1; i <= 5; i++)
            claims.add(new ChunkPos(world, 7,i));
        assertEquals(claims, area.claims().collect(Collectors.toSet()));
    }

    @Test
    public void testReduction() throws Exception
    {
        IslandArea reduced = new IslandArea(area.island, area.claims().collect(Collectors.toSet()));
        assertEquals(1, reduced.x);
        assertEquals(1, reduced.z);
        assertEquals(7, reduced.claims.length);
        assertEquals(5, reduced.claims[0].length);
        assertTrue(reduced.isClaimed(new ChunkPos(world, 4,1)));
        assertTrue(reduced.isClaimed(new ChunkPos(world, 5,3)));
    }

    @Test
    public void testIsClaimed() throws Exception
    {
        assertTrue(area.isClaimed(new ChunkPos(world, 3,2)));
        assertTrue(area.isClaimed(new ChunkPos(world, 5,3)));
        assertTrue(area.isClaimed(new ChunkPos(world, 2,4)));
        assertTrue(area.isClaimed(new ChunkPos(world, 7,3)));

        assertFalse(area.isClaimed(new ChunkPos(world, 5,1)));

        assertFalse(area.isClaimed(new ChunkPos(world, 10,1)));
        assertFalse(area.isClaimed(new ChunkPos(world, -2,1)));
        assertFalse(area.isClaimed(new ChunkPos(world, 2,-2)));
        assertFalse(area.isClaimed(new ChunkPos(world, 2,20)));
    }

    @Test
    public void testSetClaimed() throws Exception
    {
        ChunkPos pos = new ChunkPos(world, 5, 1);
        assertFalse(area.isClaimed(pos));
        area.setClaimed(pos, true);
        assertTrue(area.isClaimed(pos));
        area.setClaimed(pos, false);
        assertFalse(area.isClaimed(pos));

        IslandArea reduced = new IslandArea(area.island, area.claims().collect(Collectors.toSet()));
        assertFalse(reduced.isClaimed(pos));
        reduced.setClaimed(pos, true);
        assertTrue(reduced.isClaimed(pos));
        reduced.setClaimed(pos, false);
        assertFalse(reduced.isClaimed(pos));
    }
}