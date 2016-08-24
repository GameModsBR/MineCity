package br.com.gamemods.minecity.forge.base.accessors;

import br.com.gamemods.minecity.forge.base.command.IForgePlayer;
import net.minecraft.entity.player.EntityPlayerMP;

public interface IEntityPlayerMP
{
    void setMineCityPlayer(IForgePlayer player);
    IForgePlayer getMineCityPlayer();

    default EntityPlayerMP getEntityPlayerMP()
    {
        return (EntityPlayerMP) this;
    }
}
