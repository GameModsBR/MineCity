package br.com.gamemods.minecity.forge.mc_1_7_10;

import br.com.gamemods.minecity.api.command.CommandSender;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.ICommander;
import br.com.gamemods.minecity.forge.base.accessors.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.command.ForgeCommandSender;
import br.com.gamemods.minecity.forge.mc_1_7_10.command.SevenPlayer;
import net.minecraft.entity.player.EntityPlayer;

public class MineCitySeven extends MineCityForge
{
    @Override
    protected SevenPlayer createPlayer(IEntityPlayerMP player)
    {
        return new SevenPlayer(this, player);
    }

    @Override
    public SevenPlayer player(EntityPlayer player)
    {
        return (SevenPlayer) super.player(player);
    }

    @Override
    protected CommandSender createSender(ICommander sender)
    {
        return new ForgeCommandSender<>(this, sender);
    }
}
