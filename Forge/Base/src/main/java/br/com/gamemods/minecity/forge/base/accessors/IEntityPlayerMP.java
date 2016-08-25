package br.com.gamemods.minecity.forge.base.accessors;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.command.IForgePlayer;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.EntityPlayerMPTransformer;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;

@Referenced(at = EntityPlayerMPTransformer.class)
public interface IEntityPlayerMP extends IEntity, ICommander
{
    void setMineCityPlayer(IForgePlayer player);
    IForgePlayer getMineCityPlayer();

    default EntityPlayerMP getEntityPlayerMP()
    {
        return (EntityPlayerMP) this;
    }

    default PlayerID getIdentity()
    {
        IForgePlayer player = getMineCityPlayer();
        if(player != null)
            return player.getPlayerId();

        return new PlayerID(getUniqueID(), getName());
    }

    @Override
    default String getName()
    {
        return IEntity.super.getName();
    }

    default GameProfile getGameProfile()
    {
        return getEntityPlayerMP().getGameProfile();
    }

    default void send(Packet packet)
    {
        ((EntityPlayerMP) this).connection.sendPacket(packet);
    }

    default void kick(String reason)
    {
        ((EntityPlayerMP) this).connection.kickPlayerFromServer(reason);
    }
}
