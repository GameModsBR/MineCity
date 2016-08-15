package br.com.gamemods.minecity.bukkit.protection;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.FlagHolder;
import br.com.gamemods.minecity.api.permission.Identity;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.bukkit.MineCityBukkit;
import br.com.gamemods.minecity.bukkit.command.BukkitPlayer;
import br.com.gamemods.minecity.structure.ClaimedChunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.projectiles.BlockProjectileSource;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import static br.com.gamemods.minecity.api.CollectionUtil.optionalStream;
import static br.com.gamemods.minecity.api.permission.FlagHolder.can;
import static br.com.gamemods.minecity.api.permission.FlagHolder.wrapDeny;

public abstract class AbstractProtection implements Listener
{
    @NotNull
    protected final MineCityBukkit plugin;

    public AbstractProtection(@NotNull MineCityBukkit plugin)
    {
        this.plugin = plugin;
    }

    protected Object getShooter(ProjectileSource shooter, boolean checkClaim)
    {
        if(shooter instanceof Entity)
        {
            return shooter;
        }
        else if(shooter instanceof BlockProjectileSource)
        {
            Block block = ((BlockProjectileSource) shooter).getBlock();
            if(checkClaim)
            {
                Location loc = block.getLocation();
                ChunkPos chunkPos = plugin.chunk(loc);
                ClaimedChunk chunk = plugin.mineCity.provideChunk(chunkPos);
                PlayerID owner = chunk.getFlagHolder(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()).owner();

                if(owner == null)
                    return block;

                return owner;
            }
            return block;
        }

        return shooter;
    }

    protected boolean check(@NotNull Location location, @NotNull Player player, @NotNull PermissionFlag... flags)
    {
        BukkitPlayer user = plugin.player(player);
        Optional<Message> denial = silentCheck(location, user, flags);

        if(denial.isPresent())
        {
            user.send(wrapDeny(denial.get()));
            return true;
        }

        return false;
    }

    protected Optional<Message> silentCheck(@NotNull Location location, @NotNull Player player, @NotNull PermissionFlag... flags)
    {
        return silentCheck(location, plugin.player(player), flags);
    }

    protected Optional<Message> silentCheck(@NotNull Location location, @NotNull BukkitPlayer user, @NotNull PermissionFlag... flags)
    {
        BlockPos blockPos = plugin.blockPos(location);
        ClaimedChunk chunk = plugin.mineCity.provideChunk(blockPos.getChunk());
        FlagHolder holder = chunk.getFlagHolder(blockPos);

        Optional<Message> denial;
        if(flags.length == 1)
            return holder.can(user, flags[0]);
        else
        {
            //noinspection unchecked
            Supplier<Optional<Message>>[] array = Arrays.stream(flags).map(flag -> can(user, flag, holder)).toArray(Supplier[]::new);
            return optionalStream(array).findFirst();
        }
    }

    protected Optional<Message> check(@NotNull Location location, @NotNull Identity<?> identity, @NotNull PermissionFlag... flags)
    {
        BlockPos blockPos = plugin.blockPos(location);
        ClaimedChunk chunk = plugin.mineCity.provideChunk(blockPos.getChunk());
        FlagHolder holder = chunk.getFlagHolder(blockPos);

        if(flags.length == 1)
            return holder.can(identity, flags[0]);
        else
        {
            //noinspection unchecked
            Supplier<Optional<Message>>[] array = Arrays.stream(flags).map(flag -> can(identity, flag, holder)).toArray(Supplier[]::new);
            return optionalStream(array).findFirst();
        }
    }

    @Nullable
    protected  <E extends Entity> E getNearest(@NotNull Set<E> set, @NotNull Location loc)
    {
        double best = Double.MAX_VALUE;
        E nearest = null;
        for(E entity : set)
        {
            double dist = loc.distanceSquared(entity.getLocation());
            if(dist < best)
            {
                best = dist;
                nearest = entity;
            }
        }

        return nearest;
    }

}
