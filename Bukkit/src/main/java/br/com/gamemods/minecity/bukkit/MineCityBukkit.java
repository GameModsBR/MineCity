package br.com.gamemods.minecity.bukkit;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.MineCityConfig;
import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.Server;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.bukkit.command.BukkitCommandSender;
import br.com.gamemods.minecity.bukkit.command.BukkitLocatableSender;
import br.com.gamemods.minecity.bukkit.command.BukkitPlayer;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.Optional;
import java.util.logging.Logger;

public class MineCityBukkit implements Server
{
    public final MineCity mineCity;
    public final BukkitScheduler scheduler;
    public final MineCityPlugin plugin;
    public Logger logger;

    public MineCityBukkit(MineCityPlugin plugin, MineCityConfig config)
    {
        this.plugin = plugin;
        logger = plugin.getLogger();
        scheduler = plugin.getScheduler();

        mineCity = new MineCity(this, config);
        PluginManager pluginManager = plugin.getPluginManager();
        pluginManager.registerEvents(new WorldListener(this), plugin);
    }

    public boolean onCommand(CommandSender sender, String label, String[] args)
    {
        String[] path = new String[args.length+1];
        path[0] = label;
        System.arraycopy(args, 0, path, 1, args.length);
        mineCity.commands.invoke(sender(sender), path);
        return true;
    }

    private br.com.gamemods.minecity.api.command.CommandSender sender(CommandSender sender)
    {
        if(sender instanceof Player)
            return new BukkitPlayer(this, (Player) sender);
        if(sender instanceof Entity || sender instanceof BlockCommandSender)
            return new BukkitLocatableSender<>(this, sender);
        return new BukkitCommandSender<>(this, sender);
    }

    public WorldDim world(World world)
    {
        //noinspection deprecation
        return new WorldDim(world.getEnvironment().getId(), world.getName());
    }

    public Optional<World> world(WorldDim world)
    {
        return Optional.ofNullable(plugin.getServer().getWorld(world.dir));
    }

    public ChunkPos chunk(Chunk chunk)
    {
        return new ChunkPos(world(chunk.getWorld()), chunk.getX(), chunk.getX());
    }

    public BlockPos blockPos(Location location)
    {
        return new BlockPos(world(location.getWorld()), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public Optional<Location> location(BlockPos pos)
    {
        return world(pos.world).map(world-> new Location(world, pos.x+0.5, pos.y+0.5, pos.z+0.5));
    }

    @SuppressWarnings("deprecation")
    @Override
    public Optional<PlayerID> getPlayerId(String name)
    {
        Player player = plugin.getServer().getPlayer(name);
        if(player == null)
            return Optional.empty();
        return Optional.of(new PlayerID(player.getUniqueId(), player.getName()));
    }
}
