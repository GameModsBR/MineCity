package br.com.gamemods.minecity.forge.base.accessors.world;

import br.com.gamemods.minecity.api.shape.Point;
import br.com.gamemods.minecity.api.shape.PrecisePoint;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.accessors.IRayTraceResult;
import br.com.gamemods.minecity.forge.base.accessors.block.IBlock;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.world.WorldServerTransformer;
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

    default IBlock getIBlock(Point pos)
    {
        return getIBlock(pos.x, pos.y, pos.z);
    }

    boolean setBlock(int x, int y, int z, IState state);

    default boolean setBlock(BlockPos pos, IState state)
    {
        return setBlock(pos.x, pos.y, pos.z, state);
    }

    IRayTraceResult rayTraceBlocks(PrecisePoint start, PrecisePoint end, boolean stopOnLiquid);
}
