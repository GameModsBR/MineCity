package br.com.gamemods.minecity.forge.mc_1_10_2.command;

import br.com.gamemods.minecity.forge.base.accessors.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.forge.mc_1_10_2.MineCityFrost;
import br.com.gamemods.minecity.forge.mc_1_10_2.protection.ForstMovementMonitor;
import br.com.gamemods.minecity.forge.mc_1_10_2.protection.FrostMovementListener;
import br.com.gamemods.minecity.protection.MovementMonitor;
import net.minecraft.entity.Entity;

public class FrostPlayer extends ForgePlayer<MineCityFrost, IEntityPlayerMP, FrostPlayerSender, Entity>
    implements FrostMovementListener
{
    public FrostPlayer(MineCityFrost mod, IEntityPlayerMP sender)
    {
        super(new FrostPlayerSender(mod, sender));
    }

    @Override
    protected MovementMonitor<Entity, MineCityFrost> createMonitor()
    {
        return new ForstMovementMonitor(mod, player, mod.block(player), this);
    }
}
