package br.com.gamemods.minecity.forge.command;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.command.*;
import br.com.gamemods.minecity.api.permission.GroupID;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.unchecked.UFunction;
import br.com.gamemods.minecity.api.world.*;
import br.com.gamemods.minecity.forge.MineCityForgeMod;
import br.com.gamemods.minecity.structure.*;
import io.netty.buffer.Unpooled;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.IFluidBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static br.com.gamemods.minecity.api.CollectionUtil.optionalStream;
import static br.com.gamemods.minecity.api.permission.FlagHolder.can;

public class ForgePlayer extends ForgeCommandSender<EntityPlayerMP> implements MinecraftEntity
{
    private final PlayerID identity;
    public ChunkPos lastChunk;
    private int lastX, lastY, lastZ;
    private byte movMessageWait = 0;
    private City lastCity;
    private Plot lastPlot;
    @Nullable
    private Set<GroupID> groups;
    private UFunction<CommandSender, CommandResult<?>> confirmAction;
    private String confirmCode;
    private short confirmExpires;
    private ForgeSelection selection;
    private short hideSelectionTimer = 0;
    private short clearSelectionTimer = 0;

    public ForgePlayer(MineCityForgeMod mod, EntityPlayerMP player)
    {
        super(mod, player);
        identity = new PlayerID(player.getUniqueID(), player.getCommandSenderName());
        lastChunk = new ChunkPos(mod.world(player.worldObj), player.chunkCoordX, player.chunkCoordZ);
        lastX = (int) player.posX;
        lastY = (int) player.posY;
        lastZ = (int) player.posZ;
        Optional<ClaimedChunk> chunk = mod.mineCity.getChunk(lastChunk);
        lastCity = chunk.flatMap(ClaimedChunk::getCity).orElse(null);
        lastPlot = chunk.flatMap(c-> c.getPlotAt(lastX, lastY, lastZ)).orElse(null);
        mod.runAsynchronously(() ->
        {
            try
            {
                groups = new HashSet<>(mod.mineCity.dataSource.getEntityGroups(identity));
            }
            catch(Exception e)
            {
                mod.logger.error("An error occurred while loading the "+getName()+"'s groups!", e);
                mod.callSyncMethod(()-> kick(new Message("task.player.load.groups.failed",
                        "Oops, an error occurred while loading your groups: ${error}",
                        Message.errorArgs(e)
                )));
            }
        });
    }

    public void tick()
    {
        checkStepOnFakeBlock();
        tickConfirm();
        updateGroups();
        checkPosition();
        selectionTimer();
    }

    public void selectionTimer()
    {
        if(hideSelectionTimer > 0)
        {
            if(--hideSelectionTimer == 0)
                selection.hide();
        }

        if(clearSelectionTimer > 0)
        {
            if(--clearSelectionTimer == 0)
                selection.clear();
        }
    }

    public void checkStepOnFakeBlock()
    {
        EntityPlayerMP sender = this.sender;
        if(selection == null || selection.a == null || selection.display.isEmpty()
                || !selection.world.equals(mod.world(sender.worldObj)))
            return;

        int x = (int) sender.posX;
        int y = (int) sender.posY;
        int z = (int) sender.posZ;
        if(x == lastX && y == lastY && z == lastZ)
            return;

        for(int i = 0; i <= 1; i++)
        {
            y--;
            if(sender.worldObj.getBlock(x, y, z).isOpaqueCube())
                return;

            for(BlockPos pos : selection.display.keySet())
            {
                if(pos.x == x && pos.y == y && pos.z == z)
                {
                    selection.display.remove(pos);
                    sendPacket(new S23PacketBlockChange(x, y, z, sender.worldObj));
                    return;
                }
            }
        }
    }

    public void tickConfirm()
    {
        if(confirmExpires > 0)
        {
            confirmExpires--;
            if(confirmExpires == 0)
            {
                confirmCode = null;
                confirmAction = null;
                send(CONFIRM_EXPIRED);
            }
        }
    }

    public void updateGroups()
    {
        Queue<EntityUpdate> entityUpdates = mod.mineCity.entityUpdates;
        EntityUpdate update = entityUpdates.peek();
        if(update == null || !update.identity.equals(identity))
            return;

        if(groups == null)
        {
            if(entityUpdates.size() > 1)
                entityUpdates.add(entityUpdates.poll());
            return;
        }

        entityUpdates.poll();
        switch(update.type)
        {
            case GROUP_ADDED:
                groups.add(update.groupId);
                break;

            case GROUP_REMOVED:
                groups.remove(update.groupId);
                break;

            default:
                mod.logger.error("Unsupported update entity type: "+update.type);
        }
    }

    public void checkPosition()
    {
        Optional<Message> message;
        int posY = (int) sender.posY;
        int posZ = (int) sender.posZ;
        int posX = (int) sender.posX;
        if(lastChunk.x != sender.chunkCoordX || lastChunk.z != sender.chunkCoordZ || lastChunk.world.dim != sender.worldObj.provider.dimensionId)
        {
            ChunkPos chunk = new ChunkPos(mod.world(sender.worldObj), sender.chunkCoordX, sender.chunkCoordZ);
            ClaimedChunk claim = mod.mineCity.getChunk(chunk).orElseGet(()->Inconsistency.claim(chunk));
            City city = claim.getCity().orElse(null);
            Plot plot = null;
            if(city != null)
            {
                plot = claim.getPlotAt(posX, posY, posZ).orElse(null);
                if(city != lastCity)
                {
                    message = optionalStream(
                            can(this, PermissionFlag.ENTER, plot),
                            can(this, PermissionFlag.ENTER, city),
                            can(this, PermissionFlag.LEAVE, lastPlot),
                            can(this, PermissionFlag.LEAVE, lastCity),
                            can(this, PermissionFlag.LEAVE,
                                    lastCity == null? mod.mineCity.nature(lastChunk.world) : null
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
                message = optionalStream(
                        can(this, PermissionFlag.ENTER, mod.mineCity.nature(chunk.world)),
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
            else if(lastChunk.world.dim != chunk.world.dim)
            {
                message = optionalStream(
                        can(this, PermissionFlag.ENTER, mod.mineCity.nature(chunk.world)),
                        can(this, PermissionFlag.LEAVE, mod.mineCity.nature(lastChunk.world))
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
                Plot plot = mod.mineCity.getChunk(new ChunkPos(mod.world(sender.worldObj), sender.chunkCoordX, sender.chunkCoordZ))
                        .flatMap(chunk -> chunk.getPlotAt(posX, posY, posZ))
                        .orElse(null);

                if(plot != lastPlot)
                {
                    message = optionalStream(
                            can(this, PermissionFlag.ENTER, plot),
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
                sender.attackEntityFrom(new DamageSource("sufocation"), 2);

            if(movMessageWait == 0)
            {
                send(new Message("","<msg><red>${msg}</red></msg>", new Object[]{"msg", message.get()}));
                movMessageWait = (byte) 20*3;
            }
            Entity vehicle = sender.ridingEntity;
            if(vehicle == null)
                teleport(new BlockPos(lastChunk.world, lastX, lastY, lastZ));
            else
            {
                if(lastChunk.world.dim == vehicle.worldObj.provider.dimensionId)
                    vehicle.setPosition(lastX+0.5, lastY+0.5, lastZ+0.5);
                else
                    teleport(new BlockPos(lastChunk.world, lastX, lastY, lastZ));
            }
            return;
        }

        if(movMessageWait > 0)
            movMessageWait--;
        else if((lastX != posX || lastZ != posZ || lastY < posY))
        {
            if(!sender.worldObj.isSideSolid(posX, posY-1, posZ, ForgeDirection.UP))
            {
                Block block = sender.worldObj.getBlock(posX, posY - 1, posZ);
                if(!(block instanceof BlockLiquid || block instanceof IFluidBlock))
                    return;
            }

            lastX = posX;
            lastY = posY;
            lastZ = posZ;
        }
    }

    public void sendTitle(Message title, Message subtitle)
    {
        if(subtitle == null)
            send(new Message("",LegacyFormat.DARK_GRAY+" ~ "+LegacyFormat.GRAY+"${name}", new Object[]{"name", title}));
        else
            send(new Message("",LegacyFormat.DARK_GRAY+" ~ ${title} :"+LegacyFormat.GRAY+" ${sub}", new Object[][]{
                    {"sub", subtitle},
                    {"title", title}
            }));
    }

    @Override
    public void giveSelectionTool()
    {
        ItemStack stack = new ItemStack(Items.wooden_hoe);
        stack.setTagInfo("MineCity", new NBTTagByte((byte)1));
        stack.setStackDisplayName(mod.transformer.toLegacy(new Message("tool.selection.title", LegacyFormat.AQUA+"Selection Tool")));
        stack.setTagInfo("Lore", mod.transformer.toLore(new Message("tool.selection.lore", "Selects an area in the world")));
        if(!sender.inventory.addItemStackToInventory(stack))
            send(CommandFunction.messageFailed(new Message(
                    "action.give.tool.inventory-full",
                    "You haven't received the tool because your inventory is full."
            )));
    }

    @NotNull
    @Override
    public ForgeSelection getSelection(@NotNull WorldDim world)
    {
        if(selection == null || !selection.world.equals(world))
            selection = new ForgeSelection(world);
        return selection;
    }

    @Override
    public boolean kick(Message message)
    {
        sender.playerNetServerHandler.kickPlayerFromServer(mod.transformer.toLegacy(message));
        return true;
    }

    @Override
    public EntityPos getPosition()
    {
        return new EntityPos(mod.world(sender.worldObj), sender.posX, sender.posY, sender.posZ, sender.rotationPitch, sender.rotationYaw);
    }

    @Override
    public boolean isPlayer()
    {
        return true;
    }

    @Override
    public PlayerID getPlayerId()
    {
        return identity;
    }

    @Override
    public Direction getCardinalDirection()
    {
        return Direction.cardinal8.get(MathHelper.floor_double((double)((sender.rotationYaw + 180.0F) * 8.0F / 360.0F) + 0.5D) & 7);
    }

    @Nullable
    @Override
    public Message teleport(@NotNull BlockPos pos)
    {
        WorldDim current = mod.world(sender.worldObj);
        double x = pos.x+0.5, y = pos.y+0.5, z = pos.z+0.5;
        if(current.equals(pos.world))
        {
            sender.mountEntity(null);
            sender.setPositionAndUpdate(x, y, z);
            return null;
        }

        WorldServer worldServer = mod.world(pos.world);
        if(worldServer == null)
            return new Message("action.teleport.world-not-found",
                    "The destiny world ${name} was not found or is not loaded",
                    new Object[]{"name", pos.world.name()}
            );

        sender.mountEntity(null);
        mod.server.getConfigurationManager().transferPlayerToDimension(sender, pos.world.dim, worldServer.getDefaultTeleporter());
        sender.setPositionAndUpdate(x, y, z);

        return null;
    }

    @Nullable
    @Override
    public Message teleport(@NotNull EntityPos pos)
    {
        WorldDim current = mod.world(sender.worldObj);
        if(current.equals(pos.world))
        {
            sender.mountEntity(null);
            sender.setPositionAndRotation(pos.x, pos.y, pos.z, pos.yaw, pos.pitch);
            sender.setPositionAndUpdate(pos.x, pos.y, pos.z);
            return null;
        }

        WorldServer worldServer = mod.world(pos.world);
        if(worldServer == null)
            return new Message("action.teleport.world-not-found",
                    "The destiny world ${name} was not found or is not loaded",
                    new Object[]{"name", pos.world.name()}
            );

        sender.mountEntity(null);
        mod.server.getConfigurationManager().transferPlayerToDimension(sender, pos.world.dim, worldServer.getDefaultTeleporter());
        sender.setPositionAndRotation(pos.x, pos.y, pos.z, pos.yaw, pos.pitch);
        sender.setPositionAndUpdate(pos.x, pos.y, pos.z);

        return null;
    }

    @Override
    public boolean isConfirmPending()
    {
        return confirmExpires > 0;
    }

    @Override
    public String confirm(@NotNull UFunction<CommandSender, CommandResult<?>> onConfirm)
    {
        confirmExpires = 20*30;
        confirmCode = new BigInteger(28, MineCity.RANDOM).toString(32).toUpperCase();
        confirmAction = onConfirm;
        return confirmCode;
    }

    @Override
    public CommandResult<CommandResult<?>> confirm(String code) throws ExecutionException
    {
        if(confirmExpires == 0 || !confirmCode.equals(code.toUpperCase()))
            return CommandResult.failed();

        UFunction<CommandSender, CommandResult<?>> action = this.confirmAction;
        confirmExpires = 0;
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

    @NotNull
    @Override
    public String getName()
    {
        return sender.getCommandSenderName();
    }

    @NotNull
    @Override
    public UUID getUniqueId()
    {
        return sender.getUniqueID();
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
    public PlayerID getIdentity()
    {
        return identity;
    }

    @Override
    public boolean isGroupLoaded()
    {
        return groups != null;
    }

    @NotNull
    @Override
    public Set<GroupID> getGroupIds()
    {
        return groups == null? Collections.emptySet() : groups;
    }

    private boolean isOp()
    {
        return mod.server.getConfigurationManager().func_152596_g(sender.getGameProfile());
    }

    @Override
    public boolean hasPermission(String perm)
    {
        //TODO Mini permission system for forge and attempt to integrate to Cauldron or ForgeEssentials
        return isOp() || !perm.contains("reload");
    }

    public void sendPacket(Packet packet)
    {
        sender.playerNetServerHandler.sendPacket(packet);
    }

    public void sendFakeBlock(int x, int y, int z, Block block, int metadata)
    {
        sendFakeBlock(x, y, z, Block.getIdFromBlock(block), metadata);
    }

    public void sendFakeBlock(int x, int y, int z, int block, int metadata)
    {
        PacketBuffer buf = new PacketBuffer(Unpooled.buffer(4 + 1 + 4 + 4 + 1));
        buf.writeInt(x);
        buf.writeByte(y);
        buf.writeInt(z);
        buf.writeVarIntToBuffer(block);
        buf.writeByte(metadata);

        S23PacketBlockChange empty = new S23PacketBlockChange();
        try
        {
            empty.readPacketData(buf);
            sendPacket(empty);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public class ForgeSelection extends DisplayedSelection<Block>
    {
        private ForgeSelection(@NotNull WorldDim world)
        {
            super(world);
            cornerA = Blocks.glowstone;
            cornerB = Blocks.lit_redstone_lamp;
            corners = Blocks.lit_furnace;
            linesA = Blocks.gold_block;
            linesB = Blocks.lapis_block;
            lines = Blocks.sponge;
            extension = Blocks.glowstone;
        }

        @Override
        public void updateDisplay()
        {
            super.updateDisplay();
            hideSelectionTimer = 60*20;
            clearSelectionTimer = 5*60*20;
        }

        @Override
        protected void send(Map<BlockPos, Block> last)
        {
            BlockPos pos = getPosition().getBlock();
            display.remove(pos);
            display.remove(pos.add(Direction.UP));
            mod.callSyncMethod(()->{
                World worldObj = sender.worldObj;
                if(!mod.world(worldObj).equals(world))
                    return;

                Set<BlockPos> removed = last.keySet();
                removed.removeAll(display.keySet());
                for(BlockPos p: removed)
                {
                    Chunk chunk = mod.chunk(p.getChunk());
                    if(chunk != null)
                    {
                        // This constructor loads the chunk when it's unloaded,
                        // that's why we check if it's loaded before
                        sendPacket(new S23PacketBlockChange(p.x, p.y, p.z, worldObj));
                    }
                }

                for(Map.Entry<BlockPos, Block> entry: display.entrySet())
                {
                    BlockPos p = entry.getKey();
                    Chunk chunk = mod.chunk(p.getChunk());
                    if(chunk != null)
                    {
                        S23PacketBlockChange packet = new S23PacketBlockChange(p.x, p.y, p.z, worldObj);
                        packet.field_148883_d = entry.getValue();
                        sendPacket(packet);
                    }
                }
            });
        }
    }
}
