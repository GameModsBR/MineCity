package br.com.gamemods.minecity.bukkit.protection;

import br.com.gamemods.minecity.api.CacheMap;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.FlagHolder;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.bukkit.BukkitUtil;
import br.com.gamemods.minecity.bukkit.MineCityBukkit;
import br.com.gamemods.minecity.structure.City;
import br.com.gamemods.minecity.structure.Nature;
import br.com.gamemods.minecity.structure.Plot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("serial")
public class EntityMonitor extends BukkitRunnable implements MovementListener
{
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static final Optional<Message> COMMON_DENIAL = Optional.of(new Message("Entity movement denied"));
    public static final String KEY = "EntityMonitor";
    private final MineCityBukkit bukkit;
    private final Entity entity;
    private final MovementMonitor mov;
    private final Location location;
    private final Location valid;
    private final Location previous;
    private final Location safe;
    private final LinkedHashMap<BlockPos, Optional<Message>> cache = new CacheMap<>(5);

    @Nullable
    public Entity target;

    public EntityMonitor(@NotNull MineCityBukkit bukkit, @NotNull Entity entity)
    {
        this.bukkit = bukkit;
        this.mov = new MovementMonitor(bukkit, entity, this);
        this.entity = entity;
        this.location = entity.getLocation();
        this.valid = location.clone();
        this.previous = valid.clone();
        this.safe = previous.clone();
        runTaskTimer(bukkit.plugin, 1, 1);
    }

    @Override
    public void run()
    {
        if(!entity.isValid())
        {
            cancel();
            return;
        }

        if(checkPosition().isPresent())
            teleportBack();
    }

    public void teleportBack()
    {
        entity.teleport(safe);
        BukkitUtil.setLocation(safe, previous);
        BukkitUtil.setLocation(safe, valid);
    }

    public Optional<Message> checkPosition()
    {
        Location location = entity.getLocation(this.location);
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        for(Map.Entry<BlockPos, Optional<Message>> entry: cache.entrySet())
        {
            BlockPos pos = entry.getKey();
            if(pos.x == x && pos.y == y && pos.z == z)
                return entry.getValue();
        }

        Optional<Message> result = mov.checkPosition(location);
        cache.put(bukkit.blockPos(location), result);
        if(!result.isPresent())
        {
            BukkitUtil.setLocation(previous, safe);
            BukkitUtil.setLocation(valid, previous);
            BukkitUtil.setLocation(location, valid);
        }
        return result;
    }

    @SuppressWarnings("deprecation")
    private Optional<Message> checkChange()
    {
        if(entity instanceof LivingEntity)
        {
            LivingEntity living = (LivingEntity) entity;
            boolean allow = target instanceof Player;
            if(!allow && living.isLeashed())
            {
                Entity leashHolder = living.getLeashHolder();
                if(leashHolder != null && leashHolder instanceof HumanEntity)
                    allow = true;
            }

            if(!allow && entity instanceof Horse)
            {
                Horse horse = (Horse) entity;
                ItemStack saddle = horse.getInventory().getSaddle();
                if(saddle != null && saddle.getType() == Material.SADDLE)
                {
                    Entity passenger = horse.getPassenger();
                    if(passenger != null && passenger instanceof HumanEntity)
                        allow = true;
                }
            }

            if(allow)
            {
                BukkitUtil.setLocation(location, safe);
                BukkitUtil.setLocation(location, previous);
                BukkitUtil.setLocation(location, valid);
                return Optional.empty();
            }
        }

        return COMMON_DENIAL;
    }

    @Override
    public Optional<Message> onCityChange(@NotNull City city, @Nullable Plot plot)
    {
        FlagHolder last = mov.lastHolder();
        if(last instanceof Nature)
            return Optional.empty();
        return checkChange();
    }

    @Override
    public Optional<Message> onPlotEnter(@NotNull Plot plot)
    {
        return checkChange();
    }

    @Override
    public Optional<Message> onPlotLeave(@NotNull City city)
    {
        return checkChange();
    }

    @Override
    public Optional<Message> onCityLeave(@NotNull Nature nature)
    {
        return checkChange();
    }

    @Override
    public Optional<Message> onNatureChange(@NotNull Nature nature)
    {
        return Optional.empty();
    }
}
