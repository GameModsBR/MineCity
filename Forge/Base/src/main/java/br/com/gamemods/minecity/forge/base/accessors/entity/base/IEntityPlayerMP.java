package br.com.gamemods.minecity.forge.base.accessors.entity.base;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.command.CommandSender;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.shape.Point;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.EntityPos;
import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.ICommander;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.entity.item.IEntityItem;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.entity.EntityPlayerMPTransformer;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.world.WorldServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Referenced(at = EntityPlayerMPTransformer.class)
public interface IEntityPlayerMP extends IEntityLivingBase, ICommander
{
    void setMineCityPlayer(ForgePlayer player);
    ForgePlayer getMineCityPlayer();

    default MineCityForge getServer()
    {
        return getMineCityPlayer().getServer();
    }

    @Override
    default EntityPlayerMP getForgeEntity()
    {
        return (EntityPlayerMP) this;
    }

    @NotNull
    default PlayerID getIdentity()
    {
        ForgePlayer player = getMineCityPlayer();
        if(player != null)
            return player.identity();

        return new PlayerID(getUniqueID(), getName());
    }

    @NotNull
    @Override
    default PlayerID identity()
    {
        return getIdentity();
    }

    @Override
    default Message teleport(MineCityForge mod, EntityPos pos)
    {
        Entity sender = (Entity) this;
        WorldDim current = mod.world(sender.worldObj);
        if(current.equals(pos.world))
        {
            dismount();
            setPosAndRotation(pos);
            setPosAndUpdate(pos);
            return null;
        }

        WorldServer worldServer = mod.world(pos.world);
        if(worldServer == null)
            return new Message("action.teleport.world-not-found",
                    "The destiny world ${name} was not found or is not loaded",
                    new Object[]{"name", pos.world.name()}
            );

        dismount();
        mod.server.getIPlayerList().transferToDimension(this, pos.world.dim, worldServer.getDefaultTeleporter());
        setPosAndRotation(pos);
        setPosAndUpdate(pos);
        return null;
    }

    @Override
    default Message teleport(MineCityForge mod, BlockPos pos)
    {
        Entity sender = (Entity) this;
        WorldDim current = mod.world(sender.worldObj);
        double x = pos.x+0.5, y = pos.y+0.5, z = pos.z+0.5;
        if(current.equals(pos.world))
        {
            dismount();
            setPosAndUpdate(x, y, z);
            return null;
        }

        WorldServer worldServer = mod.world(pos.world);
        if(worldServer == null)
            return new Message("action.teleport.world-not-found",
                    "The destiny world ${name} was not found or is not loaded",
                    new Object[]{"name", pos.world.name()}
            );

        dismount();
        mod.server.getIPlayerList().transferToDimension(this, pos.world.dim, worldServer.getDefaultTeleporter());
        setPosAndUpdate(x, y, z);
        return null;
    }

    void sendTitle(MineCityForge mod, Message title, Message subTitle);

    @NotNull
    @Override
    default String getName()
    {
        return IEntityLivingBase.super.getName();
    }

    void sendBlock(int x, int y, int z);

    default void sendBlock(BlockPos pos)
    {
        sendBlock(pos.x, pos.y, pos.z);
    }

    void sendFakeBlock(int x, int y, int z, IState state);

    default void sendFakeBlock(BlockPos pos, IState state)
    {
        sendFakeBlock(pos.x, pos.y, pos.z, state);
    }

    default GameProfile getGameProfile()
    {
        return getForgeEntity().getGameProfile();
    }

    default void sendPacket(Packet packet)
    {
        ((EntityPlayerMP) this).connection.sendPacket(packet);
    }

    default void kick(String reason)
    {
        ((EntityPlayerMP) this).connection.kickPlayerFromServer(reason);
    }

    default boolean isFlying()
    {
        return ((EntityPlayerMP) this).capabilities.isFlying;
    }

    default void sendInventoryContents()
    {
        EntityPlayerMP player = (EntityPlayerMP) this;
        player.sendContainerToPlayer(player.inventoryContainer);
    }

    default float getEyeHeight()
    {
        return getForgeEntity().getEyeHeight();
    }

    default boolean isCreative()
    {
        return getForgeEntity().isCreative();
    }

    default boolean isSneaking()
    {
        return getForgeEntity().isSneaking();
    }

    @NotNull
    @Override
    default Type getType()
    {
        return Type.PLAYER;
    }

    @Nullable
    @Override
    default CommandSender getCommandSender()
    {
        ForgePlayer player = getMineCityPlayer();
        if(player == null)
            return null;
        return player.getCommandSender();
    }

    @Override
    default void send(Message message)
    {
        ForgePlayer player = getMineCityPlayer();
        if(player != null)
            player.send(message);
    }

    @Override
    default void send(Message[] messages)
    {
        ForgePlayer player = getMineCityPlayer();
        if(player != null)
            player.send(messages);
    }

    default ItemStack addToEnderChest(IItemStack stack)
    {
        return ((EntityPlayerMP) this).getInventoryEnderChest().addItem(stack.getStack());
    }

    default void attemptToReturn(IItemStack stack)
    {
        EntityPlayerMP player = getForgeEntity();
        if(!player.inventory.addItemStackToInventory(stack.getStack()))
        {
            ItemStack rest = addToEnderChest(stack);
            if(rest != null)
            {
                EntityItem drop = player.dropItem(rest, false, true);
                if(drop == null)
                    getServer().logger.error("Failed to return a stack to the player: "+identity()+" stack: "+stack);
                else
                {
                    drop.setNoPickupDelay();
                    drop.setOwner(player.getName());
                    ((IEntityItem) drop).allowToPickup(identity());
                }
            }
        }
    }

    default void sendChanges()
    {
        ((EntityPlayerMP) this).inventoryContainer.detectAndSendChanges();
    }

    void sendTileEntity(int x, int y, int z);

    default void sendTileEntity(Point pos)
    {
        sendTileEntity(pos.x, pos.y, pos.z);
    }

    default void sendBlockAndTile(int x, int y, int z)
    {
        sendBlock(x, y, z);
        sendTileEntity(x, y, z);
    }

    default void sendBlockAndTile(Point pos)
    {
        sendBlockAndTile(pos.x, pos.y, pos.z);
    }

    default void closeScreen()
    {
        getForgeEntity().closeScreen();
    }

    default int getSelectedSlot()
    {
        return ((EntityPlayerMP) this).inventory.currentItem;
    }

    default IItemStack getStackInSlot(int slot)
    {
        return (IItemStack) (Object) ((EntityPlayerMP) this).inventory.getStackInSlot(slot);
    }

    void sendFakeAir(int x, int y, int z);

    default void sendFakeAir(BlockPos pos)
    {
        sendFakeAir(pos.x, pos.y, pos.z);
    }

    void sendHealth();

    default BlockPos getBlockPos()
    {
        return getBlockPos(getServer());
    }

    default EntityPos getEntityPos()
    {
        return getEntityPos(getServer());
    }

    void sendLeashState(IEntityLiving entity);
}
