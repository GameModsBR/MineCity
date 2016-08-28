package br.com.gamemods.minecity.forge.mc_1_7_10.accessors.entity;

import br.com.gamemods.minecity.forge.base.accessors.entity.IEntityFishHook;
import br.com.gamemods.minecity.forge.base.accessors.entity.IEntityPlayerMP;
import net.minecraft.entity.projectile.EntityFishHook;

public interface SevenEntityFishHook extends IEntityFishHook
{
    @Override
    default IEntityPlayerMP getAnger()
    {
        return (IEntityPlayerMP) ((EntityFishHook) this).field_146042_b;
    }
}
