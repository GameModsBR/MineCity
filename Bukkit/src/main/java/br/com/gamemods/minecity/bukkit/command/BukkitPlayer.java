package br.com.gamemods.minecity.bukkit.command;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.command.*;
import br.com.gamemods.minecity.api.permission.GroupID;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.unchecked.UFunction;
import br.com.gamemods.minecity.api.world.*;
import br.com.gamemods.minecity.bukkit.MineCityBukkit;
import br.com.gamemods.minecity.structure.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

import static br.com.gamemods.minecity.api.CollectionUtil.optionalStream;
import static br.com.gamemods.minecity.api.permission.FlagHolder.can;

@SuppressWarnings("deprecation")
public class BukkitPlayer extends BukkitLocatableSender<Player> implements MinecraftEntity
{
    public final PlayerID playerId;
    private BukkitSelection selection;
    private UFunction<CommandSender, CommandResult<?>> confirmAction;
    private String confirmCode;
    public ChunkPos lastChunk;
    private int lastX, lastY, lastZ;
    @Nullable
    private Set<GroupID> groups;
    private City lastCity;
    private Plot lastPlot;
    private byte movMessageWait;
    public byte pickupRandomDelay;
    public byte pickupHarvestDelay;
    public byte lureDelay;
    public byte skipTick;

    public BukkitPlayer(MineCityBukkit plugin, Player player)
    {
        super(plugin, player);
        this.playerId = new PlayerID(player.getUniqueId(), player.getName());
        Location location = sender.getLocation();
        lastX = location.getBlockX();
        lastY = location.getBlockY();
        lastZ = location.getBlockZ();
        lastChunk = new ChunkPos(plugin.world(location.getWorld()), lastX >> 4, lastZ >> 4);
        Optional<ClaimedChunk> chunk = plugin.mineCity.getChunk(lastChunk);
        lastCity = chunk.flatMap(ClaimedChunk::getCity).orElse(null);
        lastPlot = chunk.flatMap(c-> c.getPlotAt(lastX, lastY, lastZ)).orElse(null);
        plugin.runAsynchronously(() ->
        {
            try
            {
                groups = new HashSet<>(plugin.mineCity.dataSource.getEntityGroups(playerId));
            }
            catch(Exception e)
            {
                plugin.logger.log(Level.SEVERE, "An error occurred while loading the "+getName()+"'s groups!", e);
                plugin.callSyncMethod(()-> kick(new Message("task.player.load.groups.failed",
                        "Oops, an error occurred while loading your groups: ${error}",
                        Message.errorArgs(e)
                )));
            }
        });
    }

    public void tick()
    {
        Location location = sender.getLocation();
        if(skipTick > 0)
        {
            skipTick--;
            return;
        }

        checkStepOnFakeBlock(location);
        checkPosition(location);
    }

    public void checkStepOnFakeBlock(Location location)
    {
        Player sender = this.sender;
        if(selection == null || selection.a == null || selection.display.isEmpty()
                || !selection.world.equals(plugin.world(sender.getWorld())))
            return;

        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        if(x == lastX && y == lastY && z == lastZ)
            return;

        for(int i = 0; i <= 1; i++)
        {
            y--;
            World world = sender.getWorld();
            Block block = world.getBlockAt(x, y, z);
            if(block.getType().isSolid())
                return;

            for(BlockPos pos : selection.display.keySet())
            {
                if(pos.x == x && pos.y == y && pos.z == z)
                {
                    selection.display.remove(pos);
                    sender.sendBlockChange(new Location(world, x, y, z), block.getType(), block.getData());
                    return;
                }
            }
        }
    }

    public void checkPosition(Location location)
    {
        int posX = location.getBlockX();
        int posY = location.getBlockY();
        int posZ = location.getBlockZ();
        int chunkX = posX >> 4;
        int chunkZ = posZ >> 4;
        World worldObj = location.getWorld();
        WorldDim worldDim = plugin.world(worldObj);
        Optional<Message> message;
        Entity vehicle = sender.getVehicle();
        if(lastChunk.x != chunkX || lastChunk.z != chunkZ || !lastChunk.world.equals(worldDim))
        {
            ChunkPos chunk = new ChunkPos(worldDim, chunkX, chunkZ);
            ClaimedChunk claim = plugin.mineCity.getChunk(chunk).orElseGet(()-> Inconsistency.claim(chunk));
            City city = claim.reserve? null : claim.getCity().orElse(null);
            Plot plot = null;
            if(city != null)
            {
                plot = claim.getPlotAt(posX, posY, posZ).orElse(null);
                if(city != lastCity)
                {
                    message = optionalStream(
                            can(this, PermissionFlag.ENTER, plot),
                            can(this, PermissionFlag.ENTER, city),
                            can(this, PermissionFlag.RIDE, vehicle == null? null : plot),
                            can(this, PermissionFlag.RIDE, vehicle == null? null : city),
                            can(this, PermissionFlag.LEAVE, lastPlot),
                            can(this, PermissionFlag.LEAVE, lastCity),
                            can(this, PermissionFlag.LEAVE,
                                    lastCity == null? plugin.mineCity.nature(lastChunk.world) : null
                            )
                    ).findFirst();

                    if(!message.isPresent())
                    {
                        Message title = new Message("", "${name}", new Object[]{"name", city.getName()});
                        Message subtitle;
                        if(plot != null)
                            subtitle = new Message("","${name}", new Object[]{"name", plot.getName()});
                        else
                            subtitle = null;

                        sendTitle(title, subtitle);
                    }
                }
                else if(plot != lastPlot)
                {
                    message = optionalStream(
                            can(this, PermissionFlag.ENTER, plot),
                            can(this, PermissionFlag.RIDE, vehicle == null? null : plot),
                            can(this, PermissionFlag.LEAVE, lastPlot)
                    ).findFirst();

                    if(!message.isPresent())
                    {
                        Message subtitle = new Message("","${name}", new Object[]{"name", plot == null? city.getName() : plot.getName()});
                        sendTitle(null, subtitle);
                    }
                }
                else
                    message = Optional.empty();
            }
            else if(lastCity != null)
            {
                Nature nature = plugin.mineCity.nature(chunk.world);
                message = optionalStream(
                        can(this, PermissionFlag.ENTER, nature),
                        can(this, PermissionFlag.RIDE, vehicle == null? null : nature),
                        can(this, PermissionFlag.LEAVE, lastPlot),
                        can(this, PermissionFlag.LEAVE, lastCity)
                ).findFirst();

                if(!message.isPresent())
                {
                    Message title = new Message("enter.nature", LegacyFormat.GREEN+"Nature");
                    Message subtitle = new Message("","${name}", new Object[]{"name", chunk.world.name()});
                    sendTitle(title, subtitle);
                }
            }
            else if(!lastChunk.world.equals(chunk.world))
            {
                Nature nature = plugin.mineCity.nature(chunk.world);
                message = optionalStream(
                        can(this, PermissionFlag.ENTER, nature),
                        can(this, PermissionFlag.RIDE, vehicle == null? null : nature),
                        can(this, PermissionFlag.LEAVE, plugin.mineCity.nature(lastChunk.world))
                ).findFirst();

                if(!message.isPresent())
                {
                    Message title = new Message("enter.nature", LegacyFormat.GREEN+"Nature");
                    Message subtitle = new Message("","${name}", new Object[]{"name", chunk.world.name()});
                    sendTitle(title, subtitle);
                }
            }
            else
                message = Optional.empty();

            if(!message.isPresent())
            {
                lastCity = city;
                lastChunk = chunk;
                lastPlot = plot;
            }
        }
        else if(posX != lastX || posY != lastY || posZ != lastZ)
        {
            if(lastCity != null)
            {
                Plot plot = plugin.mineCity.getChunk(new ChunkPos(worldDim, chunkX, chunkZ))
                        .flatMap(chunk -> chunk.getPlotAt(posX, posY, posZ))
                        .orElse(null);

                if(plot != lastPlot)
                {
                    message = optionalStream(
                            can(this, PermissionFlag.ENTER, plot),
                            can(this, PermissionFlag.RIDE, vehicle == null? null : plot),
                            can(this, PermissionFlag.LEAVE, lastPlot)
                    ).findFirst();

                    if(!message.isPresent())
                    {
                        lastPlot = plot;

                        Message title = new Message("", "${name}", new Object[]{"name", lastCity.getName()});
                        Message subtitle;
                        if(plot != null)
                            subtitle = new Message("","${name}", new Object[]{"name", plot.getName()});
                        else
                            subtitle = null;

                        sendTitle(title, subtitle);
                    }
                }
                else
                    message = Optional.empty();
            }
            else
                message = Optional.empty();
        }
        else
            message = Optional.empty();

        if(message.isPresent())
        {
            if(movMessageWait > 0 && movMessageWait % 5 == 0)
                sender.damage(2);

            if(movMessageWait == 0)
            {
                send(new Message("","<msg><red>${msg}</red></msg>", new Object[]{"msg", message.get()}));
                movMessageWait = (byte) 20*3;
            }


            if(vehicle == null)
                teleport(new BlockPos(lastChunk.world, lastX, lastY, lastZ));
            else
            {
                Location vLoc = vehicle.getLocation();
                Optional<World> world = plugin.world(lastChunk.world);
                if(!world.isPresent())
                    teleport(new BlockPos(lastChunk.world, lastX, lastY, lastZ));
                else
                    if(!vehicle.teleport(new Location(world.get(), lastX+0.5, lastY+0.5, lastZ+0.5, vLoc.getYaw(), vLoc.getPitch())))
                    {
                        Entity passenger = vehicle.getPassenger();
                        vehicle.eject();
                        teleport(new BlockPos(lastChunk.world, lastX, lastY, lastZ));
                        if(vehicle.teleport(new Location(world.get(), lastX+0.5, lastY+0.5, lastZ+0.5, vLoc.getYaw(), vLoc.getPitch())))
                            getServer().callSyncMethod(()-> vehicle.setPassenger(passenger));
                    }
            }
            return;
        }

        if(movMessageWait > 0)
            movMessageWait--;
        else if((lastX != posX || lastZ != posZ || lastY < posY))
        {
            Material block = sender.getWorld().getBlockAt(posX, posY - 1, posZ).getType();
            switch(block)
            {
                case WATER:
                case STATIONARY_WATER:
                case LAVA:
                case STATIONARY_LAVA:
                    break;
                default:
                    if(block.isSolid())
                        break;
                    else if(!sender.isGliding() && !sender.isFlying())
                        return;
            }
            lastX = posX;
            lastY = posY;
            lastZ = posZ;
        }
    }

    public void sendTitle(Message title, Message subtitle)
    {
        MessageTransformer trans = plugin.mineCity.messageTransformer;
        sender.resetTitle();
        sender.sendTitle(title == null? "" : trans.toLegacy(title), subtitle == null? "" : trans.toLegacy(subtitle));
    }

    @Override
    public String confirm(@NotNull UFunction<CommandSender, CommandResult<?>> onConfirm)
    {
        String code = confirmCode = new BigInteger(28, MineCity.RANDOM).toString(32).toUpperCase();
        confirmAction = onConfirm;
        sender.getServer().getScheduler().runTaskLater(plugin.plugin, ()->{
            if(code.equals(confirmCode))
            {
                confirmCode = null;
                confirmAction = null;
                send(CONFIRM_EXPIRED);
            }
        }, 20*30);
        return code;
    }

    @Override
    public CommandResult<CommandResult<?>> confirm(String code) throws ExecutionException
    {
        if(!confirmCode.equals(code.toUpperCase()))
            return CommandResult.failed();

        UFunction<CommandSender, CommandResult<?>> action = this.confirmAction;
        confirmCode = null;
        confirmAction = null;
        try
        {
            return new CommandResult<>(null, action.apply(this), true);
        }
        catch(Exception e)
        {
            throw new ExecutionException(e);
        }
    }

    @Override
    public boolean isConfirmPending()
    {
        return confirmCode != null;
    }

    @NotNull
    @Override
    public BukkitSelection getSelection(@NotNull WorldDim world)
    {
        if(selection == null || !selection.world.equals(world))
            selection = new BukkitSelection(world);
        return selection;
    }

    @Override
    public void giveSelectionTool()
    {
        ItemStack stack = new ItemStack(Material.WOOD_HOE);
        ItemMeta meta = stack.getItemMeta();
        MessageTransformer transformer = plugin.mineCity.messageTransformer;
        meta.setDisplayName(transformer.toLegacy(new Message("tool.selection.title", LegacyFormat.AQUA+"Selection Tool")));
        meta.setLore(Arrays.asList(transformer.toMultilineLegacy(
                new Message("","<white>${lore}</white>", new Object[]
                        {"lore", new Message("tool.selection.lore","Selects an area in the world")}
                ))
        ));
        stack.setItemMeta(meta);
        HashMap<Integer, ItemStack> rejected = sender.getInventory().addItem(stack);
        if(!rejected.isEmpty())
            send(CommandFunction.messageFailed(new Message(
                    "action.give.tool.inventory-full",
                    "You haven't received the tool because your inventory is full."
            )));
        sender.updateInventory();
    }

    @Override
    public EntityPos getPosition()
    {
        return plugin.entityPos(sender.getLocation());
    }

    @Override
    public boolean isPlayer()
    {
        return true;
    }

    @Override
    public PlayerID getPlayerId()
    {
        return playerId;
    }

    public boolean kick(Message message)
    {
        sender.kickPlayer(plugin.mineCity.messageTransformer.toLegacy(message));
        return true;
    }

    @NotNull
    @Override
    public String getName()
    {
        return sender.getName();
    }

    @NotNull
    @Override
    public UUID getUniqueId()
    {
        return playerId.uniqueId;
    }

    @NotNull
    @Override
    public Type getType()
    {
        return Type.PLAYER;
    }

    @Nullable
    @Override
    public CommandSender getCommandSender()
    {
        return this;
    }

    @NotNull
    @Override
    public Set<GroupID> getGroupIds()
    {
        return groups == null? Collections.emptySet() : groups;
    }

    @Override
    public boolean isGroupLoaded()
    {
        return groups != null;
    }

    public boolean updateGroups(EntityUpdate update)
    {
        if(groups == null)
            return false;

        switch(update.type)
        {
            case GROUP_ADDED:
                groups.add(update.groupId);
                return true;

            case GROUP_REMOVED:
                groups.remove(update.groupId);
                return true;

            default:
                plugin.logger.severe("Unsupported update entity type: "+update.type);
                return true;
        }
    }

    public class BukkitSelection extends DisplayedSelection<Material>
    {

        protected BukkitSelection(@NotNull WorldDim world)
        {
            super(world);
            cornerA = Material.GLOWSTONE;
            cornerB = Material.REDSTONE_LAMP_ON;
            corners = Material.BURNING_FURNACE;
            linesA = Material.GOLD_BLOCK;
            linesB = Material.LAPIS_BLOCK;
            lines = Material.GLASS;
            extension = Material.GLOWSTONE;
        }

        @Override
        protected void send(Map<BlockPos, Material> last)
        {
            plugin.callSyncMethod(()->{
                World worldObj = sender.getWorld();
                if(!plugin.world(worldObj).equals(world))
                    return;

                Set<BlockPos> removed = last.keySet();
                removed.removeAll(display.keySet());
                for(BlockPos p: removed)
                {
                    Optional<ClaimedChunk> chunk = plugin.mineCity.getChunk(p.getChunk());
                    if(chunk.isPresent())
                    {
                        // This would load the chunk when it's unloaded,
                        // that's why we check if it's loaded before
                        Block block = worldObj.getBlockAt(p.x, p.y, p.z);
                        sender.sendBlockChange(new Location(worldObj, p.x, p.y, p.z), block.getType(), block.getData());
                    }
                }

                for(Map.Entry<BlockPos, Material> entry: display.entrySet())
                {
                    BlockPos p = entry.getKey();
                    Optional<ClaimedChunk> chunk = plugin.mineCity.getChunk(p.getChunk());
                    if(chunk.isPresent())
                        sender.sendBlockChange(new Location(worldObj, p.x, p.y, p.z), entry.getValue(), (byte) 0);
                }
            });
        }
    }
}
