package br.com.gamemods.minecity.bukkit.command;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.command.*;
import br.com.gamemods.minecity.api.permission.GroupID;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.unchecked.UFunction;
import br.com.gamemods.minecity.api.world.*;
import br.com.gamemods.minecity.bukkit.MineCityBukkit;
import br.com.gamemods.minecity.structure.City;
import br.com.gamemods.minecity.structure.ClaimedChunk;
import br.com.gamemods.minecity.structure.DisplayedSelection;
import br.com.gamemods.minecity.structure.Inconsistency;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.*;
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
    private byte movMessageWait = 0;

    public BukkitPlayer(MineCityBukkit plugin, Player player)
    {
        super(plugin, player);
        this.playerId = new PlayerID(player.getUniqueId(), player.getName());
        Location location = sender.getLocation();
        lastX = location.getBlockX();
        lastY = location.getBlockY();
        lastZ = location.getBlockZ();
        lastChunk = new ChunkPos(plugin.world(location.getWorld()), lastX >> 4, lastZ >> 4);
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
        checkStepOnFakeBlock();
        checkPosition();
    }

    public void checkStepOnFakeBlock()
    {
        Player sender = this.sender;
        if(selection == null || selection.a == null || selection.display.isEmpty()
                || !selection.world.equals(plugin.world(sender.getWorld())))
            return;

        Location location = sender.getLocation();
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

    public void checkPosition()
    {
        Location location = sender.getLocation();
        int posX = location.getBlockX();
        int posY = location.getBlockY();
        int posZ = location.getBlockZ();
        int chunkX = posX >> 4;
        int chunkZ = posZ >> 4;
        World worldObj = location.getWorld();
        WorldDim worldDim = plugin.world(worldObj);
        if(lastChunk.x != chunkX || lastChunk.z != chunkZ || !lastChunk.world.equals(worldDim))
        {
            ChunkPos chunk = new ChunkPos(worldDim, chunkX, chunkZ);
            ClaimedChunk claim = plugin.mineCity.getChunk(chunk).orElseGet(()-> Inconsistency.claim(chunk));
            City city = claim.getCity().orElse(null);
            if(city != null && city != lastCity)
            {
                Optional<Message> message = optionalStream(
                        can(this, PermissionFlag.ENTER, city),
                        can(this, PermissionFlag.LEAVE, lastCity),
                        can(this, PermissionFlag.LEAVE, plugin.mineCity.nature(lastChunk.world))
                )
                        .findFirst()
                        ;

                if(message.isPresent())
                {
                    if(movMessageWait == 0)
                    {
                        send(message.get());
                        movMessageWait = (byte) 20*3;
                    }
                    teleport(new BlockPos(lastChunk.world, lastX, lastY, lastZ));
                    return;
                }
            }

            lastCity = city;
            lastChunk = chunk;
        }

        if(movMessageWait > 0)
            movMessageWait--;
        else
        {
            lastX = posX;
            lastY = posY;
            lastZ = posZ;
        }
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
        return null;
    }

    @Override
    public CommandResult<CommandResult<?>> confirm(String code) throws Exception
    {
        if(!confirmCode.equals(code.toUpperCase()))
            return CommandResult.failed();

        UFunction<CommandSender, CommandResult<?>> action = this.confirmAction;
        confirmCode = null;
        confirmAction = null;
        return new CommandResult<>(null, action.apply(this), true);
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
