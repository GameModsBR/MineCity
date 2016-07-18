package br.com.gamemods.minecity.structure;

import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.api.world.WorldDim;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class IslandArea
{
    @NotNull
    public final WorldDim world;
    public final int x, z;
    @NotNull
    public final Island island;
    @NotNull
    public boolean[][] claims;

    public IslandArea(@NotNull Island island, int x, int z, @NotNull boolean[][] claims)
    {
        this.island = island;
        world = island.getWorld();
        this.x = x;
        this.z = z;
        this.claims = claims;
    }

    public IslandArea(@NotNull Island island, Collection<ChunkPos> claimedChunks)
    {
        this.island = island;
        world = island.getWorld();
        int minX = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        for(ChunkPos claimedChunk: claimedChunks)
        {
            minX = Math.min(minX, claimedChunk.x);
            minZ = Math.min(minZ, claimedChunk.z);
            maxX = Math.max(maxX, claimedChunk.x);
            maxZ = Math.max(maxZ, claimedChunk.z);
        }

        x = minX;
        z = minZ;
        claims = new boolean[maxX-minX+1][maxZ-minZ+1];
        claimedChunks.forEach(c-> claims[c.x-x][c.z-z] = true);
    }

    public Set<ChunkPos> touching(ChunkPos pos) throws IllegalArgumentException
    {
        if(!pos.world.equals(world)) throw new IllegalArgumentException("Different world");
        int rx = pos.x - x;
        int rz = pos.z - z;

        HashSet<ChunkPos> result = new HashSet<>(4);
        for(Direction direction: Direction.cardinal)
        {
            int cx = rx + direction.x;
            int cz = rz + direction.z;
            if(cx < 0 || cz < 0 || cx >= claims.length)
                continue;

            boolean[] az = claims[cx];
            if(az == null || cz >= az.length || !az[cz])
                continue;

            result.add(new ChunkPos(world, x+cx, z+cz));
        }

        return result;
    }

    public Set<ChunkPos> contiguous(ChunkPos pos) throws IllegalArgumentException
    {
        if(!pos.world.equals(world)) throw new IllegalArgumentException("Different world");
        int rx = pos.x - x;
        int rz = pos.z - z;
        Set<ChunkPos> result = new HashSet<>();
        if(isClaimed(pos))
            result.add(pos);

        contiguousLookup(rx, rz, new HashSet<>(Collections.singleton(pos)), result);
        return result;
    }

    private void contiguousLookup(int rx, int rz, Set<ChunkPos> scanned, Set<ChunkPos> claimed)
    {
        for(Direction direction: Direction.cardinal)
        {
            int cx = rx + direction.x;
            int cz = rz + direction.z;
            if(cx < 0 || cz < 0 || cx >= claims.length)
                continue;

            boolean[] az = claims[cx];
            if(az == null || cz >= az.length)
                continue;

            ChunkPos cp = new ChunkPos(world, x+cx, z+cz);
            if(!scanned.add(cp))
                continue;

            if(az[cz])
            {
                claimed.add(cp);
                contiguousLookup(cx, cz, scanned, claimed);
            }
        }
    }

    public Set<ChunkPos> claims()
    {
        Set<ChunkPos> result = new HashSet<>();
        for(int rx = 0; rx < claims.length; rx++)
        {
            boolean[] az = claims[rx];
            if(az != null)
                for(int rz = 0; rz < az.length; rz++)
                    if(az[rz])
                        result.add(new ChunkPos(world, x+rx, z+rz));
        }

        return result;
    }

    public boolean isClaimed(ChunkPos pos) throws IllegalArgumentException
    {
        if(!pos.world.equals(world)) throw new IllegalArgumentException("Different world");
        int rx = pos.x - x;
        int rz = pos.z - z;
        if(rx < 0 || rz < 0 || rx >= claims.length)
            return false;
        boolean[] az = claims[rx];
        return !(az == null || rz >= az.length) && az[rz];
    }

    public void setClaimed(ChunkPos pos, boolean val) throws IllegalArgumentException, IndexOutOfBoundsException
    {
        if(!pos.world.equals(world)) throw new IllegalArgumentException("Different world");
        claims[pos.x - x][pos.z - z] = val;
    }
}
