package br.com.gamemods.minecity.bukkit;

import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.FlagHolder;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.bukkit.command.BukkitPlayer;
import br.com.gamemods.minecity.structure.ClaimedChunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.entity.minecart.PoweredMinecart;
import org.bukkit.entity.minecart.RideableMinecart;
import org.bukkit.entity.minecart.SpawnerMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;

import static br.com.gamemods.minecity.api.CollectionUtil.optionalStream;
import static br.com.gamemods.minecity.api.permission.FlagHolder.can;
import static br.com.gamemods.minecity.api.permission.FlagHolder.wrapDeny;
import static br.com.gamemods.minecity.bukkit.BukkitUtil.optional;

public class VanillaProtections implements Listener
{
    @NotNull
    private final MineCityBukkit plugin;

    public VanillaProtections(@NotNull MineCityBukkit plugin)
    {
        this.plugin = plugin;
    }

    private boolean check(Location location, Player player, PermissionFlag... flags)
    {
        BlockPos blockPos = plugin.blockPos(location);
        ClaimedChunk chunk = plugin.mineCity.provideChunk(blockPos.getChunk());
        FlagHolder holder = chunk.getFlagHolder(blockPos);

        BukkitPlayer user = plugin.player(player);
        Optional<Message> denial;
        if(flags.length == 1)
            denial = holder.can(user, flags[0]);
        else
        {
            //noinspection unchecked
            Supplier<Optional<Message>>[] array = Arrays.stream(flags).map(flag -> can(user, flag, holder)).toArray(Supplier[]::new);
            denial = optionalStream(array).findFirst();
        }

        if(denial.isPresent())
        {
            user.send(wrapDeny(denial.get()));
            return true;
        }

        return false;
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

            return;
        }
        else if(entity instanceof Horse)
        {
            Horse horse = (Horse) entity;
            if(horse.isAdult())
            {
                Material m = hand.map(ItemStack::getType).orElse(Material.AIR);
                boolean modifying;
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

                    default:
                        modifying = false;
                }

                if(modifying)
                {
                    if(!player.equals(horse.getOwner()))
                    {
                        if(check(horse.getLocation(), player, PermissionFlag.MODIFY))
                            event.setCancelled(true);
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

            return;
        }
        else if(entity instanceof Minecart)
        {
            if(entity instanceof RideableMinecart)
            {
                if(check(entity.getLocation(), player, PermissionFlag.ENTER, PermissionFlag.RIDE))
                    event.setCancelled(true);

                return;
            }
            else if(entity instanceof InventoryHolder)
            {
                if(check(entity.getLocation(), player, PermissionFlag.OPEN))
                    event.setCancelled(true);

                return;
            }
            else if(entity instanceof PoweredMinecart)
            {
                if(check(entity.getLocation(), player, PermissionFlag.CLICK))
                    event.setCancelled(true);

                return;
            }
            else if(entity instanceof ExplosiveMinecart)
            {
                if(hand.map(ItemStack::getType).filter(Material.FLINT_AND_STEEL::equals).isPresent())
                {
                    if(check(entity.getLocation(), player, PermissionFlag.MODIFY))
                        event.setCancelled(true);
                }

                return;
            }
            else if(entity instanceof SpawnerMinecart)
                return;
        }
        else if(entity instanceof Cow)
        {
            if(hand.isPresent())
            {
                Material item = hand.get().getType();
                EntityType type = entity.getType();
                if(item == Material.BUCKET && type == EntityType.COW || item == Material.BOWL && type == EntityType.MUSHROOM_COW)
                {
                    if(check(entity.getLocation(), player, PermissionFlag.HARVEST))
                        event.setCancelled(true);
                }
            }

            return;
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

            return;
        }
        else if(entity instanceof Vehicle)
        {
            if(check(entity.getLocation(), player, PermissionFlag.ENTER, PermissionFlag.RIDE))
                event.setCancelled(true);

            return;
        }
    }
}
