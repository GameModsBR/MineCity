package br.com.gamemods.minecity.forge.mc_1_7_10.listeners;

import br.com.gamemods.minecity.api.Slow;
import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.datasource.api.DataSourceException;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.IChunk;
import br.com.gamemods.minecity.forge.base.accessors.IWorldServer;
import br.com.gamemods.minecity.structure.ClaimedChunk;
import br.com.gamemods.minecity.structure.Inconsistency;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;

public class WorldListener
{
    private MineCityForge forge;

    public WorldListener(MineCityForge forge)
    {
        this.forge = forge;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onChunkLoad(ChunkEvent.Load event) throws DataSourceException
    {
        if(event.world.isRemote)
            return;

        Chunk chunk = event.getChunk();
        ChunkPos pos = new ChunkPos(forge.world(chunk.worldObj), chunk.xPosition, chunk.zPosition);
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
        if(event.world.isRemote)
            return;

        forge.mineCity.unloadChunk(forge.chunk(event.getChunk()));
    }

    @Slow
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onWorldLoad(WorldEvent.Load event) throws DataSourceException
    {
        if(event.world.isRemote)
            return;

        forge.mineCity.loadNature(forge.world(event.world));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onWorldUnload(WorldEvent.Unload event) throws DataSourceException
    {
        if(event.world.isRemote)
            return;

        forge.mineCity.unloadNature(forge.world(event.world));

        if(event.world instanceof IWorldServer)
            ((IWorldServer) event.world).setMineCityWorld(null);
    }
}
