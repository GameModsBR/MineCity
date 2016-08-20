package br.com.gamemods.minecity.bukkit.protection;

import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.FlagHolder;
import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.bukkit.MineCityBukkit;
import br.com.gamemods.minecity.structure.*;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class MovementMonitor
{
    public final MineCityBukkit plugin;
    public final LivingEntity entity;
    private final MovementListener listener;
    public ChunkPos lastChunk;
    public int lastX, lastY, lastZ;
    public City lastCity;
    public Plot lastPlot;
    public byte messageWait;

    public MovementMonitor(MineCityBukkit plugin, LivingEntity entity, MovementListener listener)
    {
        this.plugin = plugin;
        this.entity = entity;
        this.listener = listener;
        Location location = entity.getLocation();
        lastX = location.getBlockX();
        lastY = location.getBlockY();
        lastZ = location.getBlockZ();
        lastChunk = new ChunkPos(plugin.world(location.getWorld()), lastX >> 4, lastZ >> 4);
        Optional<ClaimedChunk> chunk = plugin.mineCity.getChunk(lastChunk);
        lastCity = chunk.flatMap(ClaimedChunk::getCity).orElse(null);
        lastPlot = chunk.flatMap(c-> c.getPlotAt(lastX, lastY, lastZ)).orElse(null);
    }

    @Contract(pure = true)
    @NotNull
    public final FlagHolder lastHolder()
    {
        return lastPlot != null? lastPlot :
                lastCity != null? lastCity :
                        lastChunk.world.nature != null? lastChunk.world.nature :
                                plugin.mineCity.nature(lastChunk.world)
                ;
    }

    public Optional<Message> checkPosition(Location location)
    {
        int posX = location.getBlockX();
        int posY = location.getBlockY();
        int posZ = location.getBlockZ();
        int chunkX = posX >> 4;
        int chunkZ = posZ >> 4;
        World worldObj = location.getWorld();
        WorldDim worldDim = plugin.world(worldObj);
        if(lastChunk.x != chunkX || lastChunk.z != chunkZ || !lastChunk.world.equals(worldDim))
        {
            ChunkPos chunk = new ChunkPos(worldDim, chunkX, chunkZ);
            ClaimedChunk claim = plugin.mineCity.getChunk(chunk).orElseGet(() -> Inconsistency.claim(chunk));
            City city = claim.reserve? null : claim.getCity().orElse(null);
            Plot plot = null;
            Optional<Message> denial;
            if(city != null)
            {
                plot = claim.getPlotAt(posX, posY, posZ).orElse(null);
                if(city != lastCity)
                {
                    denial = listener.onCityChange(city, plot);
                    if(denial.isPresent())
                        return denial;
                }
                else if(plot != lastPlot)
                {
                    if(plot != null)
                        denial = listener.onPlotEnter(plot);
                    else
                        denial = listener.onPlotLeave(city);

                    if(denial.isPresent())
                        return denial;
                }
            }
            else if(lastCity != null)
            {
                Nature nature = plugin.mineCity.nature(chunk.world);
                denial = listener.onCityLeave(nature);
                if(denial.isPresent())
                    return denial;
            }
            else if(!lastChunk.world.equals(chunk.world))
            {
                Nature nature = plugin.mineCity.nature(chunk.world);
                denial = listener.onNatureChange(nature);
                if(denial.isPresent())
                    return denial;
            }

            lastCity = city;
            lastChunk = chunk;
            lastPlot = plot;
        }
        else if(posX != lastX || posY != lastY || posZ != lastZ)
        {
            if(lastCity != null)
            {
                Plot plot = plugin.mineCity.getChunk(new ChunkPos(worldDim, chunkX, chunkZ))
                        .flatMap(chunk -> chunk.getPlotAt(posX, posY, posZ))
                        .orElse(null);

                if(plot != lastPlot)
                {
                    Optional<Message> denial;
                    if(plot == null)
                        denial = listener.onPlotLeave(lastCity);
                    else
                        denial = listener.onPlotEnter(plot);

                    if(denial.isPresent())
                        return denial;

                    lastPlot = plot;
                }
            }
        }

        if(messageWait > 0)
            messageWait--;
        else if(lastX != posX || lastZ != posZ || lastY < posY)
        {
            if(listener.isSafeToStep(entity, entity.getWorld().getBlockAt(posX, posY - 1, posZ)))
            {
                lastX = posX;
                lastY = posY;
                lastZ = posZ;
            }
        }

        return Optional.empty();
    }
}
