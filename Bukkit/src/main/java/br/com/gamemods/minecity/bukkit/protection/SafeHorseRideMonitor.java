package br.com.gamemods.minecity.bukkit.protection;

import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.FlagHolder;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.bukkit.BukkitUtil;
import br.com.gamemods.minecity.bukkit.MineCityBukkit;
import br.com.gamemods.minecity.bukkit.command.BukkitPlayer;
import br.com.gamemods.minecity.structure.City;
import br.com.gamemods.minecity.structure.ClaimedChunk;
import br.com.gamemods.minecity.structure.Nature;
import br.com.gamemods.minecity.structure.Plot;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@SuppressWarnings("deprecation")
public class SafeHorseRideMonitor extends BukkitRunnable implements MovementListener
{
    public static final String KEY = "SafeHorse";
    private final MineCityBukkit bukkit;
    public final BukkitPlayer player;
    private final MovementMonitor mov;
    public final Horse horse;
    private double lastHealth;
    private byte unregisterTimer = Byte.MIN_VALUE;
    private boolean enableFirstCheck = false;
    private boolean teleported;
    private boolean cancelled;

    public SafeHorseRideMonitor(MineCityBukkit plugin, BukkitPlayer player, Horse horse)
    {
        this.bukkit = plugin;
        this.player = player;
        this.lastHealth = horse.getHealth();
        this.horse = horse;
        this.mov = new MovementMonitor(plugin, horse, this);
        horse.setMetadata(KEY, new FixedMetadataValue(plugin.plugin, this));
    }

    @Override
    public void cancel() throws IllegalStateException
    {
        super.cancel();
        cancelled = true;
    }

    public boolean isCancelled()
    {
        return cancelled;
    }

    @Override
    public void run()
    {
        Entity passenger = horse.getPassenger();
        if(!horse.isValid() || passenger != null && !passenger.equals(player.sender) || BukkitUtil.getMeta(bukkit.plugin, KEY, horse) != this)
        {
            cancel();
            return;
        }

        double health = horse.getHealth();
        if(health < lastHealth)
            horse.setHealth(lastHealth);
        else if(health > lastHealth)
            lastHealth = health;

        boolean safe = true;
        Block block = horse.getLocation().getBlock();
        search:
        for(int ix = -1; ix <= 1; ix++)
            for(int iz = -1; iz <= 1; iz++)
                for(int iy = -1; iy <= 0; iy++)
                {
                    switch(block.getRelative(ix, iy, iz).getType())
                    {
                        case LAVA:
                        case STATIONARY_LAVA:
                        case WATER:
                        case STATIONARY_WATER:
                        case FIRE:
                        case CACTUS:
                        case MAGMA:
                        {
                            if(enableFirstCheck)
                            {
                                teleportBack();
                                return;
                            }

                            safe = false;
                            break search;
                        }
                    }
                }

        if(safe)
        {
            enableFirstCheck = true;
            teleported = false;
        }

        if(unregisterTimer == Byte.MIN_VALUE + 1)
        {
            cancel();
            return;
        }

        if(passenger == null && unregisterTimer == Byte.MIN_VALUE)
        {
            unregisterTimer = Byte.MAX_VALUE;
            teleportBack();
        }

        int previousY = mov.lastY;
        Optional<Message> message = mov.checkPosition(horse.getLocation());
        if(unregisterTimer > Byte.MIN_VALUE)
        {
            if(!message.isPresent())
            {
                if(previousY == mov.lastY)
                    unregisterTimer--;
                else if(unregisterTimer < 126)
                    unregisterTimer += 2;
            }
            else
            {
                unregisterTimer = 127;
                teleportBack();
            }

            return;
        }

        if(message.isPresent())
            teleportBack();
    }

    public void teleportBack()
    {
        if(!teleported)
        {
            horse.eject();
            Location current = horse.getLocation();
            Location loc = mov.lastLocation();
            loc.setPitch(current.getPitch());
            loc.setYaw(current.getYaw());
            horse.teleport(loc);
        }
        horse.setFireTicks(0);
        horse.setRemainingAir(horse.getMaximumAir());
        teleported = true;
    }

    @Override
    public boolean isSafeToStep(Entity entity, Block block)
    {
        BlockPos pos = bukkit.blockPos(block);
        ClaimedChunk claim = bukkit.mineCity.provideChunk(pos.getChunk());
        FlagHolder holder = claim.getFlagHolder(pos);

        for(int ix = -1; ix <= 1; ix++)
            for(int iz = -1; iz <= 1; iz++)
            {
                Block relative = block.getRelative(ix, 0, iz);
                BlockPos relPos = pos.add(ix, 0, iz);
                if(!bukkit.mineCity.provideChunk(relPos.getChunk(), claim).getFlagHolder(relPos).equals(holder))
                    return false;

                switch(relative.getType())
                {
                    case LAVA:
                    case WATER:
                    case STATIONARY_LAVA:
                    case STATIONARY_WATER:
                    case MAGMA:
                        return false;
                    default:
                        if(!MovementListener.super.isSafeToStep(entity, block))
                            return false;
                }
            }
        return true;
    }

    @Override
    public Optional<Message> onCityChange(@NotNull City city, @Nullable Plot plot)
    {
        teleported = false;
        if(unregisterTimer > Byte.MIN_VALUE)
            return Optional.of(new Message("Don't leave the last location!"));
        return player.onCityChange(city, plot);
    }

    @Override
    public Optional<Message> onPlotEnter(@NotNull Plot plot)
    {
        teleported = false;
        if(unregisterTimer > Byte.MIN_VALUE)
            return Optional.of(new Message("Don't leave the last location!"));
        return player.onPlotEnter(plot);
    }

    @Override
    public Optional<Message> onPlotLeave(@NotNull City city)
    {
        teleported = false;
        if(unregisterTimer > Byte.MIN_VALUE)
            return Optional.of(new Message("Don't leave the last location!"));
        return player.onPlotLeave(city);
    }

    @Override
    public Optional<Message> onCityLeave(@NotNull Nature nature)
    {
        teleported = false;
        if(unregisterTimer > Byte.MIN_VALUE)
            return Optional.of(new Message("Don't leave the last location!"));
        return player.onCityLeave(nature);
    }

    @Override
    public Optional<Message> onNatureChange(@NotNull Nature nature)
    {
        teleported = false;
        if(unregisterTimer > Byte.MIN_VALUE)
            return Optional.of(new Message("Don't leave the last location!"));
        return player.onNatureChange(nature);
    }
}
