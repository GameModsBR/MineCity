package br.com.gamemods.minecity.bukkit;

import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.FlagHolder;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.bukkit.command.BukkitPlayer;
import br.com.gamemods.minecity.structure.ClaimedChunk;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static br.com.gamemods.minecity.api.CollectionUtil.optionalStream;
import static br.com.gamemods.minecity.api.permission.FlagHolder.can;

public class VanillaProtections implements Listener
{
    @NotNull
    private final MineCityBukkit plugin;

    public VanillaProtections(@NotNull MineCityBukkit plugin)
    {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event)
    {
        Entity entity = event.getRightClicked();
        if(entity instanceof Vehicle && (!(entity instanceof Pig) || ((Pig) entity).hasSaddle()))
        {
            Location location = entity.getLocation();
            ClaimedChunk chunk = plugin.mineCity.provideChunk(plugin.chunk(location));
            FlagHolder holder = chunk.getFlagHolder(location.getBlockX(), location.getBlockY(),
                    location.getBlockZ()
            );

            BukkitPlayer player = plugin.player(event.getPlayer());

            Optional<Message> denial = optionalStream(
                    can(player, PermissionFlag.ENTER, holder),
                    can(player, PermissionFlag.RIDE, holder)
            ).findFirst();

            if(denial.isPresent())
            {
                event.setCancelled(true);
                player.send(FlagHolder.wrapDeny(denial.get()));
                plugin.callSyncMethod(entity::eject);

                if(entity instanceof Horse)
                {
                    if(player.sender.equals(((Horse) entity).getOwner()))
                        entity.teleport(player.sender);
                }
            }
        }
    }
}
