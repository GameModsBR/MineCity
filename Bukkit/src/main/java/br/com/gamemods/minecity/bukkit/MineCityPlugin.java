package br.com.gamemods.minecity.bukkit;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.MineCityConfig;
import br.com.gamemods.minecity.api.ChunkPos;
import br.com.gamemods.minecity.api.WorldDim;
import br.com.gamemods.minecity.datasource.api.DataSourceException;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.nio.file.Path;
import java.util.logging.Level;

public class MineCityPlugin extends JavaPlugin implements Listener
{
    private MineCity mineCity;
    private BukkitScheduler scheduler;

    @Override
    public void onEnable()
    {
        scheduler = getServer().getScheduler();
        MineCityConfig config = new MineCityConfig();

        mineCity = new MineCity(config);
        getServer().getPluginManager().registerEvents(this, this);
    }

    private WorldDim world(World world)
    {
        Path container = getServer().getWorldContainer().toPath();
        Path worldPath = container.relativize(world.getWorldFolder().toPath());
        return new WorldDim(world.getEnvironment().getId(), worldPath.toString());
    }

    private ChunkPos chunk(Chunk chunk)
    {
        return new ChunkPos(world(chunk.getWorld()), chunk.getX(), chunk.getX());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    void on(WorldLoadEvent event)
    {
        scheduler.runTaskAsynchronously(this,()-> mineCity.loadNature(world(event.getWorld())));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    void on(WorldUnloadEvent event)
    {
        scheduler.runTaskAsynchronously(this, ()-> mineCity.unloadNature(world(event.getWorld())));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    void on(ChunkLoadEvent event)
    {
        scheduler.runTaskAsynchronously(this, () -> {
            Chunk chunk = event.getChunk();
            try
            {
                mineCity.loadChunk(chunk(chunk));
            }
            catch(DataSourceException e)
            {
                getLogger().log(Level.SEVERE, "Failed to load the chunk: "+
                        event.getWorld().getName()+" "+chunk.getX()+"."+chunk.getZ(), e
                );
            }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    void on(ChunkUnloadEvent event)
    {
        scheduler.runTaskAsynchronously(this, ()-> mineCity.unloadChunk(chunk(event.getChunk())));
    }
}
