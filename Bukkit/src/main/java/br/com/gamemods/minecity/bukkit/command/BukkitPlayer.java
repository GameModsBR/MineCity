package br.com.gamemods.minecity.bukkit.command;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.command.*;
import br.com.gamemods.minecity.api.permission.FlagHolder;
import br.com.gamemods.minecity.api.permission.GroupID;
import br.com.gamemods.minecity.api.unchecked.UFunction;
import br.com.gamemods.minecity.api.world.*;
import br.com.gamemods.minecity.bukkit.MineCityBukkit;
import br.com.gamemods.minecity.bukkit.protection.MovementListener;
import br.com.gamemods.minecity.bukkit.protection.MovementMonitor;
import br.com.gamemods.minecity.structure.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.stream.Stream;

import static br.com.gamemods.minecity.api.CollectionUtil.optionalStream;
import static br.com.gamemods.minecity.api.permission.FlagHolder.can;
import static br.com.gamemods.minecity.api.permission.PermissionFlag.*;

@SuppressWarnings("deprecation")
public class BukkitPlayer extends BukkitLocatableSender<Player> implements MinecraftEntity, MovementListener
{
    public final PlayerID playerId;
    private BukkitSelection selection;
    private UFunction<CommandSender, CommandResult<?>> confirmAction;
    private String confirmCode;
    @Nullable
    private Set<GroupID> groups;
    private final MovementMonitor mov;
    public byte pickupRandomDelay;
    public byte pickupHarvestDelay;
    public byte lureDelay;
    public byte skipTick;
    public Set<LivingEntity> leashedEntities = new HashSet<>(1);

    public BukkitPlayer(MineCityBukkit plugin, Player player)
    {
        super(plugin, player);
        this.playerId = new PlayerID(player.getUniqueId(), player.getName());
        mov = new MovementMonitor(plugin, player, this);
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
        if(x == mov.lastX && y == mov.lastY && z == mov.lastZ)
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

    private void removeUnleashedEntities()
    {
        leashedEntities.removeIf(entity -> !entity.isLeashed() || !sender.equals(entity.getLeashHolder()));
    }

    @Override
    public Optional<Message> onCityChange(@NotNull City city, Plot plot)
    {
        removeUnleashedEntities();

        FlagHolder destiny = plot != null? plot : city;

        // Check if can enter the plot and leave the previous location
        FlagHolder lastHolder = mov.lastHolder();
        Stream<Message> stream = optionalStream(
                can(this, ENTER, destiny),
                can(this, LEAVE, lastHolder)
        );

        Entity vehicle = sender.getVehicle();
        boolean modifying = !leashedEntities.isEmpty();
        if(vehicle != null)
        {
            // Check if can use the ride in that location
            stream = Stream.concat(stream, optionalStream(
                    can(this, RIDE, destiny)
            ));

            if(!modifying)
            {
                // Check if the ride is a community ride
                if(vehicle instanceof Tameable)
                    modifying = !sender.equals(((Tameable) vehicle).getOwner());
                else if(vehicle instanceof LivingEntity)
                    modifying = true;
            }
        }

        // Check if the player can move entities
        if(modifying)
            stream = Stream.concat(stream, can(this, MODIFY, plot, lastHolder instanceof Nature? null : lastHolder));

        return stream.findFirst();
    }

    @Override
    public Optional<Message> onPlotEnter(@NotNull Plot plot)
    {
        removeUnleashedEntities();

        // Check if can enter the plot and leave the previous plot
        Stream<Message> stream = optionalStream(
                can(this, ENTER, plot),
                can(this, LEAVE, mov.lastPlot != null? mov.lastPlot : null)
        );

        Entity vehicle = sender.getVehicle();
        boolean modifying = !leashedEntities.isEmpty();
        if(vehicle != null)
        {
            // Check if can use the ride in the plot
            stream = Stream.concat(stream, optionalStream(
                    can(this, RIDE, plot)
            ));

            if(!modifying)
            {
                // Check if the ride is a community ride
                if(vehicle instanceof Tameable)
                    modifying = !sender.equals(((Tameable) vehicle).getOwner());
                else if(vehicle instanceof LivingEntity)
                    modifying = true;
            }
        }

        // Check if the player can move entities
        if(modifying)
            stream = Stream.concat(stream, can(this, MODIFY, plot, mov.lastPlot != null? mov.lastPlot : mov.lastCity));

        return stream.findFirst();
    }

    @Override
    public Optional<Message> onPlotLeave(@NotNull City city)
    {
        removeUnleashedEntities();
        // Check if can enter the city and leave the plot
        Stream<Message> stream = optionalStream(
                can(this, ENTER, city),
                can(this, LEAVE, mov.lastPlot)
        );

        Entity vehicle = sender.getVehicle();
        boolean modifying = !leashedEntities.isEmpty();
        if(vehicle != null)
        {
            // Check if can use the ride in the city
            stream = Stream.concat(stream, optionalStream(
                    can(this, RIDE, city)
            ));

            if(!modifying)
            {
                // Check if the ride is a community ride
                if(vehicle instanceof Tameable)
                    modifying = !sender.equals(((Tameable) vehicle).getOwner());
                else if(vehicle instanceof LivingEntity)
                    modifying = true;
            }
        }

        // Check if the player can move entities
        if(modifying)
            stream = Stream.concat(stream, can(this, MODIFY, city, mov.lastPlot));

        return stream.findFirst();
    }

    @Override
    public Optional<Message> onCityLeave(@NotNull Nature nature)
    {
        removeUnleashedEntities();

        boolean modifying = !leashedEntities.isEmpty();
        if(!modifying)
        {
            Entity vehicle = sender.getVehicle();
            if(vehicle != null)
            {
                // Check if the ride is a community ride
                if(vehicle instanceof Tameable)
                    modifying = !sender.equals(((Tameable) vehicle).getOwner());
                else if(vehicle instanceof LivingEntity)
                    modifying = true;
            }
        }

        FlagHolder lastHolder = mov.lastHolder();
        return optionalStream(
                can(this, ENTER, nature),
                can(this, LEAVE, lastHolder),
                can(this, MODIFY, modifying? lastHolder : null)
        ).findFirst();
    }

    @Override
    public Optional<Message> onNatureChange(@NotNull Nature nature)
    {
        return optionalStream(
                can(this, ENTER, nature),
                can(this, LEAVE, mov.lastHolder())
        ).findFirst();
    }

    public void checkPosition(Location location)
    {
        City lastCity = mov.lastCity;
        Plot lastPlot = mov.lastPlot;
        ChunkPos lastChunk = mov.lastClaim.chunk;
        Optional<Message> message = mov.checkPosition(location);
        if(message.isPresent())
        {
            if(mov.messageWait > 0 && mov.messageWait % 5 == 0)
                sender.damage(2);

            if(mov.messageWait == 0)
            {
                send(new Message("","<msg><red>${msg}</red></msg>", new Object[]{"msg", message.get()}));
                mov.messageWait = (byte) 20*3;
            }

            Entity vehicle = sender.getVehicle();
            if(vehicle == null)
                teleport(new BlockPos(lastChunk.world, mov.lastX, mov.lastY, mov.lastZ));
            else
            {
                Location vLoc = vehicle.getLocation();
                Optional<World> world = plugin.world(lastChunk.world);
                if(!world.isPresent())
                    teleport(new BlockPos(lastChunk.world, mov.lastX, mov.lastY, mov.lastZ));
                else
                {
                    Location safeLoc = new Location(world.get(), mov.lastX + 0.5, mov.lastY + 0.5, mov.lastZ + 0.5,
                            vLoc.getYaw(), vLoc.getPitch()
                    );
                    if(!vehicle.teleport(safeLoc))
                        vehicle.eject();
                }
            }

            return;
        }

        if(mov.lastCity != lastCity)
        {
            Message title, subtitle;
            if(mov.lastCity != null)
            {
                title = mov.lastCity.getId() > 0? Message.string(mov.lastCity.getName()) : null;
                subtitle = mov.lastPlot != null? Message.string(mov.lastPlot.getName()) : null;
            }
            else
            {
                title = new Message("enter.nature", LegacyFormat.GREEN+"Nature");
                subtitle = Message.string(mov.lastClaim.chunk.world.name());
            }
            sendTitle(title, subtitle);
        }
        else if(mov.lastPlot != lastPlot)
        {
            if(mov.lastPlot != null)
            {
                sendTitle(null, Message.string(mov.lastPlot.getName()));
            }
            else
            {
                sendTitle(Message.string(mov.lastCity.getName()), null);
            }
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
