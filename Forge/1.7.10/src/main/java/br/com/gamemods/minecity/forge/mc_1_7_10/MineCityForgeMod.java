package br.com.gamemods.minecity.forge.mc_1_7_10;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.command.CommandSender;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.IChunk;
import br.com.gamemods.minecity.forge.base.accessors.IWorldServer;
import br.com.gamemods.minecity.forge.base.command.IForgePlayer;
import br.com.gamemods.minecity.forge.mc_1_7_10.command.ForgeCommandSender;
import br.com.gamemods.minecity.forge.mc_1_7_10.command.ForgePlayer;
import br.com.gamemods.minecity.structure.ClaimedChunk;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.DimensionManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class MineCityForgeMod extends MineCityForge
{
    @Override
    public int dimension(World world)
    {
        return world.provider.dimensionId;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Stream<String> getOnlinePlayerNames()
    {
        return ((List<EntityPlayer>)server.getConfigurationManager().playerEntityList).stream()
                .map(EntityPlayer::getCommandSenderName);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Stream<PlayerID> getOnlinePlayers()
    {
        return ((List<EntityPlayer>)server.getConfigurationManager().playerEntityList).stream()
                .map(e-> this.player(e).getPlayerId());
    }

    @Override
    protected IForgePlayer createPlayer(EntityPlayerMP player)
    {
        return new ForgePlayer(this, player);
    }

    @Override
    protected CommandSender createSender(ICommandSender sender)
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

        Stream<IChunk> composite = overWorld.theChunkProviderServer.loadedChunks.stream();
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

    @SuppressWarnings("unchecked")
    @Override
    public Optional<PlayerID> getPlayerId(String name)
    {
        for(EntityPlayer player : (List<EntityPlayer>) server.getConfigurationManager().playerEntityList)
        {
            String playerName = player.getCommandSenderName();
            if(name.equals(playerName))
                return Optional.of(new PlayerID(player.getUniqueID(), playerName));
        }

        return Optional.empty();
    }
}
