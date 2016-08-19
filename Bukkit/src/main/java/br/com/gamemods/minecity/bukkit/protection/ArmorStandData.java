package br.com.gamemods.minecity.bukkit.protection;

import br.com.gamemods.minecity.api.permission.FlagHolder;
import br.com.gamemods.minecity.api.permission.Identity;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.bukkit.MineCityBukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.stream.Stream;

public class ArmorStandData
{
    public static final String KEY = "ArmorStandData";
    private final ArmorStand armorStand;
    private MineCityBukkit bukkit;
    private FlagHolder home;
    private BukkitTask timer;
    private BlockPos lastPos;

    public ArmorStandData(@NotNull MineCityBukkit bukkit, ArmorStand armorStand)
    {
        this.lastPos = bukkit.blockPos(armorStand.getLocation());
        this.home = bukkit.mineCity.provideChunk(lastPos.getChunk()).getFlagHolder(lastPos);
        this.armorStand = armorStand;
        this.bukkit = bukkit;
        timer = bukkit.scheduler.runTaskTimer(bukkit.plugin, this::tick, 20, 20);
    }

    public void tick()
    {
        if(!armorStand.isValid())
        {
            timer.cancel();
            return;
        }

        Location location = armorStand.getLocation();
        if(lastPos.x == location.getBlockX() && lastPos.y == location.getBlockY() && lastPos.z == location.getBlockZ())
            return;

        BlockPos newPos = bukkit.blockPos(lastPos, location);
        FlagHolder newHome = bukkit.mineCity.provideChunk(newPos.getChunk()).getFlagHolder(newPos);
        Identity<?> oldOwner = home.owner();
        if(newHome.equals(home) || !newHome.can(oldOwner, PermissionFlag.MODIFY).isPresent())
            home = newHome;
        else
        {
            World world = location.getWorld();
            Stream<Item> stream = Stream.of(
                        armorStand.getHelmet(), armorStand.getChestplate(), armorStand.getLeggings(),
                        armorStand.getBoots(), armorStand.getItemInHand(), new ItemStack(Material.ARMOR_STAND)
                    )
                    .filter(stack -> stack != null && stack.getType() != Material.AIR)
                    .map(stack -> world.dropItemNaturally(location, stack));

            if(oldOwner.getType() == Identity.Type.PLAYER)
                stream.forEachOrdered(item-> bukkit.entityProtections.allowToPickup(item, (UUID) oldOwner.uniqueId));
            else
                stream.forEachOrdered(item -> {});

            armorStand.remove();
            timer.cancel();
        }
    }
}
