package br.com.gamemods.minecity.forge.mc_1_7_10.command;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.command.CommandFunction;
import br.com.gamemods.minecity.api.command.LegacyFormat;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.EntityPos;
import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.forge.base.command.ForgePlayerSender;
import br.com.gamemods.minecity.forge.mc_1_7_10.MineCitySeven;
import io.netty.buffer.Unpooled;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.world.WorldServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SevenPlayerSender extends ForgePlayerSender<EntityPlayerMP, MineCitySeven>
{
    public SevenPlayerSender(MineCitySeven mod, EntityPlayerMP sender)
    {
        super(mod, sender);
    }

    @Override
    protected PlayerID createId(EntityPlayerMP player)
    {
        return new PlayerID(player.getUniqueID(), player.getCommandSenderName());
    }

    @Override
    public void giveSelectionTool()
    {
        ItemStack stack = new ItemStack(Items.wooden_hoe);
        stack.setTagInfo("MineCity", new NBTTagByte((byte)1));
        stack.setStackDisplayName(mod.transformer.toLegacy(new Message("tool.selection.title", LegacyFormat.AQUA+"Selection Tool")));
        //stack.setTagInfo("Lore", mod.transformer.toLore(new Message("tool.selection.lore", "Selects an area in the world")));
        if(!sender.inventory.addItemStackToInventory(stack))
            send(CommandFunction.messageFailed(new Message(
                    "action.give.tool.inventory-full",
                    "You haven't received the tool because your inventory is full."
            )));
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
    public boolean isOp()
    {
        return mod.server.getConfigurationManager().func_152596_g(sender.getGameProfile());
    }

    @NotNull
    @Override
    public ForgeSelection<Block> createSelection(@NotNull WorldDim world)
    {
        ForgeSelection<Block> selection = new ForgeSelection<>(world);
        selection.cornerA = Blocks.glowstone;
        selection.cornerB = Blocks.lit_redstone_lamp;
        selection.corners = Blocks.lit_furnace;
        selection.linesA = Blocks.gold_block;
        selection.linesB = Blocks.lapis_block;
        selection.lines = Blocks.sponge;
        selection.extension = Blocks.glowstone;
        return selection;
    }

    public void sendPacket(Packet packet)
    {
        sender.playerNetServerHandler.sendPacket(packet);
    }

    @Override
    public void sendBlock(int x, int y, int z)
    {
        sendPacket(new S23PacketBlockChange(x, y, z, sender.worldObj));
    }

    @Override
    public void sendFakeBlock(int x, int y, int z, Object block)
    {
        sendFakeBlock(x, y, z, (Block) block, 0);
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
}
