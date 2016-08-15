package br.com.gamemods.minecity.bukkit.protection;

import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.FlagHolder;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.shape.Cuboid;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.bukkit.MineCityBukkit;
import br.com.gamemods.minecity.bukkit.command.BukkitPlayer;
import br.com.gamemods.minecity.structure.ClaimedChunk;
import br.com.gamemods.minecity.structure.Plot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.material.CocoaPlant;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

public class BlockProtections extends AbstractProtection
{
    public BlockProtections(@NotNull MineCityBukkit plugin)
    {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event)
    {
        Block block = event.getBlock();
        Location l = block.getLocation();
        Player player = event.getPlayer();

        BlockPos blockPos = plugin.blockPos(l);
        ClaimedChunk chunk = plugin.mineCity.provideChunk(blockPos.getChunk());
        FlagHolder holder = chunk.getFlagHolder(blockPos);

        BukkitPlayer user = plugin.player(player);
        Optional<Message> denial = holder.can(user, PermissionFlag.MODIFY);

        if(denial.isPresent())
        {
            event.setCancelled(true);
            user.send(FlagHolder.wrapDeny(denial.get()));
            return;
        }

        if(!block.getType().hasGravity())
            return;

        switch(block.getRelative(BlockFace.DOWN).getType())
        {
            // Gravity blocks on falls on placement if the block bellow is one of those:
            case AIR:
            case FIRE:
            case WATER:
            case STATIONARY_WATER:
            case LAVA:
            case STATIONARY_LAVA:
                break;
            default:
                if(checkFall(chunk, block, user, l, blockPos))
                    event.setCancelled(true);
        }
    }

    public boolean checkFall(ClaimedChunk claim, Block block, BukkitPlayer player, Location l, BlockPos blockPos)
    {
        Collection<Plot> plots = claim.getPlots();
        if(plots.isEmpty())
            return false;

        Optional<Plot> from = claim.getPlotAt(blockPos);

        BlockPos zero = new BlockPos(blockPos, block.getX(), 0, block.getZ());
        Cuboid fallArea = new Cuboid(blockPos, zero);
        Stream<Plot> stream = plots.stream();
        if(from.isPresent())
        {
            Plot fromPlot = from.get();
            stream = stream.filter(plot -> !plot.equals(fromPlot));
        }

        Plot[] risk = stream.filter(plot -> plot.getShape().overlaps(fallArea)).toArray(Plot[]::new);

        if(risk.length == 0)
            return false;

        while(true)
        {
            Block below = block.getRelative(BlockFace.DOWN);
            l.setY(l.getY()-1);
            switch(below.getType())
            {
                case AIR:
                case FIRE:
                case WATER:
                case STATIONARY_WATER:
                case LAVA:
                case STATIONARY_LAVA:
                    break;
                default:
                    return false;
            }

            Optional<Message> denial = Arrays.stream(risk).filter(
                    plot -> plot.getShape().contains(l.getBlockX(), l.getBlockY(), l.getBlockZ()))
                    .map(plot -> plot.can(player, PermissionFlag.MODIFY))
                    .filter(Optional::isPresent).map(Optional::get).findFirst();

            if(denial.isPresent())
            {
                player.send(FlagHolder.wrapDeny(denial.get()));
                return true;
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event)
    {
        Block block = event.getBlock();
        Location l = block.getLocation();
        BukkitPlayer player = plugin.player(event.getPlayer());

        BlockPos blockPos = plugin.blockPos(l);
        ClaimedChunk claim = plugin.mineCity.provideChunk(blockPos.getChunk());

        FlagHolder holder = claim.getFlagHolder(blockPos);
        Optional<Message> denial = holder.can(player, PermissionFlag.MODIFY);
        if(denial.isPresent())
        {
            Material type = block.getType();
            boolean harvest = false;
            switch(type)
            {
                case PUMPKIN:
                case MELON_BLOCK:
                {
                    for(BlockFace dir: new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST})
                    {
                        Block blockRelative = block.getRelative(dir);
                        Material relative = blockRelative.getType();
                        harvest = relative == Material.PUMPKIN_STEM && type == Material.PUMPKIN
                                || relative == Material.MELON_STEM && type == Material.MELON;

                        if(harvest)
                        {
                            Location loc = blockRelative.getLocation();
                            int x = loc.getBlockX();
                            int z = loc.getBlockZ();
                            if(claim.chunk.x == x>>4 && claim.chunk.z == z>>4 && claim.getFlagHolder(x, loc.getBlockY(), z) == holder)
                                break;
                            harvest = false;
                        }
                    }
                }
                break;

                case CACTUS:
                case SUGAR_CANE_BLOCK:
                    harvest = block.getRelative(BlockFace.DOWN).getType() == type;
                    break;

                case CROPS:
                case POTATO:
                case CARROT:
                    harvest = block.getData() == 7;
                    break;

                case BEETROOT_BLOCK:
                case NETHER_WART_BLOCK:
                    harvest = block.getData() == 3;
                    break;

                case COCOA:
                    harvest = ((CocoaPlant) block.getState().getData()).getSize() == CocoaPlant.CocoaPlantSize.LARGE;
                    break;
            }

            if(harvest)
                denial = holder.can(player, PermissionFlag.HARVEST);
        }

        if(denial.isPresent())
        {
            event.setCancelled(true);
            player.send(FlagHolder.wrapDeny(denial.get()));
            return;
        }

        Block above = block.getRelative(BlockFace.UP);
        if(above.getType().hasGravity() && checkFall(claim, block, player, l, blockPos))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEmptyBucket(PlayerBucketEmptyEvent event)
    {
        Block target = event.getBlockClicked();
        if(!target.getType().isSolid())
            target = target.getRelative(event.getBlockFace());

        Location l = target.getLocation();
        BukkitPlayer player = plugin.player(event.getPlayer());

        BlockPos blockPos = plugin.blockPos(l);
        ClaimedChunk claim = plugin.mineCity.provideChunk(blockPos.getChunk());
        FlagHolder holder = claim.getFlagHolder(blockPos);

        Optional<Message> denial = holder.can(player, PermissionFlag.MODIFY);
        if(denial.isPresent())
        {
            player.send(FlagHolder.wrapDeny(denial.get()));
            plugin.plugin.getScheduler().runTaskLater(plugin.plugin, player.sender::updateInventory, 5);
            event.setCancelled(true);
            return;
        }

        Block above = target.getRelative(BlockFace.UP);
        if(above.getType().hasGravity() && checkFall(claim, target, player, l, blockPos))
        {

            plugin.plugin.getScheduler().runTaskLater(plugin.plugin, player.sender::updateInventory, 5);
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onFillBucket(PlayerBucketFillEvent event)
    {
        Player player = event.getPlayer();
        if(check(event.getBlockClicked().getLocation(), player, PermissionFlag.MODIFY))
        {
            event.setCancelled(true);
            plugin.plugin.getScheduler().runTaskLater(plugin.plugin, player::updateInventory, 5);
        }
    }
}
