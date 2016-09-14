package br.com.gamemods.minecity.forge.mc_1_7_10.accessors.entity;

import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.entity.SevenEntityPlayerMPTransformer;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.InventoryEnderChest;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import org.jetbrains.annotations.NotNull;

@Referenced(at = SevenEntityPlayerMPTransformer.class)
public interface SevenEntityPlayerMP extends IEntityPlayerMP, SevenEntityLivingBase
{
    @Override
    default void sendPacket(Packet packet)
    {
        ((EntityPlayerMP) this).playerNetServerHandler.sendPacket(packet);
    }

    @Override
    default void kick(String reason)
    {
        ((EntityPlayerMP) this).playerNetServerHandler.kickPlayerFromServer(reason);
    }

    @Override
    default void sendBlock(int x, int y, int z)
    {
        sendPacket(new S23PacketBlockChange(x, y, z, getWorld()));
    }

    @Override
    default void sendFakeBlock(int x, int y, int z, IState state)
    {
        sendFakeBlock(x, y, z, state.getIBlock().getId(), state.getStateId());
    }

    default void sendFakeBlock(int x, int y, int z, int block, int metadata)
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

    @Override
    default void sendTitle(MineCityForge mod, Message title, Message subTitle)
    {
        // Not supported
    }

    @NotNull
    @Override
    default String getName()
    {
        return SevenEntityLivingBase.super.getName();
    }

    @Override
    default float getEyeHeight()
    {
        return ((EntityPlayerMP) this).yOffset;
    }

    @Override
    default boolean isCreative()
    {
        return getForgeEntity().capabilities.isCreativeMode;
    }

    @Override
    default ItemStack addToEnderChest(IItemStack stack)
    {
        InventoryEnderChest inv = getForgeEntity().getInventoryEnderChest();
        ItemStack itemStack = stack.getStack().copy();

        for (int i = 0; i < inv.getSizeInventory(); ++i)
        {
            ItemStack other = inv.getStackInSlot(i);

            //noinspection ConstantConditions
            if (other == null)
            {
                inv.setInventorySlotContents(i, itemStack);
                inv.markDirty();
                return null;
            }

            if (ItemStack.areItemStacksEqual(other, itemStack))
            {
                int j = Math.min(inv.getInventoryStackLimit(), other.getMaxStackSize());
                int k = Math.min(itemStack.stackSize, j - other.stackSize);

                if (k > 0)
                {
                    other.stackSize += k;
                    itemStack.stackSize -= k;

                    if (itemStack.stackSize <= 0)
                    {
                        inv.markDirty();
                        return null;
                    }
                }
            }
        }

        if (itemStack.stackSize != stack.getSize())
            inv.markDirty();

        return itemStack;
    }

    @Override
    default void sendTileEntity(int x, int y, int z)
    {
        TileEntity tile = getWorld().getTileEntity(x, y, z);
        //noinspection ConstantConditions
        if(tile == null)
            return;

        Packet packet = tile.getDescriptionPacket();
        //noinspection ConstantConditions
        if(packet == null)
        {
            NBTTagCompound nbt = new NBTTagCompound();
            tile.writeToNBT(nbt);
            packet = new S35PacketUpdateTileEntity(x, y, z, 1, nbt);
        }

        sendPacket(packet);
    }

    @Override
    default void sendFakeAir(int x, int y, int z)
    {
        sendFakeBlock(x, y, z, 0, 0);
    }
}
