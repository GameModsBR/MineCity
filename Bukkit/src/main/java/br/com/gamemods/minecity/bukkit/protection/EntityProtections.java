package br.com.gamemods.minecity.bukkit.protection;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.api.Lazy;
import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.FlagHolder;
import br.com.gamemods.minecity.api.permission.Identity;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.bukkit.BukkitUtil;
import br.com.gamemods.minecity.bukkit.BukkitUtil19;
import br.com.gamemods.minecity.bukkit.MineCityBukkit;
import br.com.gamemods.minecity.bukkit.command.BukkitPlayer;
import br.com.gamemods.minecity.structure.ClaimedChunk;
import br.com.gamemods.minecity.structure.Nature;
import com.google.common.collect.MapMaker;
import org.bukkit.Art;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.entity.minecart.PoweredMinecart;
import org.bukkit.entity.minecart.RideableMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.BlockProjectileSource;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static br.com.gamemods.minecity.api.CollectionUtil.optionalStream;
import static br.com.gamemods.minecity.api.permission.FlagHolder.can;
import static br.com.gamemods.minecity.api.permission.FlagHolder.wrapDeny;
import static br.com.gamemods.minecity.api.permission.PermissionFlag.*;
import static br.com.gamemods.minecity.bukkit.BukkitUtil.optional;

@SuppressWarnings("deprecation")
public class EntityProtections extends AbstractProtection
{
    private Set<Egg> eggs = Collections.newSetFromMap(new MapMaker().weakKeys().makeMap());
    private Set<EnderPearl> pearls = Collections.newSetFromMap(new MapMaker().weakKeys().makeMap());
    private Map<UUID, Collection<UUID>> drops = new MapMaker().weakKeys().makeMap();
    private Map<UUID, Player> attackers = new MapMaker().weakKeys().makeMap();
    private List<Function<Item, Boolean>> captureDrops = new ArrayList<>();
    private Map<UUID, Object> explosionCreator = new MapMaker().weakKeys().makeMap();

    public EntityProtections(@NotNull MineCityBukkit plugin)
    {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event)
    {
        CreatureSpawnEvent.SpawnReason reason = event.getSpawnReason();
        switch(reason)
        {
            case ENDER_PEARL:
            case EGG:
            {
                Location location = event.getLocation();
                Set<? extends Projectile> set = reason == CreatureSpawnEvent.SpawnReason.EGG? eggs : pearls;
                Projectile entity = getNearest(set, location);
                if(entity == null)
                {
                    event.setCancelled(true);
                    return;
                }

                ProjectileSource shooter = entity.getShooter();
                if(shooter instanceof Player)
                {
                    if(check(location, (Player) shooter, PermissionFlag.MODIFY))
                    {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
            case BUILD_SNOWMAN:
            {
                BlockPos pos = plugin.blockPos(event.getLocation());
                ClaimedChunk chunk = plugin.mineCity.provideChunk(pos.getChunk());
                Snowman entity = (Snowman) event.getEntity();
                plugin.callSyncMethod(()->
                    entity.setMetadata(SnowmanData.KEY, new FixedMetadataValue(
                            plugin.plugin,
                            new SnowmanData(plugin, chunk, pos)
                    ))
                );
            }
            break;
            case DEFAULT:
            {
                if(event.getEntityType() == EntityType.ARMOR_STAND)
                {
                    ArmorStand armorStand = (ArmorStand) event.getEntity();
                    plugin.callSyncMethod(() ->
                        armorStand.setMetadata(ArmorStandData.KEY, new FixedMetadataValue(
                                plugin.plugin,
                                new ArmorStandData(plugin, armorStand)
                        ))
                    );
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onProjectHit(ProjectileHitEvent event)
    {
        Projectile entity = event.getEntity();
        switch(entity.getType())
        {
            case EGG:
            {
                Egg egg = (Egg) entity;
                eggs.add(egg);
                plugin.plugin.getScheduler().runTaskLater(plugin.plugin, () -> eggs.remove(egg), 5);
            }
            break;
            case ENDER_PEARL:
            {
                EnderPearl pearl = (EnderPearl) entity;
                Location location = pearl.getLocation();

                ProjectileSource shooter = pearl.getShooter();
                if(shooter instanceof Player)
                {
                    if(check(location, (Player) shooter, PermissionFlag.ENTER))
                    {
                        pearl.remove();
                        return;
                    }
                }

                pearls.add(pearl);
                plugin.plugin.getScheduler().runTaskLater(plugin.plugin, () -> pearls.remove(pearl), 5);
            }
            break;
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onVehicleDamage(VehicleDamageEvent event)
    {
        Entity attacker = event.getAttacker();
        Vehicle vehicle = event.getVehicle();

        if(attacker instanceof Projectile)
        {
            ProjectileSource shooter = ((Projectile) attacker).getShooter();

            if(shooter instanceof Entity)
                attacker = (Entity) shooter;
            else if(shooter instanceof BlockProjectileSource)
            {
                Block block = ((BlockProjectileSource) shooter).getBlock();
                Location loc = block.getLocation();
                ChunkPos chunkPos = plugin.chunk(loc);
                ClaimedChunk chunk = plugin.mineCity.provideChunk(chunkPos);
                Identity<?> owner = chunk.getFlagHolder(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()).owner();

                BlockPos blockPos = plugin.blockPos(vehicle.getLocation());
                FlagHolder holder;
                if(blockPos.getChunk().equals(chunkPos))
                    holder = chunk.getFlagHolder(blockPos);
                else
                    holder = plugin.mineCity.provideChunk(blockPos.getChunk()).getFlagHolder(blockPos);

                Optional<Message> denial = holder.can(owner, PermissionFlag.MODIFY);
                if(denial.isPresent())
                {
                    event.setCancelled(true);
                    return;
                }
            }
        }

        if(attacker instanceof Player)
        {
            if(check(vehicle.getLocation(), (Player) attacker, PermissionFlag.MODIFY))
                event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event)
    {
        Entity attacker = event.getDamager();
        switch(attacker.getType())
        {
            case EGG:
                Egg egg = (Egg) attacker;
                eggs.add(egg);
                plugin.plugin.getScheduler().runTaskLater(plugin.plugin, () -> eggs.remove(egg), 5);
                break;

            case ENDER_PEARL:
                EnderPearl pearl = (EnderPearl) pearls;
                pearls.add(pearl);
                plugin.plugin.getScheduler().runTaskLater(plugin.plugin, () -> pearls.remove(pearl), 5);
                break;
        }

        if(event.isCancelled())
            return;

        if(attacker instanceof TNTPrimed)
        {
            TNTPrimed tnt = (TNTPrimed) attacker;
            Entity source = tnt.getSource();
            if(source != null)
                attacker = source;
            else
            {
                event.setCancelled(true);
                return;
            }
        }

        if(attacker instanceof Projectile || attacker instanceof AreaEffectCloud)
        {
            ProjectileSource shooter;
            if(attacker instanceof Projectile)
                shooter = ((Projectile) attacker).getShooter();
            else
                shooter = ((AreaEffectCloud) attacker).getSource();

            if(shooter instanceof Entity)
            {
                attacker = (Entity) shooter;
            }
            else if(shooter instanceof BlockProjectileSource)
            {
                Block block = ((BlockProjectileSource) shooter).getBlock();
                Location loc = block.getLocation();
                ChunkPos chunkPos = plugin.chunk(loc);
                ClaimedChunk chunk = plugin.mineCity.provideChunk(chunkPos);
                Identity<?> owner = chunk.getFlagHolder(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()).owner();

                Entity victim = event.getEntity();
                BlockPos blockPos = plugin.blockPos(victim.getLocation());
                FlagHolder holder;
                if(blockPos.getChunk().equals(chunkPos))
                    holder = chunk.getFlagHolder(blockPos);
                else
                    holder = plugin.mineCity.provideChunk(blockPos.getChunk()).getFlagHolder(blockPos);

                Optional<Message> denial = holder.can(owner,
                        victim instanceof Player? PVP : victim.getCustomName() != null? MODIFY :
                                victim instanceof Monster? PVM : victim instanceof LivingEntity? PVC : MODIFY
                );
                if(denial.isPresent())
                {
                    event.setCancelled(true);
                    return;
                }

                if(victim instanceof EnderCrystal)
                    explosionCreator.put(victim.getUniqueId(), owner);
                return;
            }
        }

        Creature tame = null;
        if(attacker instanceof Tameable)
        {
            if(attacker instanceof Creature)
                tame = (Creature) attacker;

            AnimalTamer tamer = ((Tameable) attacker).getOwner();
            if(tamer instanceof Entity)
                attacker = (Entity) tamer;
            else if(tamer instanceof OfflinePlayer)
            {
                OfflinePlayer offlinePlayer = (OfflinePlayer) tamer;
                PlayerID owner = new PlayerID(offlinePlayer.getUniqueId(), offlinePlayer.getName());

                Entity victim = event.getEntity();
                BlockPos blockPos = plugin.blockPos(victim.getLocation());
                FlagHolder holder = plugin.mineCity.provideChunk(blockPos.getChunk()).getFlagHolder(blockPos);

                Optional<Message> denial = holder.can(owner,
                        victim instanceof Player? PVP : victim.getCustomName() != null? MODIFY :
                                victim instanceof Monster? PVM : victim instanceof LivingEntity? PVC : MODIFY
                );
                if(denial.isPresent())
                {
                    event.setCancelled(true);
                    if(tame != null)
                        tame.setTarget(null);
                    return;
                }

                if(victim instanceof EnderCrystal)
                    explosionCreator.put(victim.getUniqueId(), owner);
                return;
            }
        }

        Entity victim = event.getEntity();
        if(attacker.equals(victim))
            return;

        if(attacker instanceof Monster)
        {
            if(victim instanceof Player)
            {
                if(silentCheck(attacker.getLocation(), (Player) victim, attacker.getCustomName() != null? MODIFY : PVM).isPresent())
                {
                    event.setCancelled(true);
                    ((Monster) attacker).setTarget(null);
                    return;
                }
            }
            else
            {
                LivingEntity target = ((Monster) attacker).getTarget();
                if(target instanceof Player)
                {
                    if(silentCheck(victim.getLocation(), (Player) target, playerAttackType(victim)).isPresent())
                    {
                        event.setCancelled(true);
                        if(tame != null)
                            tame.setTarget(null);
                        return;
                    }
                    else if(victim instanceof Monster)
                    {
                        if(silentCheck(victim.getLocation(), (Player) target, playerAttackType(victim)).isPresent())
                        {
                            event.setCancelled(true);
                            if(tame != null)
                                tame.setTarget(null);
                            return;
                        }
                    }
                }

                if(victim instanceof EnderCrystal)
                    explosionCreator.put(victim.getUniqueId(), attacker);
            }

            return;
        }

        if(attacker instanceof Player)
        {
            if(check(victim.getLocation(), (Player) attacker, playerAttackType(victim)))
            {
                event.setCancelled(true);
                if(tame != null)
                    tame.setTarget(null);
                return;
            }

            if(victim instanceof EnderCrystal)
                explosionCreator.put(victim.getUniqueId(), attacker);
        }
    }

    private PermissionFlag playerAttackType(Entity entity)
    {
        if(entity.getCustomName() != null)
            return MODIFY;

        if(entity instanceof Player)
            return PVP;

        if(entity instanceof Monster)
            return PVM;

        if(entity instanceof LivingEntity)
            return PVC;

        return MODIFY;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event)
    {
        Entity entity = event.getRightClicked();
        Player player = event.getPlayer();
        if(entity instanceof ArmorStand)
        {
            if(check(entity.getLocation(), player, PermissionFlag.OPEN))
                event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event)
    {
        Entity entity = event.getRightClicked();
        Player player = event.getPlayer();
        Optional<ItemStack> hand = BukkitUtil19.getItemInSlot(player.getInventory(), event.getHand());

        if(entity instanceof Villager)
        {
            Villager villager = (Villager) entity;
            if(villager.isAdult() && check(villager.getLocation(), player, PermissionFlag.HARVEST))
                event.setCancelled(true);
        }
        else if(entity instanceof Horse)
        {
            Horse horse = (Horse) entity;
            if(horse.isAdult())
            {
                Material m = hand.map(ItemStack::getType).orElse(Material.AIR);
                boolean modifying;
                boolean sendUpdate = false;
                switch(m)
                {
                    case SADDLE:
                        modifying = !optional(horse.getInventory().getSaddle()).isPresent();
                        break;

                    case DIAMOND_BARDING:
                    case GOLD_BARDING:
                    case IRON_BARDING:
                        modifying = !optional(horse.getInventory().getArmor()).isPresent();
                        break;

                    case CHEST:
                        switch(horse.getVariant())
                        {
                            case MULE:
                            case DONKEY:
                                modifying = !horse.isCarryingChest();
                                sendUpdate = true;
                                break;

                            default:
                                modifying = false;
                        }
                        break;

                    default:
                        modifying = false;
                }

                if(modifying)
                {
                    if(!player.equals(horse.getOwner()))
                    {
                        if(check(horse.getLocation(), player, PermissionFlag.MODIFY))
                        {
                            event.setCancelled(true);
                            if(sendUpdate)
                                plugin.scheduler.runTask(plugin.plugin, player::updateInventory);
                        }
                    }
                }
                else
                {
                    if(check(entity.getLocation(), player, PermissionFlag.ENTER, PermissionFlag.RIDE))
                    {
                        event.setCancelled(true);

                        if(player.equals(horse.getOwner()))
                            entity.teleport(player);
                    }
                }
            }
        }
        else if(entity instanceof Sheep)
        {
            Sheep sheep = (Sheep) entity;
            if(!sheep.isSheared() && hand.map(ItemStack::getType).filter(Material.SHEARS::equals).isPresent())
            {
                if(check(entity.getLocation(), player, PermissionFlag.HARVEST))
                    event.setCancelled(true);
            }
        }
        else if(entity instanceof Minecart)
        {
            if(entity instanceof RideableMinecart)
            {
                if(check(entity.getLocation(), player, PermissionFlag.ENTER, PermissionFlag.RIDE))
                    event.setCancelled(true);
            }
            else if(entity instanceof InventoryHolder)
            {
                if(check(entity.getLocation(), player, PermissionFlag.OPEN))
                    event.setCancelled(true);
            }
            else if(entity instanceof PoweredMinecart)
            {
                if(check(entity.getLocation(), player, PermissionFlag.CLICK))
                    event.setCancelled(true);
            }
            else if(entity instanceof ExplosiveMinecart)
            {
                if(hand.map(ItemStack::getType).filter(Material.FLINT_AND_STEEL::equals).isPresent())
                {
                    if(check(entity.getLocation(), player, PermissionFlag.MODIFY))
                        event.setCancelled(true);
                }
            }
        }
        else if(entity instanceof Cow)
        {
            if(hand.isPresent())
            {
                Material item = hand.get().getType();
                EntityType type = entity.getType();
                if(item == Material.BUCKET && type == EntityType.COW ||
                        item == Material.BOWL && type == EntityType.MUSHROOM_COW)
                {
                    if(check(entity.getLocation(), player, PermissionFlag.HARVEST))
                        event.setCancelled(true);
                }
            }
        }
        else if(entity instanceof Pig)
        {
            Pig pig = (Pig) entity;
            if(pig.isAdult())
            {
                if(!pig.hasSaddle())
                {
                    if(hand.map(ItemStack::getType).filter(Material.SADDLE::equals).isPresent())
                    {
                        if(check(pig.getLocation(), player, PermissionFlag.MODIFY))
                            event.setCancelled(true);
                    }
                }
                else
                {
                    if(check(entity.getLocation(), player, PermissionFlag.ENTER, PermissionFlag.RIDE))
                        event.setCancelled(true);
                }
            }
        }
        else if(entity instanceof Vehicle)
        {
            if(check(entity.getLocation(), player, PermissionFlag.ENTER, PermissionFlag.RIDE))
                event.setCancelled(true);
        }
        else if(entity instanceof ItemFrame)
        {
            if(check(entity.getLocation(), player, PermissionFlag.OPEN))
                event.setCancelled(true);
        }
        else if(entity instanceof LeashHitch)
        {
            if(check(entity.getLocation(), player, PermissionFlag.MODIFY))
                event.setCancelled(true);
        }
        else if(entity.getType() == EntityType.ZOMBIE)
        {
            Zombie zombie = (Zombie) entity;
            if(hand.map(ItemStack::getType).filter(Material.GOLDEN_APPLE::equals).isPresent() &&
                    zombie.hasPotionEffect(PotionEffectType.WEAKNESS))
            {
                if(check(zombie.getLocation(), player, PermissionFlag.MODIFY))
                    event.setCancelled(true);
            }
        }
        else if(entity instanceof Tameable)
        {
            Tameable tameable = (Tameable) entity;
            if(!tameable.isTamed())
            {
                if(check(entity.getLocation(), player, PermissionFlag.MODIFY))
                    event.setCancelled(true);
            }
        }
        else if(entity instanceof EnderCrystal)
        {
            if(check(entity.getLocation(), player, PermissionFlag.MODIFY))
                event.setCancelled(true);
        }
        else if(entity instanceof LingeringPotion)
        {
            if(hand.map(ItemStack::getType).filter(Material.GLASS_BOTTLE::equals).isPresent())
            {
                if(check(entity.getLocation(), player, PermissionFlag.CLICK))
                    event.setCancelled(true);
            }
        }
        else if(entity instanceof Creeper)
        {
            if(hand.map(ItemStack::getType).filter(Material.FLINT_AND_STEEL::equals).isPresent())
            {
                if(check(entity.getLocation(), player, PVM))
                    event.setCancelled(true);
                else
                    explosionCreator.putIfAbsent(entity.getUniqueId(), player);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onHangingPlace(HangingPlaceEvent event)
    {
        Block block = event.getBlock();
        BlockFace placeFace = event.getBlockFace();
        Location location = block.getLocation().add(placeFace.getModX(), placeFace.getModY(), placeFace.getModZ());
        if(check(location, event.getPlayer(), MODIFY))
            event.setCancelled(true);
        else
        {
            Lazy<ClaimedChunk> lowestClaim = new Lazy<>(()-> plugin.mineCity.provideChunk(plugin.chunk(location)));
            plugin.plugin.getScheduler().runTask(plugin.plugin, ()->{
                location.add(0.5, 0.5, 0.5);
                location.getWorld().getNearbyEntities(location, 1, 1, 1).stream()
                        .filter(e-> e.getType() == EntityType.PAINTING).map(e-> (Painting) e)
                        .filter(p-> {
                            Art art = p.getArt();
                            return art.getBlockWidth() > 1 || art.getBlockHeight() > 1;
                        })
                        .filter(p->{
                            Art art = p.getArt();
                            BlockFace face = BukkitUtil.right(p.getAttachedFace());

                            ClaimedChunk ac = lowestClaim.get();
                            BlockPos a = plugin.blockPos(p.getLocation());

                            int width = art.getBlockWidth() -1;
                            BlockPos b = a.add(face.getModX() * width, art.getBlockHeight() -1, face.getModZ() * width);
                            ClaimedChunk bc = plugin.mineCity.provideChunk(b.getChunk(), ac);

                            return !ac.getFlagHolder(a).equals(bc.getFlagHolder(b));
                        })
                        .forEachOrdered(p->{
                            p.getWorld().dropItemNaturally(
                                    location,
                                    new ItemStack(Material.PAINTING));
                            p.remove();
                        });
            });
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onHangingBreakByEntity(HangingBreakByEntityEvent event)
    {
        Entity remover = event.getRemover();
        Hanging entity = event.getEntity();
        if(remover instanceof Projectile)
        {
            Object shooter = getShooter(((Projectile) remover).getShooter(), true);
            if(shooter instanceof Player)
                remover = (Entity) shooter;
            else if(shooter instanceof Identity)
            {
                if(check(entity.getLocation(), (Identity) shooter, MODIFY).isPresent())
                    event.setCancelled(true);
                return;
            }
        }

        // TODO Skeleton shooting, creeper, tnt, crystal explosions...
        if(!(remover instanceof Player))
        {
            event.setCancelled(true);
            return;
        }

        if(entity instanceof Painting)
        {
            Painting painting = (Painting) entity;
            Art art = painting.getArt();
            int width = art.getBlockWidth();
            int height = art.getBlockHeight();
            if(width > 1 || height > 1)
            {
                BlockFace face = BukkitUtil.right(painting.getAttachedFace());
                BlockPos a = plugin.blockPos(painting.getLocation());
                width -= 1;
                BlockPos b = a.add(face.getModX() * width, height -1, face.getModZ() * width);

                ClaimedChunk ac = plugin.mineCity.provideChunk(a.getChunk());
                ClaimedChunk bc = plugin.mineCity.provideChunk(b.getChunk(), ac);

                FlagHolder ah = ac.getFlagHolder(a);
                FlagHolder bh = bc.getFlagHolder(b);

                BukkitPlayer player = plugin.player((Player) remover);
                Optional<Message> ad = ah.can(player, MODIFY);
                if(ad.isPresent())
                {
                    Optional<Message> bd = bh.can(player, MODIFY);
                    if(bd.isPresent())
                    {
                        event.setCancelled(true);
                        Message denial = MineCity.RANDOM.nextBoolean()? bd.get() : ad.get();
                        player.send(FlagHolder.wrapDeny(denial));
                        return;
                    }
                }

                return;
            }
        }

        if(check(entity.getLocation(), (Player) remover, MODIFY))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityTargetEvent(EntityTargetEvent event)
    {
        Entity entity = event.getEntity();
        Entity target = event.getTarget();
        if(target instanceof Player)
        {
            BukkitPlayer player = plugin.player((Player) target);
            if(entity instanceof Animals)
            {
                if(player.lureDelay > 0)
                {
                    player.lureDelay--;
                    event.setCancelled(true);
                    return;
                }

                BlockPos entityPos = plugin.blockPos(entity.getLocation());
                ClaimedChunk entityChunk = plugin.mineCity.provideChunk(entityPos.getChunk());
                FlagHolder holder = entityChunk.getFlagHolder(entityPos);

                boolean named = entity.getCustomName() != null;
                Optional<Message> denial = holder.can(player, MODIFY);
                if(!named && denial.isPresent())
                    denial = holder.can(player, PVC);

                if(!denial.isPresent())
                {
                    BlockPos playerPos = plugin.blockPos(entityPos, player.sender.getLocation());
                    ClaimedChunk playerChunk = plugin.mineCity.provideChunk(playerPos.getChunk(), entityChunk);
                    FlagHolder playerHolder = playerChunk.getFlagHolder(playerPos);

                    if(!playerHolder.equals(holder))
                    {
                        denial = playerHolder.can(player, MODIFY);
                        if(!named && denial.isPresent())
                            denial = playerHolder.can(player, PVC);
                    }
                }

                if(denial.isPresent())
                {
                    event.setCancelled(true);
                    player.lureDelay = 40;
                    player.send(FlagHolder.wrapDeny(denial.get()));
                }
            }
            else if(entity instanceof Monster)
            {
                if(player.lureDelay > 0 && entity.getCustomName() != null)
                {
                    player.lureDelay--;
                    event.setCancelled(true);
                    return;
                }

                BlockPos mobPos = plugin.blockPos(entity.getLocation());
                ClaimedChunk mobChunk = plugin.mineCity.provideChunk(mobPos.getChunk());
                FlagHolder holder = mobChunk.getFlagHolder(mobPos);

                boolean named = entity.getCustomName() != null;
                Optional<Message> denial = holder.can(player, named? MODIFY : PVM);
                if(!denial.isPresent())
                {
                    BlockPos playerPos = plugin.blockPos(mobPos, player.sender.getLocation());
                    ClaimedChunk playerChunk = plugin.mineCity.provideChunk(playerPos.getChunk(), mobChunk);
                    FlagHolder playerHolder = playerChunk.getFlagHolder(playerPos);

                    if(!playerHolder.equals(holder))
                        denial = playerHolder.can(player, named? MODIFY : PVM);
                }

                if(denial.isPresent())
                {
                    event.setCancelled(true);
                    if(named)
                        player.lureDelay = 40;
                }
            }
            else if(entity.getCustomName() != null)
            {
                if(player.lureDelay > 0)
                {
                    player.lureDelay--;
                    event.setCancelled(true);
                    return;
                }

                BlockPos entPos = plugin.blockPos(entity.getLocation());
                ClaimedChunk entChunk = plugin.mineCity.provideChunk(entPos.getChunk());
                FlagHolder holder = entChunk.getFlagHolder(entPos);

                Optional<Message> denial = holder.can(player, MODIFY);
                if(!denial.isPresent())
                {
                    BlockPos playerPos = plugin.blockPos(entPos, player.sender.getLocation());
                    ClaimedChunk playerChunk = plugin.mineCity.provideChunk(playerPos.getChunk(), entChunk);
                    FlagHolder playerHolder = playerChunk.getFlagHolder(playerPos);

                    if(!playerHolder.equals(holder))
                        denial = playerHolder.can(player, MODIFY);
                }

                if(denial.isPresent())
                {
                    event.setCancelled(true);
                    player.lureDelay = 40;
                    player.send(FlagHolder.wrapDeny(denial.get()));
                }
            }
        }
    }

    public void allowToPickup(Item item, UUID playerId)
    {
        drops.computeIfAbsent(item.getUniqueId(), id-> new HashSet<>(1)).add(playerId);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event)
    {
        Item item = event.getItemDrop();
        BukkitPlayer player = plugin.player(event.getPlayer());
        Optional<Message> denial = canCollect(player, isHarvest(item.getItemStack()), item);
        if(denial.isPresent())
        {
            event.setCancelled(true);
            player.send(FlagHolder.wrapDeny(denial.get()));
        }
        else
        {
            allowToPickup(item, player.sender.getUniqueId());
            new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    if(item.isDead())
                    {
                        drops.remove(item.getUniqueId());
                        cancel();
                    }
                }
            }.runTaskTimer(plugin.plugin, 10*20, 5*20);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onItemMerge(ItemMergeEvent event)
    {
        Collection<UUID> a = drops.get(event.getEntity().getUniqueId());
        Collection<UUID> b = drops.get(event.getTarget().getUniqueId());
        if(!Objects.equals(a, b))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerFish(PlayerFishEvent event)
    {
        switch(event.getState())
        {
            case CAUGHT_FISH:
                allowToPickup((Item) event.getCaught(), event.getPlayer().getUniqueId());
                return;

            case CAUGHT_ENTITY:
            {
                Entity caught = event.getCaught();
                Player player = event.getPlayer();
                if(caught instanceof Item && player.equals(drops.get(caught.getUniqueId())))
                    return;

                if(caught instanceof Tameable && player.equals(((Tameable) caught).getOwner()))
                    return;

                if(caught instanceof Player)
                {
                    if(caught.equals(player))
                        return;

                    BlockPos from = plugin.blockPos(player.getLocation());
                    ClaimedChunk fromClaim = plugin.mineCity.provideChunk(from.getChunk());
                    FlagHolder fromHolder = fromClaim.getFlagHolder(from);
                    BukkitPlayer fromPlayer = plugin.player(player);

                    BlockPos to = plugin.blockPos(from, caught.getLocation());
                    ClaimedChunk toClaim = plugin.mineCity.provideChunk(to.getChunk(), fromClaim);
                    FlagHolder toHolder = toClaim.getFlagHolder(to);
                    BukkitPlayer toPlayer = plugin.player((Player) caught);

                    Optional<Message> denial;
                    if(toHolder.equals(fromHolder))
                        denial = optionalStream(
                                can(fromPlayer, PVP, fromHolder),
                                can(toPlayer, PVP, fromHolder)
                        ).findFirst();
                    else
                        denial = Stream.concat(
                                can(fromPlayer, PVP, fromHolder, toHolder),
                                can(toPlayer, PVP, fromHolder, toHolder)
                        ).findFirst();

                    if(denial.isPresent())
                    {
                        event.setCancelled(true);
                        fromPlayer.send(FlagHolder.wrapDeny(denial.get()));
                    }

                    return;
                }

                if(caught.getCustomName() != null)
                {
                    if(check(caught.getLocation(), player, MODIFY))
                        event.setCancelled(true);
                    return;
                }

                if(caught instanceof Animals)
                {
                    if(check(caught.getLocation(), player, PVC))
                        event.setCancelled(true);
                    return;
                }

                if(caught instanceof Monster)
                {
                    if(check(caught.getLocation(), player, PVM))
                        event.setCancelled(true);
                    return;
                }

                if(check(caught.getLocation(), player, MODIFY))
                    event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerPickupItemMonitor(PlayerPickupItemEvent event)
    {
        drops.remove(event.getItem().getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerPickupItem(PlayerPickupItemEvent event)
    {
        if(event instanceof PlayerPickupArrowEvent)
        {
            if(((PlayerPickupArrowEvent) event).getArrow().getShooter() == event.getPlayer())
                return;
        }

        Item item = event.getItem();
        Collection<UUID> allowedPlayers = drops.get(item.getUniqueId());
        Player entityPlayer = event.getPlayer();
        if(allowedPlayers != null && allowedPlayers.contains(entityPlayer.getUniqueId()))
            return;

        BukkitPlayer player = plugin.player(entityPlayer);
        boolean harvest = isHarvest(item.getItemStack());
        if(harvest)
        {
            if(player.pickupHarvestDelay > 0)
            {
                player.pickupHarvestDelay--;
                event.setCancelled(true);
                return;
            }
        }
        else
        {
            if(player.pickupRandomDelay > 0)
            {
                player.pickupRandomDelay--;
                event.setCancelled(true);
                return;
            }
        }

        Optional<Message> denial = canCollect(player, harvest, item);
        if(denial.isPresent())
        {
            event.setCancelled(true);
            player.send(FlagHolder.wrapDeny(denial.get()));
            if(harvest)
                player.pickupHarvestDelay = 40 ;
            else
                player.pickupRandomDelay = 40;
        }
    }

    public static boolean isHarvest(@NotNull ItemStack item)
    {
        switch(item.getType())
        {
            case EGG:
            case BEETROOT:
            case POTATO_ITEM:
            case POISONOUS_POTATO:
            case WHEAT:
            case CARROT_ITEM:
            case PUMPKIN:
            case MELON_BLOCK:
            case MELON:
            case SEEDS:
            case BEETROOT_SEEDS:
            case APPLE:
            case COCOA:
            case SUGAR_CANE:
            case CACTUS:
            case WOOL:
                return true;

            case INK_SACK:
                return item.getDurability() == 3;

            default:
                return false;
        }
    }

    public Optional<Message> canCollect(BukkitPlayer player, boolean harvest, Item item)
    {
        FlagHolder holder = plugin.getFlagHolder(item.getLocation());

        Optional<Message> denial;
        if(harvest)
        {
            denial = holder.can(player, HARVEST);
            if(!denial.isPresent())
                return denial;
        }
        else
        {
            denial = Optional.empty();
        }

        Optional<Message> pickup = holder.can(player, PICKUP);
        if(pickup.isPresent())
            return denial.isPresent()? denial : pickup;

        return pickup;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event)
    {
        BlockPos from = plugin.blockPos(event.getFrom());
        BlockPos to = plugin.blockPos(from, event.getTo());

        ClaimedChunk fromChunk = plugin.mineCity.provideChunk(from.getChunk());
        ClaimedChunk toChunk = plugin.mineCity.provideChunk(to.getChunk(), fromChunk);

        FlagHolder fromHolder = fromChunk.getFlagHolder(from);
        FlagHolder toHolder = toChunk.getFlagHolder(to);

        if(fromHolder.equals(toHolder))
            return;

        BukkitPlayer player = plugin.player(event.getPlayer());

        Optional<Message> denial = optionalStream(
                can(player, ENTER, toHolder),
                can(player, LEAVE, fromHolder)
        ).findFirst();

        if(denial.isPresent())
        {
            event.setCancelled(true);
            player.send(wrapDeny(denial.get()));
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event)
    {
        final Player player = event.getPlayer();
        final BukkitPlayer user = plugin.player(player);
        user.skipTick = 5;
        new BukkitRunnable()
        {
            Location initial;
            @Override
            public void run()
            {
                if(initial == null)
                {
                    initial = player.getLocation();
                    return;
                }

                if(!player.isOnline())
                {
                    cancel();
                    return;
                }

                if(player.getLocation().equals(initial))
                    user.skipTick = 5;
                else
                    cancel();
            }
        }.runTaskTimer(plugin.plugin, 1, 4);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamageByEntityMonitor(EntityDamageByEntityEvent event)
    {
        Entity entity = event.getEntity();
        if(!(entity instanceof LivingEntity))
            return;

        Entity attacker = event.getDamager();
        if(attacker instanceof Projectile)
        {
            ProjectileSource shooter = ((Projectile) attacker).getShooter();
            if(!(shooter instanceof Entity))
                return;
            attacker = (Entity) shooter;
        }

        if(attacker instanceof Tameable)
        {
            AnimalTamer owner = ((Tameable) attacker).getOwner();
            if(!(owner instanceof Entity))
                return;
            attacker = (Entity) owner;
        }

        if(attacker instanceof Player)
            watchAttack((LivingEntity) entity, (Player) attacker);
    }

    public void watchAttack(LivingEntity entity, Player attacker)
    {
        attackers.put(entity.getUniqueId(), attacker);
        new BukkitRunnable()
        {
            double lastHealth = entity.getHealth();
            boolean remove = false;

            @Override
            public void run()
            {
                if(remove)
                {
                    cancel();
                    attackers.remove(entity.getUniqueId(), attacker);
                }

                if(!entity.isValid())
                {
                    remove = true;
                    return;
                }

                double health = entity.getHealth();
                if(health < lastHealth)
                    lastHealth = health;
                else
                    remove = true;
            }
        }.runTaskTimer(plugin.plugin, 7*20, 7*20);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent event)
    {
        List<ItemStack> drops = event.getDrops();
        if(drops.isEmpty())
            return;

        LivingEntity entity = event.getEntity();
        Player attacker = attackers.get(entity.getUniqueId());
        if(attacker == null)
            return;

        collectDrops(attacker, entity.getLocation(), drops, 2);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event)
    {
        List<ItemStack> drops = event.getDrops();
        if(drops.isEmpty())
            return;

        Player player = event.getEntity();
        collectDrops(player, player.getLocation(), drops, 2);
    }

    public void consumeDrop(Location location, Material type, int ticks)
    {
        Function<Item, Boolean> capture = item ->
        {
            if(item.getWorld().equals(location.getWorld()) && item.getLocation().distance(location) <= 2)
            {
                ItemStack stack = item.getItemStack();
                int amount = stack.getAmount();
                if(stack.getType() == type && amount > 0)
                {
                    stack.setAmount(amount -1);
                    item.setItemStack(stack);

                    if(stack.getAmount() == 0)
                        item.remove();
                    return true;
                }
            }

            return false;
        };

        captureDrops.add(capture);
        plugin.scheduler.runTaskLater(plugin.plugin, ()-> captureDrops.remove(capture), ticks);
    }

    public void collectDrops(final Player player, final Location collectLoc, Collection<ItemStack> loot, int ticks)
    {
        final List<ItemStack> collectDrops = loot.stream().map(ItemStack::clone)
                .collect(Collectors.toCollection(ArrayList::new));

        Function<Item, Boolean> capture = item ->
        {
            if(item.getWorld().equals(collectLoc.getWorld()) && item.getLocation().distance(collectLoc) <= 2)
            {
                ItemStack dropped = item.getItemStack();
                int remaining = dropped.getAmount();
                for(Iterator<ItemStack> iter = collectDrops.iterator(); iter.hasNext(); )
                {
                    ItemStack need = iter.next();
                    if(dropped.isSimilar(need))
                    {
                        int needAmount = need.getAmount();
                        int take = Math.min(remaining, needAmount);
                        remaining -= take;
                        needAmount -= take;
                        need.setAmount(needAmount);
                        if(needAmount <= 0)
                            iter.remove();

                        if(remaining == 0)
                            break;
                    }
                }

                if(remaining != dropped.getAmount())
                    allowToPickup(item, player.getUniqueId());

                if(collectDrops.isEmpty())
                    return true;
            }

            return false;
        };

        captureDrops.add(capture);
        plugin.scheduler.runTaskLater(plugin.plugin, ()-> captureDrops.remove(capture), ticks);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemSpawnEvent(ItemSpawnEvent event)
    {
        Item item = event.getEntity();
        for(Iterator<Function<Item, Boolean>> iter = captureDrops.iterator(); iter.hasNext();)
            if(iter.next().apply(item))
                iter.remove();

        ItemStack stack = item.getItemStack();
        if(stack.getType() == Material.AIR || stack.getAmount() <= 0)
            event.setCancelled(true);
        else
        {
            FlagHolder holder = plugin.getFlagHolder(item.getLocation());
            Identity<?> owner = holder.owner();
            if(owner.getType() == Identity.Type.PLAYER)
                allowToPickup(item, (UUID) owner.uniqueId);
        }
    }

    public boolean isNegative(PotionEffectType type)
    {
        switch(type.getId())
        {
            case 2: //slow
            case 4: //slow digging
            case 7: //harm
            case 8: //jump -- sprint+jump is slower, allows creatures to jump fences and escape
            case 9: //confusion
            case 15: //blindness
            case 17: //hunger
            case 18: //weakness
            case 19: //poison
            case 20: //wither
            case 24: //glowing
            case 25: //levitation
            case 27: //unlucky
                return true;
            default:
                return false;
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPotionSplash(PotionSplashEvent event)
    {
        ThrownPotion potion = event.getPotion();

        Collection<LivingEntity> affected = event.getAffectedEntities();
        List<LivingEntity> negated = new ArrayList<>(affected.size());

        if(checkPotionEffects(true, potion.getShooter(),
                potion.getEffects().stream().map(PotionEffect::getType),
                affected, negated))
        {
            event.setCancelled(true);
        }

        negated.forEach(e-> event.setIntensity(e, 0));
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onAreaEffectCloudApply(AreaEffectCloudApplyEvent event)
    {
        AreaEffectCloud cloud = event.getEntity();
        Stream<PotionEffectType> effects = Stream.of(cloud.getBasePotionData().getType().getEffectType());
        if(cloud.hasCustomEffects())
            effects = Stream.concat(effects, cloud.getCustomEffects().stream().map(PotionEffect::getType));

        List<LivingEntity> affected = event.getAffectedEntities();
        List<LivingEntity> negated = new ArrayList<>(affected.size());

        if(checkPotionEffects(false, cloud.getSource(), effects, affected, negated))
            affected.clear();
        else
            affected.removeAll(negated);
    }

    public boolean checkPotionEffects(boolean verbose,
                                      ProjectileSource shooterEntity,
                                      Stream<PotionEffectType> effects,
                                      Collection<LivingEntity> entities,
                                      Collection<LivingEntity> negatedEntities
    )
    {
        if(entities.isEmpty())
            return false;

        boolean negative = effects.anyMatch(this::isNegative);

        Optional<Message> result = Optional.empty();
        Object shooter = getShooter(shooterEntity, true);
        if(shooter instanceof Player)
        {
            Player playerEntity = (Player) shooter;
            BukkitPlayer player = plugin.player((Player)shooter);
            BlockPos playerPos = plugin.blockPos(player.sender.getLocation());
            ClaimedChunk playerChunk = plugin.mineCity.provideChunk(playerPos.getChunk());
            FlagHolder playerHolder = playerChunk.getFlagHolder(playerPos);

            boolean allowedAnybody = false;
            for(LivingEntity entity : entities)
            {
                if(entity.equals(playerEntity))
                {
                    allowedAnybody = true;
                    continue;
                }

                if(entity instanceof Tameable)
                {
                    if(!negative)
                    {
                        allowedAnybody = true;
                        continue;
                    }

                    AnimalTamer owner = ((Tameable) entity).getOwner();
                    if(playerEntity.equals(owner))
                        continue;

                    if(entity instanceof Wolf)
                    {
                        if(owner instanceof Player)
                        {
                            Player playerOwner = (Player) owner;
                            BlockPos ownerPos = plugin.blockPos(playerPos, playerOwner.getLocation());
                            ClaimedChunk ownerChunk = plugin.mineCity.provideChunk(ownerPos.getChunk(), playerChunk);
                            FlagHolder ownerHolder = ownerChunk.getFlagHolder(ownerPos);
                            BukkitPlayer ownerUser = plugin.player(playerOwner);

                            Optional<Message> denial = optionalStream(
                                    can(player, PVP, ownerHolder),
                                    can(player, PVP, playerHolder),
                                    can(ownerUser, PVP, ownerHolder),
                                    can(ownerUser, PVP, playerHolder)
                            ).findFirst();

                            if(denial.isPresent())
                            {
                                result = denial;
                                negatedEntities.add(entity);
                            }
                            else
                            {
                                allowedAnybody = true;
                            }

                            continue;
                        }
                    }
                }

                BlockPos entityPos = plugin.blockPos(playerPos, entity.getLocation());
                ClaimedChunk entityChunk = plugin.mineCity.provideChunk(entityPos.getChunk(), playerChunk);
                FlagHolder entityHolder = entityChunk.getFlagHolder(entityPos);

                if(entity instanceof Ageable || entity instanceof Golem)
                {
                    if(!negative)
                    {
                        allowedAnybody = true;
                        continue;
                    }

                    Optional<Message> denial = entityHolder.can(player, MODIFY);
                    if(denial.isPresent() && entity.getCustomName() != null)
                        denial = entityHolder.can(player, PVC);

                    if(denial.isPresent())
                    {
                        result = denial;
                        negatedEntities.add(entity);
                    }
                    else
                    {
                        allowedAnybody = true;
                    }

                    continue;
                }

                if(entity instanceof Monster)
                {
                    Optional<Message> denial = optionalStream(
                            can(player, PVM, entityHolder),
                            can(player, PVM, playerHolder)
                    ).findFirst();

                    if(denial.isPresent())
                    {
                        result = denial;
                        negatedEntities.add(entity);
                    }
                    else
                    {
                        allowedAnybody = true;
                    }

                    continue;
                }

                if(entity instanceof Player)
                {
                    if(!negative)
                    {
                        allowedAnybody = true;
                        continue;
                    }

                    BukkitPlayer otherUser = plugin.player((Player) entity);

                    Optional<Message> denial = optionalStream(
                            can(player, PVP, entityHolder),
                            can(player, PVP, playerHolder),
                            can(otherUser, PVP, entityHolder),
                            can(otherUser, PVP, playerHolder)
                    ).findFirst();

                    if(denial.isPresent())
                    {
                        result = denial;
                        negatedEntities.add(entity);
                    }
                    else
                    {
                        allowedAnybody = true;
                    }

                    continue;
                }

                Optional<Message> denial = entityHolder.can(player, MODIFY);

                if(denial.isPresent())
                {
                    result = denial;
                    negatedEntities.add(entity);
                }
                else
                {
                    allowedAnybody = true;
                }
            }

            if(!allowedAnybody)
            {
                if(verbose)
                    player.send(FlagHolder.wrapDeny(result.orElse(FlagHolder.DEFAULT_DENIAL_MESSAGE)));
                return true;
            }

            return false;
        }
        else if(shooter instanceof Identity)
        {
            Identity player = (Identity) shooter;
            BlockPos basePos = plugin.blockPos(entities.iterator().next().getLocation());
            ClaimedChunk cache = plugin.mineCity.provideChunk(basePos.getChunk());

            boolean allowedAnybody = false;
            for(LivingEntity entity : entities)
            {
                if(entity instanceof Tameable)
                {
                    if(!negative)
                    {
                        allowedAnybody = true;
                        continue;
                    }

                    AnimalTamer owner = ((Tameable) entity).getOwner();
                    if(owner != null && owner.getUniqueId().equals(player.getUniqueId()))
                    {
                        allowedAnybody = true;
                        continue;
                    }

                    if(entity instanceof Wolf)
                    {
                        if(owner instanceof Player)
                        {
                            Player playerOwner = (Player) owner;
                            BlockPos ownerPos = plugin.blockPos(basePos, playerOwner.getLocation());
                            ClaimedChunk ownerChunk = plugin.mineCity.provideChunk(ownerPos.getChunk(), cache);
                            FlagHolder ownerHolder = ownerChunk.getFlagHolder(ownerPos);
                            BukkitPlayer ownerUser = plugin.player(playerOwner);

                            Optional<Message> denial = optionalStream(
                                    can(player, PVP, ownerHolder),
                                    can(ownerUser, PVP, ownerHolder)
                            ).findFirst();

                            if(denial.isPresent())
                                negatedEntities.add(entity);
                            else
                                allowedAnybody = true;

                            continue;
                        }
                    }
                }

                BlockPos entityPos = plugin.blockPos(basePos, entity.getLocation());
                ClaimedChunk entityChunk = plugin.mineCity.provideChunk(entityPos.getChunk(), cache);
                FlagHolder entityHolder = entityChunk.getFlagHolder(entityPos);

                if(entity instanceof Ageable || entity instanceof Golem)
                {
                    if(!negative)
                    {
                        allowedAnybody = true;
                        continue;
                    }

                    Optional<Message> denial = entityHolder.can(player, MODIFY);
                    if(denial.isPresent() && entity.getCustomName() != null)
                        denial = entityHolder.can(player, PVC);

                    if(denial.isPresent())
                        negatedEntities.add(entity);
                    else
                        allowedAnybody = true;

                    continue;
                }

                if(entity instanceof Monster)
                {
                    Optional<Message> denial = optionalStream(
                            can(player, PVM, entityHolder)
                    ).findFirst();

                    if(denial.isPresent())
                        negatedEntities.add(entity);
                    else
                        allowedAnybody = true;

                    continue;
                }

                if(entity instanceof Player)
                {
                    if(!negative)
                    {
                        allowedAnybody = true;
                        continue;
                    }

                    BukkitPlayer otherUser = plugin.player((Player) entity);

                    Optional<Message> denial = optionalStream(
                            can(player, PVP, entityHolder),
                            can(otherUser, PVP, entityHolder)
                    ).findFirst();

                    if(denial.isPresent())
                        negatedEntities.add(entity);
                    else
                        allowedAnybody = true;

                    continue;
                }

                Optional<Message> denial = entityHolder.can(player, MODIFY);

                if(denial.isPresent())
                    negatedEntities.add(entity);
                else
                    allowedAnybody = true;
            }

            return !allowedAnybody;
        }
        else if(shooter instanceof Entity)
        {
            BlockPos basePos = plugin.blockPos(((Entity) shooter).getLocation());
            ClaimedChunk baseClaim = plugin.mineCity.provideChunk(basePos.getChunk());
            FlagHolder baseHolder = baseClaim.getFlagHolder(basePos);
            boolean allowedAnybody = false;
            for(LivingEntity entity : entities)
            {
                if(entity instanceof Tameable)
                {
                    if(!negative)
                    {
                        allowedAnybody = true;
                        continue;
                    }

                    AnimalTamer owner = ((Tameable) entity).getOwner();

                    if(owner instanceof Player)
                    {
                        Player playerOwner = (Player) owner;
                        BlockPos ownerPos = plugin.blockPos(basePos, playerOwner.getLocation());
                        ClaimedChunk ownerChunk = plugin.mineCity.provideChunk(ownerPos.getChunk(), baseClaim);
                        FlagHolder ownerHolder = ownerChunk.getFlagHolder(ownerPos);
                        BukkitPlayer ownerUser = plugin.player(playerOwner);

                        Optional<Message> denial = optionalStream(
                                can(ownerUser, PVM, ownerHolder),
                                can(ownerUser, PVM, baseHolder)
                        ).findFirst();

                        if(denial.isPresent())
                            negatedEntities.add(entity);
                        else
                            allowedAnybody = true;

                        continue;
                    }
                }

                if(entity instanceof Player)
                {
                    if(!negative)
                    {
                        allowedAnybody = true;
                        continue;
                    }

                    Player playerOwner = (Player) entity;
                    BlockPos ownerPos = plugin.blockPos(basePos, playerOwner.getLocation());
                    ClaimedChunk ownerChunk = plugin.mineCity.provideChunk(ownerPos.getChunk(), baseClaim);
                    FlagHolder ownerHolder = ownerChunk.getFlagHolder(ownerPos);
                    BukkitPlayer ownerUser = plugin.player(playerOwner);

                    Optional<Message> denial = optionalStream(
                            can(ownerUser, PVM, ownerHolder),
                            can(ownerUser, PVM, baseHolder)
                    ).findFirst();

                    if(denial.isPresent())
                        negatedEntities.add(entity);
                    else
                        allowedAnybody = true;

                    continue;
                }

                allowedAnybody = true;
            }

            return !allowedAnybody;
        }
        else
        {
            return true;
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event)
    {
        InventoryHolder inventoryHolder = event.getInventory().getHolder();
        if(!(inventoryHolder instanceof Horse))
            return;

        HumanEntity human = event.getWhoClicked();
        Horse horse = (Horse) inventoryHolder;
        if(horse.equals(horse.getOwner()))
            return;

        Set<Integer> rawSlots = event.getRawSlots();
        InventoryView view = event.getView();
        int topSize = view.getTopInventory().getSize();
        boolean affectTop = false;
        for(int slot: rawSlots)
        {
            if(slot < topSize)
            {
                affectTop = true;
                break;
            }
        }

        if(!affectTop)
            return;

        if(human instanceof Player)
        {
            if(check(horse.getLocation(), (Player) human, MODIFY))
                event.setCancelled(true);
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event)
    {
        InventoryHolder inventoryHolder = event.getInventory().getHolder();
        if(!(inventoryHolder instanceof Horse))
            return;

        InventoryAction action = event.getAction();
        switch(action)
        {
            case NOTHING:
            case CLONE_STACK:
                return;
        }

        HumanEntity human = event.getWhoClicked();
        Horse horse = (Horse) inventoryHolder;
        if(horse.equals(horse.getOwner()))
            return;

        if(action != InventoryAction.MOVE_TO_OTHER_INVENTORY && action != InventoryAction.COLLECT_TO_CURSOR
                && event.getView().getBottomInventory().equals(event.getClickedInventory()))
        {
            return;
        }

        if(human instanceof Player)
        {
            if(check(horse.getLocation(), (Player) human, MODIFY))
                event.setCancelled(true);
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event)
    {
        List<Block> blocks = event.blockList();
        if(blocks.isEmpty())
            return;

        Entity entity = event.getEntity();
        Entity responsible = null;
        Location responsibleLoc = null;
        PlayerID responsibleId = null;
        if(entity instanceof TNTPrimed)
        {
            responsible = ((TNTPrimed) entity).getSource();
        }
        else if(entity instanceof Fireball)
        {
            Fireball fireball = (Fireball) entity;
            ProjectileSource shooter = fireball.getShooter();
            if(shooter instanceof Entity)
                responsible = (Entity) shooter;
            else if(shooter instanceof BlockProjectileSource)
                responsibleLoc = ((BlockProjectileSource) shooter).getBlock().getLocation();
        }
        else if(entity instanceof EnderCrystal || entity instanceof Creeper)
        {
            Object exploder = explosionCreator.remove(entity.getUniqueId());
            if(exploder instanceof Entity)
                responsible = (Entity) exploder;
            else if(exploder instanceof BlockProjectileSource)
                responsibleLoc = ((BlockProjectileSource) exploder).getBlock().getLocation();
            else if(exploder instanceof PlayerID)
                responsibleId = (PlayerID) exploder;
        }

        BlockPos entityPos = plugin.blockPos(entity.getLocation());
        ClaimedChunk entityClaim = plugin.mineCity.provideChunk(entityPos.getChunk());

        BiFunction<ClaimedChunk, BlockPos, Boolean> check = null;

        // A location were determined as responsible of this explosion
        if(responsibleLoc != null)
        {
            BlockPos responsiblePos = plugin.blockPos(responsibleLoc);
            ClaimedChunk chunk = plugin.mineCity.provideChunk(responsiblePos.getChunk(), entityClaim);

            // Natural explosions will be handled later
            if(!chunk.reserve && !(chunk.owner instanceof Nature))
            {
                FlagHolder holder = chunk.getFlagHolder(responsiblePos);
                Identity<?> owner = holder.owner();
                check = (claim, pos) -> !claim.getFlagHolder(pos).can(owner, MODIFY).isPresent();
            }
        }
        // A player were indirectly responsible by this explosion
        else if(responsibleId != null)
        {
            // Just check the permission
            PlayerID player = responsibleId;
            check = (claim, pos) -> !claim.getFlagHolder(pos).can(player, MODIFY).isPresent();
        }
        // An entity were responsible by this explosion,
        // can be ghast shooting fireballs, lucky skeleton that shoots an ender crystal
        // or a player who ignited a creeper or a tnt directly
        else if(responsible != null)
        {
            // Players, just check the permission
            if(responsible instanceof Player)
            {
                BukkitPlayer player = plugin.player((Player) responsible);
                check = (claim, pos) -> !claim.getFlagHolder(pos).can(player, MODIFY).isPresent();
            }
            // Other cases, blame the nature
        }

        // No check were defined yet? So... Blame the nature
        // Natural explosions can only change blocks in nature below height 40
        // It can't change blocks in protected zones
        if(check == null)
            check = (claim, pos) -> claim.owner instanceof Nature && pos.y < 40;


        for(Iterator<Block> iter = blocks.iterator(); iter.hasNext();)
        {
            Block block = iter.next();
            BlockPos blockPos = plugin.blockPos(entityPos, block);
            ClaimedChunk blockClaim = plugin.mineCity.provideChunk(blockPos.getChunk(), entityClaim);

            if(!check.apply(blockClaim, blockPos))
                iter.remove();
        }
    }

    /**
     * BlockExplodeEvent is fired when an explosion happens with an unknown case
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event)
    {
        List<Block> blocks = event.blockList();
        if(blocks.isEmpty())
            return;

        /// This position will be used mainly for cache, it will decrease the computation needed in the iteration
        // because they will share the same ChunkPos instance when possible
        BlockPos pos = plugin.blockPos(blocks.get(0));

        // Also mainly used for cache, this will be reused when possible
        ClaimedChunk chunk = plugin.mineCity.provideChunk(pos.getChunk());

        for(Iterator<Block> iter = blocks.iterator(); iter.hasNext();)
        {
            Block block = iter.next();
            BlockPos blockPos = plugin.blockPos(pos, block);
            ClaimedChunk claim = plugin.mineCity.provideChunk(blockPos.getChunk(), chunk);

            if(claim.reserve || claim.owner instanceof Nature)
            {
                // Natural explosions can only change blocks in nature below height 40
                if(blockPos.y >= 40)
                    iter.remove();
            }
            else
            {
                // Natural explosions can't change blocks in protected zones
                iter.remove();
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerLeashEntity(PlayerLeashEntityEvent event)
    {
        if(check(event.getEntity().getLocation(), event.getPlayer(), MODIFY))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerUnleashEntity(PlayerUnleashEntityEvent event)
    {
        if(check(event.getEntity().getLocation(), event.getPlayer(), MODIFY))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerLeashEntityMonitor(PlayerLeashEntityEvent event)
    {
        BukkitPlayer player = plugin.player(event.getPlayer());
        LivingEntity entity = (LivingEntity) event.getEntity();
        new LeashedEntityMonitor(plugin, player, entity).runTaskTimer(plugin.plugin, 1, 1);
        player.leashedEntities.add(entity);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerUnleashEntityMonitor(PlayerUnleashEntityEvent event)
    {
        //noinspection SuspiciousMethodCalls
        plugin.player(event.getPlayer()).leashedEntities.remove(event.getEntity());
    }
}
