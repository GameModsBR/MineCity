package br.com.gamemods.minecity.structure;

import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.api.world.WorldDim;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Stream;

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

    public Iterator<ClaimedChunk> iterator()
    {
        return new Iterator<ClaimedChunk>()
        {
            int rx;
            int rz;
            boolean[] az;
            {
                prepare();
            }

            void prepare()
            {
                for(;rx < claims.length; rx++)
                {
                    rz = 0;
                    az = claims[rx];
                    if(az != null && az.length > 0)
                        break;
                }
            }

            @Override
            public boolean hasNext()
            {
                return rx < claims.length || az != null && rz < az.length;
            }

            @Override
            public ClaimedChunk next()
            {
                try
                {
                    ChunkPos chunk = new ChunkPos(world, x + rx, z + rz);
                    ClaimedChunk result;
                    if(az[rz++])
                        result = new ClaimedChunk(island, chunk);
                    else
                        result = new ClaimedChunk(Inconsistency.INSTANCE, chunk);

                    prepare();
                    return result;
                }
                catch(NullPointerException | ArrayIndexOutOfBoundsException e)
                {
                    throw new NoSuchElementException();
                }
            }
        };
    }

    public Stream<ChunkPos> claims()
    {
        Stream.Builder<ChunkPos> builder = Stream.builder();
        for(int rx = 0; rx < claims.length; rx++)
        {
            boolean[] az = claims[rx];
            if(az != null)
                for(int rz = 0; rz < az.length; rz++)
                    if(az[rz])
                        builder.add(new ChunkPos(world, x+rx, z+rz));
        }
        return builder.build();
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

    public void setClaimed(int px, int pz, boolean val) throws IndexOutOfBoundsException
    {
        claims[px - x][pz - z] = val;
    }

    public void setClaimed(ChunkPos pos, boolean val) throws IllegalArgumentException, IndexOutOfBoundsException
    {
        if(!pos.world.equals(world)) throw new IllegalArgumentException("Different world");
        setClaimed(pos.x, pos.z, val);
    }
}
