package br.com.gamemods.minecity.forge.mc_1_10_2.listeners;

import br.com.gamemods.minecity.api.Slow;
import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.datasource.api.DataSourceException;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.world.IChunk;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.structure.ClaimedChunk;
import br.com.gamemods.minecity.structure.Inconsistency;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class FrostWorldListener
{
    private MineCityForge forge;

    public FrostWorldListener(MineCityForge forge)
    {
        this.forge = forge;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onChunkLoad(ChunkEvent.Load event) throws DataSourceException
    {
        World world = event.getWorld();
        if(world.isRemote)
            return;

        Chunk chunk = event.getChunk();
        ChunkPos pos = new ChunkPos(forge.world(world), chunk.xPosition, chunk.zPosition);
        pos.instance = chunk;
        if(chunk instanceof IChunk)
            ((IChunk) chunk).setMineCityClaim(new ClaimedChunk(Inconsistency.INSTANCE, pos));

        forge.runAsynchronously(() ->
        {
            try
            {
                forge.mineCity.loadChunk(pos);
            }
            catch(Exception e)
            {
                forge.logger.error("Failed to load the chunk: "+pos, e);
            }
        });
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onChunkUnload(ChunkEvent.Unload event) throws DataSourceException
    {
        if(event.getWorld().isRemote)
            return;

        forge.mineCity.unloadChunk(forge.chunk(event.getChunk()));
    }

    @Slow
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onWorldLoad(WorldEvent.Load event) throws DataSourceException
    {
        World world = event.getWorld();
        if(world.isRemote)
            return;

        forge.mineCity.loadNature(forge.world(world));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onWorldUnload(WorldEvent.Unload event) throws DataSourceException
    {
        World world = event.getWorld();
        if(world.isRemote)
            return;

        forge.mineCity.unloadNature(forge.world(world));

        if(world instanceof IWorldServer)
            ((IWorldServer) world).setMineCityWorld(null);
    }
}
