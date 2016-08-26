package br.com.gamemods.minecity.forge.base.accessors;

import br.com.gamemods.minecity.api.shape.Point;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.WorldServerTransformer;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

@Referenced(at = WorldServerTransformer.class)
public interface IWorldServer
{
    WorldDim getMineCityWorld();
    void setMineCityWorld(WorldDim world);
    default WorldServer getForgeWorld()
    {
        return (WorldServer) this;
    }

    default boolean isMineCityWorldServerValid()
    {
        return getMineCityWorld() != null;
    }

    IBlock getIBlock(int x, int y, int z);
    IState getIState(int x, int y, int z);
    boolean isSideSolid(int x, int y, int z, Direction direction);
    boolean isTopSolid(int x, int y, int z);

    @Nullable
    default String getFolder()
    {
        return ((WorldServer) this).provider.getSaveFolder();
    }

    default int getDimensionId()
    {
        return ((WorldServer) this).provider.getDimension();
    }

    default IChunk getLoadedChunk(int x, int z)
    {
        return (IChunk) ((WorldServer) this).getChunkProvider().getLoadedChunk(x, z);
    }

    @SuppressWarnings("unchecked")
    default Collection<IChunk> getLoadedIChunks()
    {
        return (Collection) getLoadedChunks();
    }

    default Collection<Chunk> getLoadedChunks()
    {
        return ((WorldServer) this).getChunkProvider().getLoadedChunks();
    }

    default IState getIState(Point pos)
    {
        return getIState(pos.x, pos.y, pos.z);
    }
}
