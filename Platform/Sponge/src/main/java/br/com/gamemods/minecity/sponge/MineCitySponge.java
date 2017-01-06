package br.com.gamemods.minecity.sponge;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.MineCityConfig;
import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.Server;
import br.com.gamemods.minecity.api.command.CommandSender;
import br.com.gamemods.minecity.api.permission.EntityID;
import br.com.gamemods.minecity.api.permission.Identity;
import br.com.gamemods.minecity.api.permission.Permissible;
import br.com.gamemods.minecity.api.shape.PrecisePoint;
import br.com.gamemods.minecity.api.world.*;
import br.com.gamemods.minecity.reactive.ReactiveLayer;
import br.com.gamemods.minecity.reactive.game.block.ReactiveBlock;
import br.com.gamemods.minecity.reactive.game.block.data.BlockSnapshotData;
import br.com.gamemods.minecity.reactive.game.block.data.supplier.SupplierBlockSnapshotData;
import br.com.gamemods.minecity.reactive.game.item.ReactiveItemStack;
import br.com.gamemods.minecity.sponge.cmd.*;
import br.com.gamemods.minecity.sponge.core.mixed.MixedEntity;
import br.com.gamemods.minecity.sponge.data.value.SpongeMinecraftEntity;
import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.source.ProxySource;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
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
import java.util.function.Supplier;
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

    public Cause cause()
    {
        return Cause.source(plugin.container).build();
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

    public Permissible permissible(Object subject)
    {
        if(subject instanceof CommandSource)
            return sender(subject, (CommandSource) subject);

        if(subject instanceof Entity)
            return sender(subject, null);

        if(subject instanceof SupplierBlockSnapshotData)
            subject = ((SupplierBlockSnapshotData) subject).getBlockSnapshotData();

        if(subject instanceof BlockSnapshot)
            subject = ReactiveLayer.getBlockSnapshotData(subject).get();

        if(subject instanceof BlockSnapshotData)
        {
            BlockSnapshotData data = (BlockSnapshotData) subject;
            BlockPos pos = data.getPosition();
            return mineCity.provideChunk(pos.getChunk()).getFlagHolder(pos).owner();
        }

        throw new UnsupportedOperationException("Unsupported subject: "+subject.getClass()+" "+subject);
    }

    public MinecraftEntity entity(Entity subject)
    {
        if(subject instanceof MixedEntity)
        {
            MixedEntity mix = (MixedEntity) subject;
            SpongeMinecraftEntity entity = mix.getMinecraftEntity();
            if(entity != null)
                return entity;
        }

        return entity(subject, ()-> (EntitySource) createSender(subject, subject instanceof CommandSource? (CommandSource)subject : null));
    }

    public MinecraftEntity entity(Entity subject, Supplier<EntitySource> source)
    {
        if(subject instanceof MixedEntity)
        {
            MixedEntity mix = (MixedEntity) subject;
            SpongeMinecraftEntity entity = mix.getMinecraftEntity();
            if(entity == null)
            {
                entity = new SpongeMinecraftEntity(this, subject, source.get());
                mix.setMinecraftEntity(entity);
            }

            return entity;
        }

        throw new UnsupportedOperationException("Missing MixinEntity");
    }

    public CommandSender sender(Object subject, CommandSource source)
    {
        if(subject instanceof Entity)
            return entity((Entity) subject, ()-> (EntitySource) createSender(subject, source)).getCommandSender();

        return createSender(subject, source);
    }

    private SpongeCommandSource<?,?> createSender(Object subject, CommandSource source)
    {
        if(subject instanceof Player)
            return new PlayerSender(this, (Player) subject);
        else if(subject instanceof Living)
            return new LivingSource<>(this, source, (Living) subject);
        else if(subject instanceof Entity)
            return new EntitySource<>(this, source, (Entity) subject);
        else if(subject instanceof Locatable)
            return new LocatableSource<>(this, source, (Locatable) subject);
        return new SpongeCommandSource<>(this, subject, source);
    }

    public Identity<UUID> identity(Entity entity)
    {
        if(entity instanceof Player)
            return identity((Player) entity);
        return new EntityID(type(entity), entity.getUniqueId(), entity.getTranslation().get(locale));
    }

    public MinecraftEntity.Type type(Entity entity)
    {
        return SpongeMinecraftEntity.type(entity.getType());
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

    public ReactiveItemStack reactiveStack(@Nullable ItemStack stack)
    {
        return new ReactiveItemStack(
                ReactiveLayer.getItemStackData(stack == null? ItemStack.of(ItemTypes.NONE, 1) : stack).get()
        );
    }

    public ReactiveBlock reactiveBlock(BlockSnapshot snapshot, @Nullable World world)
    {
        World blockWorld;
        if(world != null && snapshot.getWorldUniqueId().equals(world.getUniqueId()))
            blockWorld = world;
        else
            blockWorld = Sponge.getServer().getWorld(snapshot.getWorldUniqueId()).orElseThrow(()->
                    new IllegalStateException("The world "+snapshot.getWorldUniqueId()+" is not loaded!")
            );

        Vector3i pos = snapshot.getPosition();
        BlockPos blockPos = new BlockPos(world(blockWorld), pos.getX(), pos.getY(), pos.getZ());
        ChunkPos chunkPos = blockPos.getChunk();
        Chunk chunk = blockWorld.getChunk(chunkPos.x, 0, chunkPos.z).orElseThrow(()->
                new IllegalStateException("The chunk "+chunkPos+" is not loaded")
        );

        return new ReactiveBlock(
                ReactiveLayer.getChunk(chunk).get(),
                ReactiveLayer.getBlockSnapshotData(snapshot).get()
        );
    }

    public PrecisePoint precisePoint(Vector3d point)
    {
        return new PrecisePoint(point.getX(), point.getY(), point.getZ());
    }

    @Nullable
    public Direction direction(org.spongepowered.api.util.Direction targetSide)
    {
        switch(targetSide) {
            case NONE: return null;
            case NORTH: return Direction.NORTH;
            case SOUTH: return Direction.SOUTH;
            case EAST: return Direction.EAST;
            case WEST: return Direction.WEST;
            case UP: return Direction.UP;
            case DOWN: return Direction.DOWN;
            case NORTHEAST: return Direction.NORTH_EAST;
            case SOUTHEAST: return Direction.SOUTH_EAST;
            case SOUTHWEST: return Direction.SOUTH_WEST;
            case NORTHWEST: return Direction.NORTH_WEST;
            default: throw new UnsupportedOperationException(targetSide.toString());
        }
    }

    public org.spongepowered.api.util.Direction direction(@Nullable Direction targetSide)
    {
        if(targetSide == null)
            return org.spongepowered.api.util.Direction.NONE;

        switch(targetSide) {
            case NORTH: return org.spongepowered.api.util.Direction.NORTH;
            case SOUTH: return org.spongepowered.api.util.Direction.SOUTH;
            case EAST: return org.spongepowered.api.util.Direction.EAST;
            case WEST: return org.spongepowered.api.util.Direction.WEST;
            case UP: return org.spongepowered.api.util.Direction.UP;
            case DOWN: return org.spongepowered.api.util.Direction.DOWN;
            case NORTH_EAST: return org.spongepowered.api.util.Direction.NORTHEAST;
            case SOUTH_EAST: return org.spongepowered.api.util.Direction.SOUTHEAST;
            case SOUTH_WEST: return org.spongepowered.api.util.Direction.SOUTHWEST;
            case NORTH_WEST: return org.spongepowered.api.util.Direction.NORTHWEST;
            default: throw new UnsupportedOperationException(targetSide.toString());
        }
    }

    public BlockPos blockPos(Location<World> location)
    {
        return new BlockPos(world(location.getExtent()), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public PlayerSender player(Player player)
    {
        return (PlayerSender) sender(player, player);
    }

    @Override
    public String toString()
    {
        return "MineCitySponge{"+
                "mineCity="+mineCity+
                ", locale="+locale+
                '}';
    }
}
