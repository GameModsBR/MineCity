package br.com.gamemods.minecity.forge.mc_1_7_10;

import br.com.gamemods.minecity.api.command.CommandSender;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.IChunk;
import br.com.gamemods.minecity.forge.base.accessors.ICommander;
import br.com.gamemods.minecity.forge.base.accessors.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.IWorldServer;
import br.com.gamemods.minecity.forge.base.command.ForgeCommandSender;
import br.com.gamemods.minecity.forge.mc_1_7_10.command.SevenPlayer;
import br.com.gamemods.minecity.structure.ClaimedChunk;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.DimensionManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

public class MineCitySeven extends MineCityForge
{
    @Override
    protected SevenPlayer createPlayer(IEntityPlayerMP player)
    {
        return new SevenPlayer(this, player);
    }

    @Override
    public SevenPlayer player(EntityPlayer player)
    {
        return (SevenPlayer) super.player(player);
    }

    @Override
    protected CommandSender createSender(ICommander sender)
    {
        return new ForgeCommandSender<>(this, sender);
    }

    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public Stream<ClaimedChunk> loadedChunks()
    {
        WorldServer overWorld = DimensionManager.getWorld(0);
        if(!(overWorld instanceof IWorldServer))
            return Stream.empty();

        List overWorldChunks = overWorld.theChunkProviderServer.loadedChunks;
        if(overWorldChunks.isEmpty())
            return Stream.empty();

        Stream<IChunk> composite = overWorldChunks.stream();
        for(WorldServer worldServer : DimensionManager.getWorlds())
        {
            if(worldServer == overWorld) continue;
            composite = Stream.concat(composite, worldServer.theChunkProviderServer.loadedChunks.stream());
        }

        return composite.map(IChunk::getMineCityClaim);
    }

    @Nullable
    public Chunk getLoadedChunk(WorldServer world, int x, int z)
    {
        return (Chunk) world.theChunkProviderServer.loadedChunkHashMap.getValueByKey(
                ChunkCoordIntPair.chunkXZ2Int(x, z)
        );
    }
}
