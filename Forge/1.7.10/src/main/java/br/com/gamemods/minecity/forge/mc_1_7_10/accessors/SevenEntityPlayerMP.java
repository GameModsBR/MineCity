package br.com.gamemods.minecity.forge.mc_1_7_10.accessors;

import br.com.gamemods.minecity.forge.base.accessors.IEntityPlayerMP;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;

public interface SevenEntityPlayerMP extends IEntityPlayerMP
{
    @Override
    default void send(Packet packet)
    {
        ((EntityPlayerMP) this).playerNetServerHandler.sendPacket(packet);
    }

    @Override
    default void kick(String reason)
    {
        ((EntityPlayerMP) this).playerNetServerHandler.kickPlayerFromServer(reason);
    }
}
