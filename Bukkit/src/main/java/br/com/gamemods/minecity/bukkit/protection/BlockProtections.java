package br.com.gamemods.minecity.bukkit.protection;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.FlagHolder;
import br.com.gamemods.minecity.api.permission.Identity;
import br.com.gamemods.minecity.api.permission.Permissible;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.shape.Cuboid;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.bukkit.MineCityBukkit;
import br.com.gamemods.minecity.bukkit.command.BukkitPlayer;
import br.com.gamemods.minecity.structure.City;
import br.com.gamemods.minecity.structure.ClaimedChunk;
import br.com.gamemods.minecity.structure.Nature;
import br.com.gamemods.minecity.structure.Plot;
import com.google.common.collect.MapMaker;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityCreatePortalEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.CocoaPlant;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public class BlockProtections extends AbstractProtection
{
    private Map<Location, Player> portalCreator = new MapMaker().weakKeys().weakValues().makeMap();
    private boolean harvesting;

    public BlockProtections(@NotNull MineCityBukkit plugin)
    {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockMultiPlace(BlockMultiPlaceEvent event)
    {
        BukkitPlayer player = plugin.player(event.getPlayer());
        BlockPos blockPos = plugin.blockPos(event.getBlock());
        ClaimedChunk chunk = null;
        for(BlockState state : event.getReplacedBlockStates())
        {
            blockPos = plugin.blockPos(blockPos, state.getBlock());
            chunk = plugin.mineCity.provideChunk(blockPos.getChunk(), chunk);
            FlagHolder holder = chunk.getFlagHolder(blockPos);
            Optional<Message> denial = holder.can(player, PermissionFlag.MODIFY);
            if(denial.isPresent())
            {
                event.setCancelled(true);
                player.send(FlagHolder.wrapDeny(denial.get()));
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event)
    {
        Block block = event.getBlock();
        Player player = event.getPlayer();
        ItemStack hand = event.getItemInHand();
        Material type = block.getType();
        if(hand != null && hand.getType() == Material.INK_SACK && hand.getDurability() == 15)
            switch(type)
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

        Set<Block> affected = Collections.emptySet();
        switch(type)
        {
            case PUMPKIN:
            {
                affected = new HashSet<>(5);
                BlockFace[] faces = {
                        BlockFace.SOUTH, BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN
                };

                for(BlockFace face: faces)
                {
                    Block a = block.getRelative(face);
                    if(a.getType() != Material.SNOW_BLOCK)
                        continue;
                    Block b = a.getRelative(face);
                    if(b.getType() != Material.SNOW_BLOCK)
                        continue;

                    affected.add(a);
                    affected.add(b);
                    break;
                }

                if(affected.isEmpty())
                    detectShapeT(block, Material.IRON_BLOCK, true, affected);
                break;
            }

            case SKULL:
            {
                Skull skull = (Skull) block.getState();
                if(skull.getSkullType() == SkullType.WITHER)
                {
                    affected = new HashSet<>(7);
                    for(BlockFace face: new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN})
                    {
                        Block b = block.getRelative(face);
                        if(b.getType() != Material.SKULL)
                            continue;

                        skull = (Skull) b.getState();
                        if(skull.getSkullType() != SkullType.WITHER)
                            continue;

                        Block c = block.getRelative(face, 2);
                        if(c.getType() == Material.SKULL)
                        {
                            skull = (Skull) c.getState();
                            if(skull.getSkullType() == SkullType.WITHER)
                            {
                                if(detectShapeT(b, Material.SOUL_SAND, false, affected))
                                {
                                    affected.add(b);
                                    affected.add(c);
                                }
                            }

                            continue;
                        }

                        c = block.getRelative(face.getOppositeFace());
                        if(c.getType() != Material.SKULL)
                            continue;

                        skull = (Skull) c.getState();
                        if(skull.getSkullType() != SkullType.WITHER)
                            continue;

                        if(detectShapeT(block, Material.SOUL_SAND, false, affected))
                        {
                            affected.add(b);
                            affected.add(c);
                        }
                    }
                }
            }
        }


        if(!affected.isEmpty())
        {
            HashSet<Block> gravity = new HashSet<>(3);
            for(Block aff: affected)
            {
                Material affType = aff.getRelative(BlockFace.UP).getType();
                if(affType.hasGravity() || affType == Material.ANVIL || affType == Material.DRAGON_EGG)
                    gravity.add(aff);
            }

            affected.addAll(gravity);

            for(Block aff: affected)
            {
                BlockPos affPos = plugin.blockPos(blockPos, aff);
                ClaimedChunk affClaim = plugin.mineCity.provideChunk(affPos.getChunk(), chunk);
                FlagHolder affHolder = affClaim.getFlagHolder(affPos);
                if(!affHolder.equals(holder))
                {
                    Optional<Message> affDeny = affHolder.can(user, PermissionFlag.MODIFY);
                    if(affDeny.isPresent())
                    {
                        event.setCancelled(true);
                        user.send(FlagHolder.wrapDeny(affDeny.get()));
                        return;
                    }
                }

                Block above = aff.getRelative(BlockFace.UP);
                Material aboveType = above.getType();
                if(aboveType.hasGravity() || aboveType == Material.ANVIL || aboveType == Material.DRAGON_EGG)
                    checkFall(affClaim, above, user, above.getLocation(), affPos.add(Direction.UP));
            }
        }

        if(!type.hasGravity() && type != Material.DRAGON_EGG && type != Material.ANVIL)
            return;

        switch(block.getRelative(BlockFace.DOWN).getType())
        {
            // Gravity blocks falls on placement only if the block bellow is one of those:
            case AIR:
            case FIRE:
            case WATER:
            case STATIONARY_WATER:
            case LAVA:
            case STATIONARY_LAVA:
                if(checkFall(chunk, block, user, l, blockPos))
                    event.setCancelled(true);
        }
    }

    private boolean detectShapeT(final Block base, Material material, boolean checkAir, Collection<Block> found)
    {
        boolean addBase = false;
        for(BlockFace face: new BlockFace[]{BlockFace.SOUTH, BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN})
        {
            Block a = base.getRelative(face);
            if(a.getType() != material)
                continue;

            Block b = base.getRelative(face, 2);
            if(b.getType() != material)
                continue;

            int[] coords = {face.getModX(), face.getModY(), face.getModZ()};
            for(int i = 0; i < 3; i++)
                if(coords[i] != 0)
                    coords[i] = 2;

            while(true)
            {
                boolean modified = false;
                change:
                for(int i = 0; i < 3; i++)
                    switch(coords[i])
                    {
                        case 0:  coords[i] = -1; modified = true; break change;
                        case -1: coords[i] = 2 ; break;
                    }

                if(!modified)
                    break;

                Block c = a.getRelative(
                        coords[0] == 2? 0 : coords[0],
                        coords[1] == 2? 0 : coords[1],
                        coords[2] == 2? 0 : coords[2]
                );

                if(c.getType() != material)
                    continue;

                Block d = a.getRelative(
                        coords[0] == 2? 0 : coords[0] * -1,
                        coords[1] == 2? 0 : coords[1] * -1,
                        coords[2] == 2? 0 : coords[2] * -1
                );

                if(d.getType() != material)
                    continue;

                if(checkAir)
                {
                    Block air = c.getRelative(
                            coords[0] == 2? face.getModX() * -1 : 0,
                            coords[1] == 2? face.getModY() * -1 : 0,
                            coords[2] == 2? face.getModZ() * -1 : 0
                    );

                    if(air.getType() != Material.AIR)
                        continue;

                    air = d.getRelative(
                            coords[0] == 2? face.getModX() * -1 : 0,
                            coords[1] == 2? face.getModY() * -1 : 0,
                            coords[2] == 2? face.getModZ() * -1 : 0
                    );

                    if(air.getType() != Material.AIR)
                        continue;
                }

                found.add(a);
                found.add(b);
                found.add(c);
                found.add(d);
                addBase = true;
            }
        }

        return addBase;
    }

    /**
     * Checks if a gravity block would fall into an unauthorized area
     * @param claim The claim that the gravity block is located
     * @param block The gravity block
     * @param user Who caused this check
     * @param l The block's location
     * @param blockPos The block's position
     * @return {@code true} if the action must be cancelled
     */
    public boolean checkFall(ClaimedChunk claim, Block block, Permissible user, Location l, BlockPos blockPos)
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
        City city = claim.getCity().get();

        Plot last = null;
        down:
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

            Optional<Message> denial = null;
            for(Plot plot: risk)
            {
                if(plot.getShape().contains(l.getBlockX(), l.getBlockY(), l.getBlockZ()))
                {
                    if(plot.equals(last))
                        continue down;
                    last = plot;

                    denial = plot.can(user, PermissionFlag.MODIFY);
                    if(denial.isPresent())
                        break;
                }
            }

            if(denial == null)
                denial = city.can(user, PermissionFlag.MODIFY);

            if(denial.isPresent())
            {
                if(user instanceof BukkitPlayer)
                    user.send(FlagHolder.wrapDeny(denial.get()));
                return true;
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event)
    {
        harvesting = false;
        Block block = event.getBlock();
        Location l = block.getLocation();
        BukkitPlayer player = plugin.player(event.getPlayer());

        BlockPos blockPos = plugin.blockPos(l);
        ClaimedChunk claim = plugin.mineCity.provideChunk(blockPos.getChunk());

        FlagHolder holder = claim.getFlagHolder(blockPos);
        Optional<Message> denial = holder.can(player, PermissionFlag.MODIFY);

        boolean harvest = false;
        if(denial.isPresent())
        {
            Material type = block.getType();
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
                                || relative == Material.MELON_STEM && type == Material.MELON_BLOCK;

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
            {
                denial = holder.can(player, PermissionFlag.HARVEST);
                if(!denial.isPresent() && player.sender.getGameMode() == GameMode.CREATIVE)
                    denial = Optional.of(new Message(
                            "action.harvest-on-creative",
                            "You can't harvest plants on creative mode, that would just reset the growth state without any drop."
                    ));
            }
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
        else
            harvesting = harvest;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreakMonitor(BlockBreakEvent event)
    {
        if(!harvesting)
            return;

        harvesting = false;

        Block block = event.getBlock();
        final Material type = block.getType();
        final Material seeds;
        switch(type)
        {
            case CROPS:
                seeds = Material.SEEDS;
                break;

            case POTATO:
                seeds = Material.POTATO_ITEM;
                break;

            case CARROT:
                seeds = Material.CARROT_ITEM;
                break;

            case BEETROOT_BLOCK:
                seeds = Material.BEETROOT_SEEDS;
                break;

            case NETHER_WART_BLOCK:
                seeds = Material.NETHER_WARTS;
                break;

            case COCOA:
                seeds = Material.INK_SACK;
                break;

            default:
                return;
        }

        switch(type)
        {
            case CROPS:
            case POTATO:
            case CARROT:
            case BEETROOT_BLOCK:
            case NETHER_WART_BLOCK:
                plugin.scheduler.runTask(plugin.plugin, ()-> block.setType(type));
                plugin.entityProtections.consumeDrop(block.getLocation(), seeds, 2);
                break;

            case COCOA:
                final BlockState cocoa = block.getState();
                CocoaPlant data = (CocoaPlant) cocoa.getData();
                data.setSize(CocoaPlant.CocoaPlantSize.SMALL);
                cocoa.setData(data);
                plugin.scheduler.runTask(plugin.plugin, ()-> cocoa.update(true));
                plugin.entityProtections.consumeDrop(block.getLocation(), seeds, 2);
                break;
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEmptyBucket(PlayerBucketEmptyEvent event)
    {
        Block target = event.getBlockClicked();
        if(target.getType().isSolid())
            target = target.getRelative(event.getBlockFace());

        Location l = target.getLocation();
        Player entity = event.getPlayer();
        BukkitPlayer player = plugin.player(entity);

        BlockPos blockPos = plugin.blockPos(l);
        ClaimedChunk claim = plugin.mineCity.provideChunk(blockPos.getChunk());
        FlagHolder holder = claim.getFlagHolder(blockPos);

        Optional<Message> denial = holder.can(player, PermissionFlag.MODIFY);
        if(denial.isPresent())
        {
            event.setCancelled(true);
            player.send(FlagHolder.wrapDeny(denial.get()));
            sendUpdates(player.sender, target, target.getLocation());
            return;
        }

        Block above = target.getRelative(BlockFace.UP);
        if(above.getType().hasGravity() && checkFall(claim, target, player, l, blockPos))
        {
            event.setCancelled(true);
            sendUpdates(player.sender, target, target.getLocation());
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onFillBucket(PlayerBucketFillEvent event)
    {
        Player player = event.getPlayer();
        Block target = event.getBlockClicked();
        if(!target.isLiquid())
            target = target.getRelative(event.getBlockFace());

        Location liquidLoc = target.getLocation();
        if(check(liquidLoc, player, PermissionFlag.MODIFY))
        {
            event.setCancelled(true);
            sendUpdates(player, target, liquidLoc);
        }
    }

    private void sendUpdates(Player player, Block liquid, Location liquidLoc)
    {
        BlockIterator iter = new BlockIterator(player, (int)Math.ceil(liquidLoc.distance(player.getLocation())));
        plugin.plugin.getScheduler().runTask(plugin.plugin, ()->{
            player.updateInventory();
            player.sendBlockChange(liquidLoc, liquid.getType(), liquid.getData());
            HashSet<Vector> updates = new HashSet<>();
            while(iter.hasNext())
            {
                Block next = iter.next();
                Location blocLoc = next.getLocation();
                for(int ix = -1; ix <= 1; ix++)
                    for(int iz = -1; iz <= 1; iz++)
                        for(int iy = -1; iy <= 1; iy++)
                            updates.add(new Vector(
                                    blocLoc.getBlockX()+ix,
                                    blocLoc.getBlockY()+iy,
                                    blocLoc.getBlockZ()+iz
                            ));
            }

            for(Vector pos: updates)
            {
                Location loc = pos.toLocation(liquid.getWorld());
                Block block = loc.getBlock();
                player.sendBlockChange(loc, block.getType(), block.getData());
            }
        });
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
                else if(shooter instanceof Identity)
                {
                    if(check(block.getLocation(), (Identity) shooter, PermissionFlag.MODIFY).isPresent())
                        event.setCancelled(true);
                }
                else
                    event.setCancelled(true);
            }
        }
        else if(entity instanceof Enderman)
        {
            Block block = event.getBlock();
            FlagHolder holder = plugin.getFlagHolder(block.getLocation());
            if(!(holder instanceof Nature) || block.getY() >= 40)
                event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityBlockForm(EntityBlockFormEvent event)
    {
        Entity entity = event.getEntity();
        if(entity instanceof Snowman)
        {
            for(MetadataValue value : entity.getMetadata("SnowmanData"))
            {
                if(value.getOwningPlugin() == plugin.plugin)
                {
                    SnowmanData data = (SnowmanData) value.value();
                    if(data.checkFormSnow((Snowman) entity, event.getBlock()))
                        event.setCancelled(true);

                    return;
                }
            }

            event.setCancelled(true);
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
                    {
                        Block relative = block.getRelative(event.getBlockFace());
                        BlockPos pos = plugin.blockPos(relative);
                        ClaimedChunk claimedChunk = plugin.mineCity.provideChunk(pos.getChunk());
                        BukkitPlayer player = plugin.player(event.getPlayer());

                        Optional<Message> denial = claimedChunk.getFlagHolder(pos).can(player, PermissionFlag.MODIFY);
                        if(denial.isPresent())
                        {
                            event.setUseItemInHand(Event.Result.DENY);
                            player.send(FlagHolder.wrapDeny(denial.get()));
                            break;
                        }
                        else if(checkFall(claimedChunk, relative, player, relative.getLocation(), pos))
                            event.setUseItemInHand(Event.Result.DENY);
                        break;
                    }

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
                    if(event.getMaterial() == Material.FLINT_AND_STEEL)
                    {
                        Player player = event.getPlayer();
                        Location loc = block.getRelative(event.getBlockFace()).getLocation();
                        portalCreator.put(loc, player);
                        plugin.scheduler.runTaskLater(plugin.plugin, ()-> portalCreator.remove(loc, player), 2);

                        if(check(loc, player, PermissionFlag.MODIFY))
                            event.setCancelled(true);
                        return;
                    }
                    // Fall to check the end crystal
                case BEDROCK:
                    if(event.getMaterial() == Material.END_CRYSTAL && check(block.getLocation(), event.getPlayer(), PermissionFlag.MODIFY))
                        event.setCancelled(true);
                    return;

                case LONG_GRASS:
                case BROWN_MUSHROOM:
                case RED_MUSHROOM:
                case SAPLING:
                {
                    ItemStack item = event.getItem();
                    if(item != null && item.getType() == Material.INK_SACK && item.getDurability() == 15)
                    {
                        if(check(block.getLocation().add(0,1,0), event.getPlayer(), PermissionFlag.MODIFY))
                            event.setCancelled(true);
                    }
                    break;
                }

                case DIRT:
                    switch(event.getMaterial())
                    {
                        case IRON_HOE:
                        case STONE_HOE:
                        case WOOD_HOE:
                        case DIAMOND_HOE:
                        case GOLD_HOE:
                            if(check(block.getLocation(), event.getPlayer(), PermissionFlag.MODIFY))
                                event.setCancelled(true);
                            return;
                    }
                    break;

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
                        case GOLD_HOE:
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

        return !from.equals(to) && to.can(from.owner(), PermissionFlag.MODIFY).isPresent();
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
                    if(blockHolder.can(entityHolder.owner(), PermissionFlag.MODIFY).isPresent())
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
                        if(from.equals(to) || !to.can(from.owner(), PermissionFlag.MODIFY).isPresent())
                        {
                            return;
                        }
                    }
                }

                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onStructureGrow(StructureGrowEvent event)
    {
        Function<FlagHolder, Optional<Message>> check;
        Player player = event.getPlayer();
        BlockPos pos = plugin.blockPos(event.getLocation());
        ClaimedChunk claim = plugin.mineCity.provideChunk(pos.getChunk());
        FlagHolder fromHolder = claim.getFlagHolder(pos);
        BukkitPlayer bukkitPlayer;
        if(player != null)
        {
            bukkitPlayer = plugin.player(player);
            check = fh -> fh.can(bukkitPlayer, PermissionFlag.MODIFY);
        }
        else
        {
            bukkitPlayer = null;
            Identity<?> owner = fromHolder.owner();
            check = fh -> fh.can(owner, PermissionFlag.MODIFY);
        }

        for(BlockState state : event.getBlocks())
        {
            BlockPos blockPos = plugin.blockPos(pos, state.getBlock());
            ClaimedChunk chunk = plugin.mineCity.provideChunk(blockPos.getChunk(), claim);
            FlagHolder flagHolder = chunk.getFlagHolder(blockPos);
            if(!flagHolder.equals(fromHolder))
            {
                Optional<Message> denial = check.apply(flagHolder);
                if(denial.isPresent())
                {
                    event.setCancelled(true);
                    if(bukkitPlayer != null)
                        bukkitPlayer.send(FlagHolder.wrapDeny(denial.get()));
                    return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPortalCreate(PortalCreateEvent event)
    {
        ArrayList<Block> blocks = event.getBlocks();

        Player player;
        int size = portalCreator.size();
        if(size == 0)
            player = null;
        else if(size == 1)
        {
            Map.Entry<Location, Player> single = portalCreator.entrySet().iterator().next();
            if(blocks.contains(single.getKey().getBlock()))
                player = single.getValue();
            else
                player = null;
        }
        else if(size < blocks.size())
        {
            player = null;
            for(Map.Entry<Location, Player> entry : portalCreator.entrySet())
                if(blocks.contains(entry.getKey().getBlock()))
                {
                    player = entry.getValue();
                    break;
                }
        }
        else
        {
            player = null;
            for(Block block: blocks)
            {
                player = portalCreator.get(block.getLocation());
                if(player != null)
                    break;
            }
        }

        BlockPos cache = plugin.blockPos(blocks.get(0));
        cache.getChunk();
        Optional<Message> denial = onPortalCreation(blocks.stream().map(b-> plugin.blockPos(cache, b)), player);
        if(denial.isPresent())
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityCreatePortal(EntityCreatePortalEvent event)
    {
        LivingEntity entity = event.getEntity();
        List<BlockState> blocks = event.getBlocks();
        BlockPos cache = plugin.blockPos(blocks.get(0));
        cache.getChunk();
        Optional<Message> message = onPortalCreation(blocks.stream().map(b-> plugin.blockPos(cache, b)), entity);
        if(message.isPresent())
        {
            event.setCancelled(true);
            if(entity instanceof Player)
            {
                BukkitPlayer player = plugin.player((Player) entity);
                player.send(FlagHolder.wrapDeny(message.get()));
            }
        }
    }

    public Optional<Message> onPortalCreation(Stream<BlockPos> blocks, Entity creator)
    {
        BukkitPlayer user = creator instanceof Player? plugin.player((Player) creator) : null;

        return blocks.map(new Function<BlockPos, Optional<Message>>()
        {
            ClaimedChunk claim = null;
            FlagHolder last = null;

            @Override
            public Optional<Message> apply(BlockPos blockPos)
            {
                claim = plugin.mineCity.provideChunk(blockPos.getChunk(), claim);
                FlagHolder holder = claim.getFlagHolder(blockPos);
                if(holder == last)
                    return Optional.empty();
                last = holder;

                if(user == null)
                {
                    if(!(holder instanceof Nature))
                    {
                        return Optional.of(new Message("From unknown to somewhere"));
                    }
                    return Optional.empty();
                }

                Optional<Message> denial = holder.can(user, PermissionFlag.MODIFY);
                if(denial.isPresent())
                {
                    return denial;
                }

                return Optional.empty();
            }
        }).filter(Optional::isPresent).map(Optional::get).findFirst();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerPortal(PlayerPortalEvent event)
    {
        event.setPortalTravelAgent(new SafeTravelAgent(plugin, event.getPortalTravelAgent(), event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityPortal(EntityPortalEvent event)
    {
        event.setPortalTravelAgent(new SafeTravelAgent(plugin, event.getPortalTravelAgent(), event.getEntity()));
    }

    private boolean checkPistonEvent(Block piston, Collection<Block> movedBlocks)
    {
        BlockPos pistonPos = plugin.blockPos(piston);
        ClaimedChunk pistonChunk = plugin.mineCity.provideChunk(pistonPos.getChunk());
        FlagHolder pistonHolder = pistonChunk.getFlagHolder(pistonPos);
        Identity<?> owner = pistonHolder.owner();

         return movedBlocks.stream()
                .map(block -> plugin.blockPos(pistonPos, block))
                .map(pos -> plugin.mineCity.provideChunk(pos.getChunk(), pistonChunk).getFlagHolder(pos))
                .anyMatch(holder -> holder.can(owner, PermissionFlag.MODIFY).isPresent())
        ;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event)
    {
        if(checkPistonEvent(event.getBlock(), event.getBlocks()))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event)
    {
        if(checkPistonEvent(event.getBlock(), event.getBlocks()))
            event.setCancelled(true);
    }
}
