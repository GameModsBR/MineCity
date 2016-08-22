package br.com.gamemods.minecity.api.shape;

import br.com.gamemods.minecity.api.CollectionUtil;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.api.world.EntityPos;
import br.com.gamemods.minecity.api.world.WorldDim;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Stream;

public interface Shape
{
    int sizeX();
    int sizeY();
    int sizeZ();
    int area();
    int squareSize();

    boolean contains(int x, int y, int z);

    boolean affects(@NotNull ChunkPos chunk);

    Iterator<int[]> blockIterator();

    @NotNull
    Type getType();

    @NotNull
    int[] serializationParams();

    @NotNull
    default Iterator<BlockPos> blockIterator(@NotNull final WorldDim world)
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

    default boolean contains(@NotNull Point pos)
    {
        return contains(pos.x, pos.y, pos.z);
    }

    default boolean contains(@NotNull EntityPos pos)
    {
        return contains((int)pos.x, (int)pos.y, (int)pos.z);
    }

    default byte[] serializeBytes()
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bos);
        try
        {
            byte[] bytes = getType().name().getBytes();
            out.write(bytes.length);
            out.write(bytes);
            int[] params = serializationParams();
            out.write(params.length);
            for(int param : params)
                out.writeInt(param);

            return bos.toByteArray();
        }
        catch(IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    static Shape deserializeBytes(byte[] serial)
    {
        try
        {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(serial));
            byte[] bytes = new byte[in.read()];
            if(in.read(bytes) != bytes.length)
                throw new IllegalArgumentException();

            Type type = Type.valueOf(new String(bytes));
            if(type == Type.EMPTY)
                return Empty.INSTANCE;

            int[] params = new int[in.read()];
            for(int i = 0; i < params.length; i++)
                params[i] = in.readInt();

            return deserialize(type, params);
        }
        catch(IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    default String serializeString()
    {
        StringBuilder sb = new StringBuilder(getType().name());
        for(int p: serializationParams())
            sb.append(',').append(p);
        return sb.toString();
    }

    static Shape deserializeString(String str)
    {
        int comma = str.indexOf(',');
        Type type = Type.valueOf(comma == -1? str : str.substring(0, comma));
        if(type == Type.EMPTY)
            return Empty.INSTANCE;

        String[] split = str.substring(comma + 1).split(",");
        int[] ints = new int[split.length];
        for(int i = 0; i < split.length; i++)
            ints[i] = Integer.parseInt(split[i]);

        return deserialize(type, ints);
    }

    static Shape deserialize(Type type, int[] params)
    {
        switch(type)
        {
            case CUBOID:
                return new Cuboid(params);
            case EMPTY:
                return Empty.INSTANCE;
            default:
                throw new UnsupportedOperationException("Unsupported Type: "+type);
        }
    }

    default boolean overlaps(Shape shape)
    {
        if(shape.getType() == Type.EMPTY)
            return false;

        int sizeA = area();
        int sizeB = shape.area();

        Shape smaller = sizeA <= sizeB? this : shape;
        Shape bigger  = sizeA <= sizeB? shape : this;

        return CollectionUtil.parallelStream(smaller.blockIterator())
                .filter(p-> bigger.contains(p[0], p[1], p[2]))
                .findAny().isPresent();
    }

    enum Type {
        EMPTY, CUBOID
    }
}
