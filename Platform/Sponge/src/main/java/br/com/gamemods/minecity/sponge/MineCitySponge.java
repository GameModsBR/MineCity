package br.com.gamemods.minecity.sponge;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.MineCityConfig;
import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.Server;
import br.com.gamemods.minecity.api.permission.EntityID;
import br.com.gamemods.minecity.api.permission.Identity;
import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.api.world.EntityPos;
import br.com.gamemods.minecity.api.world.MinecraftEntity;
import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.sponge.cmd.*;
import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.source.ProxySource;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.Locatable;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class MineCitySponge implements Server
{
    public final MineCitySpongePlugin plugin;
    public final SpongeTransformer transformer;
    public final MineCity mineCity;
    public final Logger logger;
    public final SpongeExecutorService syncScheduler;
    public final Locale locale;
    public final ExecutorService loadingTasks;
    private final Map<World, WorldDim> worldMap = new HashMap<>(3);

    public MineCitySponge(MineCitySpongePlugin plugin, MineCityConfig config, SpongeTransformer transformer, Logger logger)
    {
        this.plugin = plugin;
        this.transformer = transformer;
        this.logger = logger;
        this.locale = config.locale;
        this.syncScheduler = Sponge.getScheduler().createSyncExecutor(plugin);
        this.loadingTasks = Executors.newSingleThreadExecutor(runnable-> {
            Thread thread = new Thread(runnable);
            thread.setName("MineCity-LoadingTasks");
            return thread;
        });

        mineCity = new MineCity(this, config, transformer);
    }

    public ChunkPos chunk(Chunk chunk)
    {
        Vector3i p = chunk.getPosition();
        ChunkPos pos = new ChunkPos(world(chunk.getWorld()), p.getX(), p.getZ());
        pos.instance = chunk;
        return pos;
    }

    public WorldDim world(World world)
    {
        return worldMap.computeIfAbsent(world, w ->
        {
            int dimId = -9000;
            Matcher matcher = Pattern.compile("DimensionId=(-?[0-9]+)").matcher(w.toString());
            if(matcher.find())
            {
                try
                {
                    dimId = Integer.parseInt(matcher.group(1));
                }
                catch(NumberFormatException ignored)
                {
                }
            }

            WorldDim dim = new WorldDim(w, dimId, w.getUniqueId().toString());
            dim.name = w.getName();
            return dim;
        });
    }

    public Optional<World> world(WorldDim dim)
    {
        if(dim.instance != null && dim.instance instanceof World)
            return Optional.of((World) dim.instance);

        UUID uuid;
        try
        {
            uuid = UUID.fromString(dim.dir);
        }
        catch(IllegalArgumentException e)
        {
            return Optional.empty();
        }

        return Sponge.getServer().getWorld(uuid);
    }

    private CommandSource original(CommandSource source)
    {
        CommandSource original = source;
        while (original instanceof ProxySource)
            original = ((ProxySource) original).getOriginalSource();
        return original;
    }

    public SpongeCommandSource<?> sender(CommandSource source)
    {
        CommandSource original = original(source);
        if(original instanceof Player)
            return new PlayerSender(this, (Player) original);
        else if(original instanceof Living)
            return new LivingSource<>(this, source, (Living) source);
        else if(original instanceof Entity)
            return new EntitySource<>(this, source, (Entity) source);
        else if(original instanceof Locatable)
            return new LocatableSource<>(this, source, (Locatable) source);
        return new SpongeCommandSource<>(this, source);
    }

    public Identity<UUID> identity(Entity entity)
    {
        if(entity instanceof Player)
            return identity((Player) entity);
        return new EntityID(type(entity), entity.getUniqueId(), entity.getTranslation().get(locale));
    }

    public MinecraftEntity.Type type(Entity entity)
    {
        return MinecraftEntity.Type.UNCLASSIFIED;
    }

    public PlayerID identity(Player player)
    {
        return PlayerID.get(player.getUniqueId(), player.getName());
    }

    @Override
    public MineCity getMineCity()
    {
        return mineCity;
    }

    @Override
    public Optional<PlayerID> getPlayerId(String name)
    {
        return Sponge.getServer().getPlayer(name).map(this::identity);
    }

    @Override
    public Stream<PlayerID> getOnlinePlayers()
    {
        return Sponge.getServer().getOnlinePlayers().stream().map(this::identity);
    }

    @Override
    public Stream<String> getOnlinePlayerNames()
    {
        return Sponge.getServer().getOnlinePlayers().stream().map(Player::getName);
    }

    @Override
    public void runAsynchronously(Runnable runnable)
    {
        Sponge.getScheduler().createTaskBuilder().async().execute(runnable).submit(plugin);
    }

    @Override
    public <R> Future<R> callSyncMethod(Callable<R> callable)
    {
        return syncScheduler.submit(callable);
    }

    @Override
    public <R> Future<R> callSyncMethod(Runnable runnable, R result)
    {
        return syncScheduler.submit(runnable, result);
    }

    @Override
    public Future<Void> callSyncMethod(Runnable runnable)
    {
        return syncScheduler.submit(runnable, null);
    }

    public EntityPos entityPos(Location<World> location)
    {
        return new EntityPos(world(location.getExtent()), location.getX(), location.getY(), location.getZ());
    }

    public EntityPos entityPos(Entity entity)
    {
        Vector3d rotation = entity.getRotation();
        Location<World> location = entity.getLocation();
        return new EntityPos(world(location.getExtent()),
                location.getX(), location.getY(), location.getZ(),
                (float) rotation.getX(), (float) rotation.getY()
        );
    }
}
