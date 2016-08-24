package br.com.gamemods.minecity.forge.mc_1_10_2;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.command.CommandSender;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.IChunk;
import br.com.gamemods.minecity.forge.base.accessors.IWorldServer;
import br.com.gamemods.minecity.forge.base.command.ForgeCommandSender;
import br.com.gamemods.minecity.forge.base.command.IForgePlayer;
import br.com.gamemods.minecity.forge.mc_1_10_2.command.FrostPlayer;
import br.com.gamemods.minecity.structure.ClaimedChunk;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.DimensionManager;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

public class MineCityFrost extends MineCityForge
{
    @Override
    public Optional<PlayerID> getPlayerId(String name)
    {
        for(EntityPlayer player : server.getPlayerList().getPlayerList())
        {
            String playerName = player.getName();
            if(name.equals(playerName))
                return Optional.of(new PlayerID(player.getUniqueID(), playerName));
        }

        return Optional.empty();
    }

    @Override
    public Stream<PlayerID> getOnlinePlayers()
    {
        return server.getPlayerList().getPlayerList().stream()
                .map(e-> this.player(e).getPlayerId());
    }

    @Override
    public Stream<String> getOnlinePlayerNames()
    {
        return server.getPlayerList().getPlayerList().stream()
                .map(EntityPlayer::getName);
    }

    @Override
    protected IForgePlayer createPlayer(EntityPlayerMP player)
    {
        return new FrostPlayer(this, player);
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

        Collection overWorldChunks = overWorld.getChunkProvider().getLoadedChunks();
        if(overWorldChunks.isEmpty())
            return Stream.empty();

        Stream<IChunk> composite = overWorldChunks.stream();
        for(WorldServer worldServer : DimensionManager.getWorlds())
        {
            if(worldServer == overWorld) continue;

            Collection loadedChunks = worldServer.getChunkProvider().getLoadedChunks();
            composite = Stream.concat(composite, loadedChunks.stream());
        }

        return composite.map(IChunk::getMineCityClaim);
    }

    @Override
    public Chunk getLoadedChunk(WorldServer world, int x, int z)
    {
        return world.getChunkProvider().getLoadedChunk(x, z);
    }

    @Override
    public boolean isTopSolid(World world, int x, int y, int z)
    {
        return world.isSideSolid(new BlockPos(x, y, z), EnumFacing.UP);
    }
}
