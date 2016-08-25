package br.com.gamemods.minecity.forge.mc_1_7_10.command;

import br.com.gamemods.minecity.forge.base.accessors.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.forge.mc_1_7_10.MineCitySeven;
import br.com.gamemods.minecity.forge.mc_1_7_10.protection.SevenMovementListener;
import br.com.gamemods.minecity.forge.mc_1_7_10.protection.SevenMovementMonitor;
import br.com.gamemods.minecity.protection.MovementMonitor;
import net.minecraft.entity.Entity;

public class SevenPlayer extends ForgePlayer<MineCitySeven, IEntityPlayerMP, SevenPlayerSender, Entity>
        implements SevenMovementListener
{
    public SevenPlayer(MineCitySeven mod, IEntityPlayerMP player)
    {
        super(new SevenPlayerSender(mod, player));
    }

    @Override
    protected MovementMonitor<Entity, MineCitySeven> createMonitor()
    {
        return new SevenMovementMonitor(mod, player, mod.block(player), this);
    }
}
