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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
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
        Optional<Message> denial = Stream.of(
                can(this, plot != null? plot : city,
                        ENTER,
                        sender.getVehicle() == null? null : RIDE,
                        leashedEntities.isEmpty()? null : MODIFY
                ),
                can(this, mov.lastHolder(),
                        LEAVE,
                        leashedEntities.isEmpty()? null : MODIFY
                )
        ).flatMap(Function.identity()).findFirst();

        if(!denial.isPresent())
        {
            Message title = new Message("", "${name}", new Object[]{"name", city.getName()});
            Message subtitle;
            if(plot != null)
                subtitle = new Message("","${name}", new Object[]{"name", plot.getName()});
            else
                subtitle = null;

            sendTitle(title, subtitle);
        }

        return denial;
    }

    @Override
    public Optional<Message> onPlotEnter(@NotNull Plot plot)
    {
        removeUnleashedEntities();
        Optional<Message> denial = optionalStream(
                can(this, ENTER, plot),
                can(this, RIDE, sender.getVehicle() == null? null : plot),
                can(this, MODIFY, leashedEntities.isEmpty()? null : plot),
                can(this, MODIFY, leashedEntities.isEmpty()? null : mov.lastHolder()),
                can(this, LEAVE, mov.lastPlot)
        ).findFirst();

        if(!denial.isPresent())
        {
            City city = plot.getCity();
            Message title = mov.lastCity != city? new Message("", "${name}", new Object[]{"name", city.getName()}) : null;
            Message subtitle = new Message("","${name}", new Object[]{"name", plot.getName()});
            sendTitle(title, subtitle);
        }

        return denial;
    }

    @Override
    public Optional<Message> onPlotLeave(@NotNull City city)
    {
        removeUnleashedEntities();
        Optional<Message> denial = optionalStream(
                can(this, ENTER, city),
                can(this, RIDE, sender.getVehicle() == null? null : city),
                can(this, MODIFY, leashedEntities.isEmpty()? null : city),
                can(this, MODIFY, leashedEntities.isEmpty()? null : mov.lastHolder()),
                can(this, LEAVE, mov.lastPlot)
        ).findFirst();

        if(!denial.isPresent())
        {
            Message title = new Message("", "${name}", new Object[]{"name", city.getName()});
            sendTitle(title, null);
        }

        return denial;
    }

    @Override
    public Optional<Message> onCityLeave(@NotNull Nature nature)
    {
        removeUnleashedEntities();
        FlagHolder lastHolder = mov.lastHolder();
        Optional<Message> denial = optionalStream(
                can(this, ENTER, nature),
                can(this, RIDE, sender.getVehicle() == null? null : nature),
                can(this, MODIFY, leashedEntities.isEmpty()? null : nature),
                can(this, MODIFY, leashedEntities.isEmpty()? null : lastHolder),
                can(this, LEAVE, leashedEntities.isEmpty()? null : lastHolder)
        ).findFirst();

        if(!denial.isPresent())
        {
            Message title = new Message("enter.nature", LegacyFormat.GREEN+"Nature");
            Message subtitle = new Message("","${name}", new Object[]{"name", nature.world.name()});
            sendTitle(title, subtitle);
        }

        return denial;
    }

    @Override
    public Optional<Message> onNatureChange(@NotNull Nature nature)
    {
        removeUnleashedEntities();
        FlagHolder lastHolder = mov.lastHolder();
        Optional<Message> denial = optionalStream(
                can(this, ENTER, nature),
                can(this, RIDE, sender.getVehicle() == null? null : nature),
                can(this, MODIFY, leashedEntities.isEmpty()? null : nature),
                can(this, MODIFY, leashedEntities.isEmpty()? null : lastHolder),
                can(this, LEAVE, lastHolder)
        ).findFirst();

        if(!denial.isPresent())
        {
            Message title = new Message("enter.nature", LegacyFormat.GREEN+"Nature");
            Message subtitle = new Message("","${name}", new Object[]{"name", nature.world.name()});
            sendTitle(title, subtitle);
        }

        return denial;
    }

    public void checkPosition(Location location)
    {
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
                teleport(new BlockPos(mov.lastChunk.world, mov.lastX, mov.lastY, mov.lastZ));
            else
            {
                Location vLoc = vehicle.getLocation();
                Optional<World> world = plugin.world(mov.lastChunk.world);
                if(!world.isPresent())
                    teleport(new BlockPos(mov.lastChunk.world, mov.lastX, mov.lastY, mov.lastZ));
                else
                    if(!vehicle.teleport(new Location(world.get(), mov.lastX+0.5, mov.lastY+0.5, mov.lastZ+0.5, vLoc.getYaw(), vLoc.getPitch())))
                    {
                        Entity passenger = vehicle.getPassenger();
                        vehicle.eject();
                        teleport(new BlockPos(mov.lastChunk.world, mov.lastX, mov.lastY, mov.lastZ));
                        if(vehicle.teleport(new Location(world.get(), mov.lastX+0.5, mov.lastY+0.5, mov.lastZ+0.5, vLoc.getYaw(), vLoc.getPitch())))
                            getServer().callSyncMethod(()-> vehicle.setPassenger(passenger));
                    }
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
