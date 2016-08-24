package br.com.gamemods.minecity.protection;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.api.Server;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.FlagHolder;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.structure.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class MovementMonitor<Entity, S extends Server>
{
    public final S server;
    private final MineCity mineCity;
    private final MovementListener<Entity,S> listener;
    public final Entity entity;
    public ClaimedChunk lastClaim;
    public int lastX, lastY, lastZ;
    public City lastCity;
    public Plot lastPlot;
    public byte messageWait;

    public MovementMonitor(S server, Entity entity, BlockPos pos, MovementListener<Entity,S> listener)
    {
        this.server = server;
        this.mineCity = server.getMineCity();
        this.listener = listener;
        this.entity = entity;
        lastX = pos.x;
        lastY = pos.y;
        lastZ = pos.z;
        ChunkPos chunk = pos.getChunk();
        lastClaim = mineCity.provideChunk(chunk);
        lastCity = lastClaim.getCity().orElse(null);
        lastPlot = lastClaim.getPlotAt(lastX, lastY, lastZ).orElse(null);
    }

    @Contract(pure = true)
    @NotNull
    public final FlagHolder lastHolder()
    {
        return lastPlot != null? lastPlot :
                lastCity != null? lastCity :
                        lastClaim.nature().orElseGet(()-> mineCity.nature(lastClaim.chunk.world))
                ;
    }

    public Optional<Message> checkPosition(WorldDim worldDim, int posX, int posY, int posZ)
    {
        if(lastClaim.isInvalid())
        {
            lastClaim = mineCity.provideChunk(lastClaim.chunk);
            lastCity = lastClaim.getCity().orElse(null);
            lastPlot = lastClaim.getPlotAt(lastX, lastY, lastZ).orElse(null);
        }

        int chunkX = posX >> 4;
        int chunkZ = posZ >> 4;
        if(lastClaim.chunk.x != chunkX || lastClaim.chunk.z != chunkZ || !lastClaim.chunk.world.equals(worldDim))
        {
            ChunkPos chunk = new ChunkPos(worldDim, chunkX, chunkZ);
            ClaimedChunk claim = mineCity.getChunk(chunk).orElseGet(() -> Inconsistency.claim(chunk));
            City city = claim.getCity().orElse(null);
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
                Nature nature = mineCity.nature(chunk.world);
                denial = listener.onCityLeave(nature);
                if(denial.isPresent())
                    return denial;
            }
            else if(!lastClaim.chunk.world.equals(chunk.world))
            {
                Nature nature = mineCity.nature(chunk.world);
                denial = listener.onNatureChange(nature);
                if(denial.isPresent())
                    return denial;
            }

            lastCity = city;
            lastClaim = claim;
            lastPlot = plot;
        }
        else if(posX != lastX || posY != lastY || posZ != lastZ)
        {
            if(lastCity != null)
            {
                Plot plot = lastClaim.getPlotAt(posX, posY, posZ).orElse(null);

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
            if(listener.isSafeToStep(server, entity, worldDim, posX, posY - 1, posZ))
            {
                lastX = posX;
                lastY = posY;
                lastZ = posZ;
            }
        }

        return Optional.empty();
    }

    public BlockPos lastPosition()
    {
        return new BlockPos(lastClaim.chunk.world, lastX, lastY, lastZ);
    }
}
