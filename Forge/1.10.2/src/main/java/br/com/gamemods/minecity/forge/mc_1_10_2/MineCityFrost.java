package br.com.gamemods.minecity.forge.mc_1_10_2;

import br.com.gamemods.minecity.api.command.CommandSender;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.IChunk;
import br.com.gamemods.minecity.forge.base.accessors.ICommander;
import br.com.gamemods.minecity.forge.base.accessors.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.IWorldServer;
import br.com.gamemods.minecity.forge.base.command.ForgeCommandSender;
import br.com.gamemods.minecity.forge.mc_1_10_2.command.FrostPlayer;
import br.com.gamemods.minecity.structure.ClaimedChunk;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.DimensionManager;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.stream.Stream;

public class MineCityFrost extends MineCityForge
{
    @Override
    protected FrostPlayer createPlayer(IEntityPlayerMP player)
    {
        return new FrostPlayer(this, player);
    }

    @Override
    protected CommandSender createSender(ICommander sender)
    {
        return new ForgeCommandSender<>(this, sender);
    }

    @Override
    public FrostPlayer player(EntityPlayer player)
    {
        return (FrostPlayer) super.player(player);
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

    public br.com.gamemods.minecity.api.world.BlockPos block(World world, Vec3i pos)
    {
        return new br.com.gamemods.minecity.api.world.BlockPos(
                world(world), pos.getX(), pos.getY(), pos.getZ()
        );
    }

    public br.com.gamemods.minecity.api.world.BlockPos block(
                br.com.gamemods.minecity.api.world.BlockPos base, Vec3i pos)
    {
        return new br.com.gamemods.minecity.api.world.BlockPos(
                base, pos.getX(), pos.getY(), pos.getZ()
        );
    }
}
