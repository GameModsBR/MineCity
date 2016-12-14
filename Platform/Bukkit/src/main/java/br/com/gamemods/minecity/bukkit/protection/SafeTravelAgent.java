package br.com.gamemods.minecity.bukkit.protection;

import br.com.gamemods.minecity.api.CollectionUtil;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.FlagHolder;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.shape.Cuboid;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.bukkit.MineCityBukkit;
import br.com.gamemods.minecity.bukkit.command.BukkitPlayer;
import br.com.gamemods.minecity.structure.ClaimedChunk;
import br.com.gamemods.minecity.structure.Nature;
import br.com.gamemods.minecity.structure.Plot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TravelAgent;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Stream;

import static br.com.gamemods.minecity.api.permission.FlagHolder.can;

public class SafeTravelAgent extends WrappedTravelAgent
{
    @NotNull
    private MineCityBukkit plugin;

    @NotNull
    private Entity traveler;

    @Nullable
    private BukkitPlayer player;

    public SafeTravelAgent(@NotNull MineCityBukkit plugin, @NotNull TravelAgent source, @NotNull Entity traveler)
    {
        super(source);
        this.plugin = plugin;
        this.traveler = traveler;
        if(traveler instanceof Player)
            player = plugin.player((Player) traveler);
    }

    @NotNull
    @Override
    public Location findOrCreate(@NotNull Location location)
    {
        Location portal = findPortal(location);
        if(portal != null)
            return portal;

        if(!attemptToCreate(location).isPresent())
        {
            portal = findPortal(location);
            if(portal != null)
                return portal;
        }

        Location safeSpot = location.clone();
        Optional<Message> denial = findSafeSpot(safeSpot);
        if(denial.isPresent())
        {
            if(player != null)
                player.send(FlagHolder.wrapDeny(denial.get()));
            return traveler.getLocation();
        }

        int original = getCreationRadius();
        try
        {
            setCreationRadius(0);
            try
            {
                if(!attemptToCreate(safeSpot).isPresent())
                {
                    Location found = findPortal(safeSpot);
                    if(found != null)
                        return found;
                }
            }
            catch(Exception e)
            {
                plugin.logger.log(Level.WARNING, "Exception while trying to create a portal in a safe spot", e);
            }

            return safeSpot;
        }
        finally
        {
            setCreationRadius(original);
        }
    }

    public boolean isRiskyToStep(@NotNull Material type)
    {
        switch(type)
        {
            case CACTUS:
            case MAGMA:
            case BEDROCK:
                return true;
            default:
                return !type.isSolid();
        }
    }

    public boolean isRiskyToStayInside(@NotNull Material type)
    {
        switch(type)
        {
            case FIRE:
            case TRIPWIRE:
            case TRIPWIRE_HOOK:
            case WOOD_PLATE:
            case STONE_PLATE:
            case IRON_PLATE:
            case GOLD_PLATE:
            case PORTAL:
            case ENDER_PORTAL:
            case STRUCTURE_VOID:
            case END_GATEWAY:
                return true;
            default:
                return !type.isTransparent();
        }
    }

    public boolean isRisky(@NotNull Material type)
    {
        switch(type)
        {
            case CACTUS:
            case LAVA:
            case STATIONARY_LAVA:
            case FIRE:
            case BEDROCK:
                return true;
            default:
                return false;
        }
    }

    public boolean isSafe(@NotNull Block block)
    {
        for(int iy = 0; iy <= 2; iy++)
            if(isRiskyToStayInside(block.getRelative(0, iy, 0).getType()))
                return false;

        for(int ix = -1; ix <= 1; ix++)
            for(int iz = -1; iz <= 1; iz++)
            {
                if(!isRiskyToStep(block.getRelative(ix, -1, iz).getType()))
                    return false;

                for(int iy = 0; iy <= 3; iy++)
                    if(isRisky(block.getRelative(ix, iy, iz).getType()))
                        return false;
            }

        return true;
    }

    @NotNull
    public Optional<Message> findSafeSpot(final @NotNull Location location)
    {
        Block block = location.getBlock();
        if(isSafe(block))
            return Optional.empty();

        AtomicReference<Message> result = new AtomicReference<>();
        int radius = getCreationRadius();
        Map<ChunkPos, ClaimedChunk> possibleChunks = null;
        BlockPos pos = null;
        if(player != null)
        {
            pos = plugin.blockPos(location);
            Cuboid area = new Cuboid(pos.subtract(radius, radius, radius), pos.add(radius, radius, radius));
            possibleChunks = area.chunks(pos.world).map(plugin.mineCity::provideChunk)
                    .filter(claim -> {
                        Collection<Plot> plots = claim.getPlots();
                        if(!plots.isEmpty())
                        {
                            Optional<Message> denial = plots.stream().map(plot -> plot.can(player, PermissionFlag.ENTER))
                                    .filter(Optional::isPresent).map(Optional::get).findAny();
                            if(denial.isPresent())
                            {
                                result.set(denial.get());
                                return false;
                            }

                            return true;
                        }

                        return !claim.getFlagHolder().can(player, PermissionFlag.ENTER).isPresent();
                    })
                    .collect(CollectionUtil.toMap(cc -> cc.chunk, Function.identity(), HashMap::new));

            if(possibleChunks.isEmpty())
                return Optional.of(result.get());
        }


        for(int ix = 0; ix < radius; ix++)
            for(int iz = 0; iz < radius; iz++)
                for(int iy = 0; iy < radius; iy++)

                    for(int nx = -1; nx <= 1; nx += 2)
                        for(int ny = -1; ny <= 1; ny += 2)
                            for(int nz = -1; nz <= 1; nz += 2)
                            {
                                Block relative = block.getRelative(ix*nx, iy*ny, iz*nz);
                                if(isSafe(relative))
                                {
                                    Location loc = relative.getLocation();
                                    if(pos != null)
                                    {
                                        BlockPos relPos = plugin.blockPos(pos, loc);
                                        Optional<Message> denial = possibleChunks.get(relPos.getChunk())
                                                .getFlagHolder(relPos).can(player, PermissionFlag.ENTER);

                                        if(denial.isPresent())
                                        {
                                            result.set(denial.get());
                                            continue;
                                        }
                                    }

                                    location.setX(loc.getBlockX()+0.5);
                                    location.setY(loc.getBlockY()+0.5);
                                    location.setZ(loc.getBlockZ()+0.5);

                                    return Optional.empty();
                                }
                            }

        return Optional.of(result.get());
    }

    @Override
    public boolean createPortal(@NotNull Location location)
    {
        Optional<Message> denial = attemptToCreate(location);
        if(!denial.isPresent())
            return true;

        if(player != null)
            player.send(FlagHolder.wrapDeny(denial.get()));

        return false;
    }

    @NotNull
    public Optional<Message> attemptToCreate(@NotNull Location location)
    {
        BukkitPlayer player = this.player;
        if(player == null)
            return Optional.of(new Message("For safety, only players can create portals"));

        int radius = getCreationRadius();
        BlockPos center = plugin.blockPos(location);
        ClaimedChunk centerClaim = plugin.mineCity.provideChunk(center.getChunk());
        Cuboid possibleArea = new Cuboid(center.subtract(radius-4, radius-1, radius-4), center.add(radius+4, radius+3, radius+4));
        Optional<Message> denial = possibleArea.chunks(center.world)
            .map(c-> plugin.mineCity.provideChunk(c, centerClaim))
            .flatMap(claim->
            {
                if(claim.reserve)
                    return Stream.of(plugin.mineCity.nature(claim.chunk.world));

                if(claim.owner instanceof Nature)
                    return Stream.of((Nature) claim.owner);

                Collection<Plot> plots = claim.getPlots();
                if(plots.isEmpty())
                    return Stream.of(claim.getFlagHolder());

                return Stream.concat(Stream.of(claim.getFlagHolder()), plots.stream());
            }).flatMap(flagHolder -> CollectionUtil.optionalStream(
                    can(player, PermissionFlag.ENTER, flagHolder),
                    can(player, PermissionFlag.MODIFY, flagHolder)
            )).findAny();

        if(denial.isPresent())
            return denial;

        if(super.createPortal(location))
            return Optional.empty();

        return Optional.of(new Message("action.portal.create.failed", "Unable to create a portal in the other dimension."));
    }
}
