package br.com.gamemods.minecity.forge.accessors;

import br.com.gamemods.minecity.forge.command.ForgePlayer;
import net.minecraft.entity.player.EntityPlayerMP;

public interface IEntityPlayerMP
{
    void setMineCityPlayer(ForgePlayer player);
    ForgePlayer getMineCityPlayer();

    default EntityPlayerMP getEntityPlayerMP()
    {
        return (EntityPlayerMP) this;
    }
}
