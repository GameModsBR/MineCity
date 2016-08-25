package br.com.gamemods.minecity.forge.base.accessors;

import net.minecraft.entity.player.EntityPlayerMP;

import java.util.List;

public interface IPlayerList
{
    List<EntityPlayerMP> getPlayerEntities();
    List<IEntityPlayerMP> getIPlayers();
}
