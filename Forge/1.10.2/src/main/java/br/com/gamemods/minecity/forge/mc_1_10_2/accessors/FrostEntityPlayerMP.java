package br.com.gamemods.minecity.forge.mc_1_10_2.accessors;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.accessors.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.IState;
import br.com.gamemods.minecity.forge.mc_1_10_2.core.transformer.forge.FrostEntityPlayerMPTransformer;
import io.netty.buffer.Unpooled;
import net.minecraft.block.state.IBlockState;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.util.math.BlockPos;

@Referenced(at = FrostEntityPlayerMPTransformer.class)
public interface FrostEntityPlayerMP extends IEntityPlayerMP
{
    @Override
    default void sendBlock(int x, int y, int z)
    {
        sendPacket(new SPacketBlockChange(getWorld(), new BlockPos(x, y, z)));
    }

    @Override
    default void sendFakeBlock(int x, int y, int z, IState state)
    {
        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer(8 + 4));
        buffer.writeBlockPos(new BlockPos(x, y, z));
        buffer.writeVarIntToBuffer(0);

        try
        {
            SPacketBlockChange packet = new SPacketBlockChange();
            packet.readPacketData(buffer);

            packet.blockState = (IBlockState) state;
            sendPacket(packet);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}
