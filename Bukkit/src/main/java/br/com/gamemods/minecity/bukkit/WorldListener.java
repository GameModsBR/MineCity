package br.com.gamemods.minecity.bukkit;

import br.com.gamemods.minecity.api.Slow;
import br.com.gamemods.minecity.datasource.api.DataSourceException;
import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

import java.util.logging.Level;

public class WorldListener implements Listener
{
    public final MineCityBukkit bukkit;

    public WorldListener(MineCityBukkit bukkit)
    {
        this.bukkit = bukkit;
    }

    @Slow
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    void on(WorldLoadEvent event) throws DataSourceException
    {
        bukkit.mineCity.loadNature(bukkit.world(event.getWorld()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    void on(WorldUnloadEvent event)
    {
        bukkit.scheduler.runTaskAsynchronously(bukkit.plugin, ()-> bukkit.mineCity.unloadNature(bukkit.world(event.getWorld())));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    void on(ChunkLoadEvent event)
    {
        bukkit.scheduler.runTaskAsynchronously(bukkit.plugin, () -> {
            Chunk chunk = event.getChunk();
            try
            {
                bukkit.mineCity.loadChunk(bukkit.chunk(chunk));
            }
            catch(DataSourceException e)
            {
                bukkit.logger.log(Level.SEVERE, "Failed to load the chunk: "+
                        event.getWorld().getName()+" "+chunk.getX()+"."+chunk.getZ(), e
                );
            }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    void on(ChunkUnloadEvent event)
    {
        bukkit.scheduler.runTaskAsynchronously(bukkit.plugin, ()-> bukkit.mineCity.unloadChunk(bukkit.chunk(event.getChunk())));
    }
}
