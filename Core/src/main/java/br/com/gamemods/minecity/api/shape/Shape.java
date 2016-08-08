package br.com.gamemods.minecity.api.shape;

import br.com.gamemods.minecity.api.CollectionUtil;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.api.world.EntityPos;
import br.com.gamemods.minecity.api.world.WorldDim;

import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Stream;

public interface Shape
{
    int sizeX();
    int sizeY();
    int sizeZ();
    int size();

    boolean contains(int x, int y, int z);

    boolean affects(ChunkPos chunk);

    Iterator<int[]> blockIterator();

    default Iterator<BlockPos> blockIterator(final WorldDim world)
    {
        Objects.requireNonNull(world);
        return new Iterator<BlockPos>()
        {
            private final Iterator<int[]> parent = blockIterator();

            @Override
            public boolean hasNext()
            {
                return parent.hasNext();
            }

            @Override
            public BlockPos next()
            {
                int[] next = parent.next();
                return new BlockPos(world, next[0], next[1], next[2]);
            }
        };
    }

    default Stream<ChunkPos> chunks(WorldDim world)
    {
        return CollectionUtil.parallelStream(this.blockIterator())
                .map(pos-> new ChunkPos(world, pos[0]>>4, pos[2]>>4)).distinct()
                ;
    }

    default boolean contains(BlockPos pos)
    {
        return contains(pos.x, pos.y, pos.z);
    }

    default boolean contains(EntityPos pos)
    {
        return contains((int)pos.x, (int)pos.y, (int)pos.z);
    }
}
