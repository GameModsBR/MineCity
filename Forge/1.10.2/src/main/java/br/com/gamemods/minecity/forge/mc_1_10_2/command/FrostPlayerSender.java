package br.com.gamemods.minecity.forge.mc_1_10_2.command;

import br.com.gamemods.minecity.api.command.CommandFunction;
import br.com.gamemods.minecity.api.command.LegacyFormat;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.world.EntityPos;
import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.forge.base.command.ForgePlayerSender;
import br.com.gamemods.minecity.forge.mc_1_10_2.MineCityFrost;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FrostPlayerSender extends ForgePlayerSender<EntityPlayerMP, MineCityFrost>
{
    public FrostPlayerSender(MineCityFrost mod, EntityPlayerMP sender)
    {
        super(mod, sender);
    }

    @Override
    public void giveSelectionTool()
    {
        ItemStack stack = new ItemStack(Items.WOODEN_HOE);
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
    public Message teleport(@NotNull br.com.gamemods.minecity.api.world.BlockPos pos)
    {
        WorldDim current = mod.world(sender.worldObj);
        double x = pos.x+0.5, y = pos.y+0.5, z = pos.z+0.5;
        if(current.equals(pos.world))
        {
            sender.dismountRidingEntity();
            sender.setPositionAndUpdate(x, y, z);
            return null;
        }

        WorldServer worldServer = mod.world(pos.world);
        if(worldServer == null)
            return new Message("action.teleport.world-not-found",
                    "The destiny world ${name} was not found or is not loaded",
                    new Object[]{"name", pos.world.name()}
            );

        sender.dismountRidingEntity();
        mod.server.getPlayerList().transferPlayerToDimension(sender, pos.world.dim, worldServer.getDefaultTeleporter());
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
            sender.dismountRidingEntity();
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

        sender.dismountRidingEntity();
        mod.server.getPlayerList().transferPlayerToDimension(sender, pos.world.dim, worldServer.getDefaultTeleporter());
        sender.setPositionAndRotation(pos.x, pos.y, pos.z, pos.yaw, pos.pitch);
        sender.setPositionAndUpdate(pos.x, pos.y, pos.z);

        return null;
    }

    @Override
    public boolean isOp()
    {
        return mod.server.getPlayerList().canSendCommands(sender.getGameProfile());
    }

    public void sendPacket(Packet<?> packet)
    {
        sender.connection.sendPacket(packet);
    }

    @Override
    public void sendBlock(int x, int y, int z)
    {
        sendPacket(new SPacketBlockChange(sender.worldObj, new BlockPos(x, y, z)));
    }

    @Override
    public void sendFakeBlock(int x, int y, int z, Object block)
    {
        SPacketBlockChange packet = new SPacketBlockChange(sender.worldObj, new BlockPos(x, y, z));
        packet.blockState = (IBlockState) block;
    }

    @NotNull
    @Override
    public ForgeSelection<IBlockState> createSelection(@NotNull WorldDim world)
    {
        ForgeSelection<IBlockState> selection = new ForgeSelection<>(world);
        selection.cornerA = Blocks.GLOWSTONE.getDefaultState();
        selection.cornerB = Blocks.LIT_REDSTONE_LAMP.getDefaultState();
        selection.corners = Blocks.SEA_LANTERN.getDefaultState();
        selection.linesA = Blocks.GOLD_BLOCK.getDefaultState();
        selection.linesB = Blocks.LAPIS_BLOCK.getDefaultState();
        selection.extension = Blocks.GLOWSTONE.getDefaultState();
        return selection;
    }
}
