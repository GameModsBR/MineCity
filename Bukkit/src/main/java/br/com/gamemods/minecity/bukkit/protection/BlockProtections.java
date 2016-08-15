package br.com.gamemods.minecity.bukkit.protection;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.FlagHolder;
import br.com.gamemods.minecity.api.permission.Identity;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.shape.Cuboid;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.bukkit.MineCityBukkit;
import br.com.gamemods.minecity.bukkit.command.BukkitPlayer;
import br.com.gamemods.minecity.structure.ClaimedChunk;
import br.com.gamemods.minecity.structure.Plot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
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
        Player player = event.getPlayer();
        ItemStack hand = event.getItemInHand();
        if(hand != null && hand.getType() == Material.INK_SACK && hand.getDurability() == 15)
            switch(block.getType())
            {
                case CROPS:
                case BEETROOT_BLOCK:
                case CARROT:
                case POTATO:
                case MELON_STEM:
                case PUMPKIN_STEM:
                case COCOA:
                    return;
            }

        Location l = block.getLocation();
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

        if(!block.getType().hasGravity() && block.getType() != Material.DRAGON_EGG)
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

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event)
    {
        Entity entity = event.getEntity();
        if(entity instanceof Arrow)
        {
            Arrow arrow = (Arrow) entity;
            Block block = event.getBlock();
            if(block.getType() == Material.TNT)
            {
                Object shooter = getShooter(arrow.getShooter(), true);
                if(shooter instanceof Player)
                {
                    if(check(block.getLocation(), (Player) shooter, PermissionFlag.MODIFY))
                        event.setCancelled(true);
                }
                else if(shooter instanceof PlayerID)
                {
                    if(check(block.getLocation(), (PlayerID) shooter, PermissionFlag.MODIFY).isPresent())
                        event.setCancelled(true);
                }
                else
                    event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityInteract(EntityInteractEvent event)
    {
        Entity entity = event.getEntity();
        switch(entity.getType())
        {
            case ARROW:
            case TIPPED_ARROW:
            case SPECTRAL_ARROW:
            {
                Arrow arrow = (Arrow) entity;
                Block block = event.getBlock();
                switch(block.getType())
                {
                    case WOOD_BUTTON:
                    case WOOD_PLATE:
                    case GOLD_PLATE:
                    case IRON_PLATE:
                    {
                        Object shooter = getShooter(arrow.getShooter(), true);
                        if(shooter instanceof Player)
                        {
                            if(check(block.getLocation(), (Player) shooter, PermissionFlag.CLICK))
                                event.setCancelled(true);
                        }
                        else if(shooter instanceof Identity)
                        {
                            if(check(block.getLocation(), (Identity) shooter, PermissionFlag.CLICK).isPresent())
                                event.setCancelled(true);
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        Action action = event.getAction();
        if(action == Action.PHYSICAL)
        {
            Block block = event.getClickedBlock();
            if(block.getType() == Material.SOIL && check(block.getLocation(), event.getPlayer(), PermissionFlag.MODIFY))
                event.setCancelled(true);

            return;
        }

        if(action == Action.RIGHT_CLICK_BLOCK)
        {
            Block block = event.getClickedBlock();
            Material type = block.getType();
            if(event.hasItem())
            {
                switch(event.getMaterial())
                {
                    case ARMOR_STAND:
                    case MONSTER_EGG:
                    case MONSTER_EGGS:
                        if(check(block.getRelative(event.getBlockFace()).getLocation(), event.getPlayer(), PermissionFlag.MODIFY))
                            event.setUseItemInHand(Event.Result.DENY);
                        break;

                    case END_CRYSTAL:
                        if(type == Material.OBSIDIAN || type == Material.BEDROCK)
                            if(check(block.getLocation(), event.getPlayer(), PermissionFlag.MODIFY))
                                event.setUseItemInHand(Event.Result.DENY);
                        break;
                }
            }

            switch(type)
            {
                case CHEST:
                case TRAPPED_CHEST:
                case FURNACE:
                case HOPPER:
                case ANVIL:
                case DROPPER:
                case DISPENSER:
                case BEACON:
                case BREWING_STAND:
                    if(check(block.getLocation(), event.getPlayer(), PermissionFlag.OPEN))
                        event.setUseInteractedBlock(Event.Result.DENY);
                    return;

                case STONE_BUTTON:
                case WOOD_BUTTON:
                case LEVER:
                case DARK_OAK_DOOR:
                case ACACIA_DOOR:
                case BIRCH_DOOR:
                case JUNGLE_DOOR:
                case SPRUCE_DOOR:
                case TRAP_DOOR:
                case WOODEN_DOOR:
                case WORKBENCH:
                case ENCHANTMENT_TABLE:
                case ENDER_CHEST:
                    if(check(block.getLocation(), event.getPlayer(), PermissionFlag.CLICK))
                        event.setUseInteractedBlock(Event.Result.DENY);
                    return;

                case DRAGON_EGG:
                    if(check(block.getLocation(), event.getPlayer(), PermissionFlag.MODIFY))
                        event.setUseInteractedBlock(Event.Result.DENY);
                    return;

                case OBSIDIAN:
                    if(event.getMaterial() == Material.FLINT_AND_STEEL && check(block.getLocation(), event.getPlayer(), PermissionFlag.MODIFY))
                    {
                        event.setCancelled(true);
                        return;
                    }
                    // Fall to check the end crystal
                case BEDROCK:
                    if(event.getMaterial() == Material.END_CRYSTAL && check(block.getLocation(), event.getPlayer(), PermissionFlag.MODIFY))
                        event.setCancelled(true);
                    return;

                case GRASS:
                    switch(event.getMaterial())
                    {
                        case DIAMOND_SPADE:
                        case IRON_SPADE:
                        case STONE_SPADE:
                        case WOOD_SPADE:
                        case GOLD_SPADE:
                        case IRON_HOE:
                        case STONE_HOE:
                        case WOOD_HOE:
                        case DIAMOND_HOE:
                            if(check(block.getLocation(), event.getPlayer(), PermissionFlag.MODIFY))
                                event.setCancelled(true);
                            return;
                        case INK_SACK:
                            if(event.getItem().getDurability() == 15)
                            {
                                if(check(block.getRelative(BlockFace.UP).getLocation(), event.getPlayer(), PermissionFlag.MODIFY))
                                    event.setCancelled(true);
                                //TODO Check around
                                return;
                            }
                    }
                    break;
                case CARROT:
                case CROPS:
                case BEETROOT_BLOCK:
                case PUMPKIN_STEM:
                case MELON_STEM:
                case POTATO:
                case COCOA:
                    if(event.hasItem())
                    {
                        ItemStack item = event.getItem();
                        if(item.getType() == Material.INK_SACK && item.getDurability() == 15)
                        {
                            BukkitPlayer player = plugin.player(event.getPlayer());
                            FlagHolder holder = plugin.mineCity.provideChunk(plugin.chunk(block))
                                    .getFlagHolder(block.getX(), block.getY(), block.getZ());

                            Optional<Message> modify = holder.can(player, PermissionFlag.MODIFY);
                            if(modify.isPresent())
                            {
                                Optional<Message> harvest = holder.can(player, PermissionFlag.HARVEST);
                                if(harvest.isPresent())
                                {
                                    event.setCancelled(true);
                                    player.send(FlagHolder.wrapDeny(MineCity.RANDOM.nextBoolean()? modify.get() : harvest.get()));
                                }
                            }
                            return;
                        }
                    }
                    break;

                case TNT:
                    if(event.getMaterial() == Material.FLINT_AND_STEEL && check(block.getLocation(), event.getPlayer(), PermissionFlag.MODIFY))
                        event.setCancelled(true);
            }
        }
    }

    public boolean checkFromTo(Block fromBlock, Block toBlock)
    {
        BlockPos fromPos = plugin.blockPos(fromBlock);
        BlockPos toPos = new BlockPos(fromPos, toBlock.getX(), toBlock.getY(), toBlock.getZ());
        return checkFromTo(fromPos, toPos);
    }

    public boolean checkFromTo(BlockPos fromPos, BlockPos toPos)
    {
        ClaimedChunk fromClaim = plugin.mineCity.provideChunk(fromPos.getChunk());
        FlagHolder from = fromClaim.getFlagHolder(fromPos);

        ClaimedChunk toClaim = fromClaim.chunk.equals(toPos.getChunk())? fromClaim : plugin.mineCity.provideChunk(toPos.getChunk());
        FlagHolder to = toClaim.getFlagHolder(toPos);

        PlayerID owner = from.owner();
        return !from.equals(to) && (owner == null || to.can(owner, PermissionFlag.MODIFY).isPresent());
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockSpread(BlockSpreadEvent event)
    {
        if(checkFromTo(event.getSource(), event.getBlock()))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event)
    {
        Block block = event.getBlock();
        for(BlockFace face: new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN})
        {
            Block fire = block.getRelative(face);
            if(fire.getType() == Material.FIRE)
            {
                if(!checkFromTo(fire, block))
                    return;
            }
        }

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockForm(EntityBlockFormEvent event)
    {
        BlockState newState = event.getNewState();
        if(newState.getType() == Material.FROSTED_ICE)
        {
            Block block = event.getBlock();
            Entity entity = event.getEntity();
            if(entity instanceof Player)
            {
                if(silentCheck(block.getLocation(), (Player) entity, PermissionFlag.MODIFY).isPresent())
                    event.setCancelled(true);
            }
            else
            {
                BlockPos entityPos = plugin.blockPos(entity.getLocation());
                BlockPos blockPos = plugin.blockPos(entityPos, block);
                ClaimedChunk entityClaim = plugin.mineCity.provideChunk(entityPos.getChunk());
                ClaimedChunk blockClaim = plugin.mineCity.provideChunk(entityClaim.getChunk(), entityClaim);

                FlagHolder entityHolder = entityClaim.getFlagHolder(entityPos);
                FlagHolder blockHolder = blockClaim.getFlagHolder(blockPos);
                if(!entityHolder.equals(blockHolder))
                {
                    PlayerID owner = entityHolder.owner();
                    if(owner == null || blockHolder.can(owner, PermissionFlag.MODIFY).isPresent())
                        event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent event)
    {
        if(checkFromTo(event.getBlock(), event.getToBlock()))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockGrowth(BlockGrowEvent event)
    {
        Material type = event.getNewState().getType();
        switch(type)
        {
            case MELON_BLOCK:
            case PUMPKIN:
            {
                Block toBlock = event.getBlock();
                BlockPos toPos = plugin.blockPos(toBlock);
                ClaimedChunk toClaim = plugin.mineCity.provideChunk(toPos.getChunk());
                FlagHolder to = toClaim.getFlagHolder(toPos);
                type = type == Material.MELON_BLOCK? Material.MELON_STEM : Material.PUMPKIN_STEM;

                for(Direction dir: Direction.cardinal)
                {
                    Block fromBlock = toBlock.getRelative(dir.x, dir.y, dir.z);
                    if(fromBlock.getType() == type)
                    {
                        BlockPos fromPos = plugin.blockPos(toPos, fromBlock);
                        ClaimedChunk fromClaim = plugin.mineCity.provideChunk(fromPos.getChunk(), toClaim);
                        FlagHolder from = fromClaim.getFlagHolder(fromPos);
                        PlayerID owner = from.owner();
                        if(from.equals(to) || (owner != null && !to.can(owner, PermissionFlag.MODIFY).isPresent()))
                        {
                            return;
                        }
                    }
                }

                event.setCancelled(true);
            }
        }
    }
}
