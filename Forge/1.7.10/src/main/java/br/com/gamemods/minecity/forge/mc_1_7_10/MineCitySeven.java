package br.com.gamemods.minecity.forge.mc_1_7_10;

import br.com.gamemods.minecity.api.command.CommandSender;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.ICommander;
import br.com.gamemods.minecity.forge.base.command.ForgeCommandSender;

public class MineCitySeven extends MineCityForge
{
    @Override
    protected CommandSender createSender(ICommander sender)
    {
        return new ForgeCommandSender<>(this, sender);
    }
}
