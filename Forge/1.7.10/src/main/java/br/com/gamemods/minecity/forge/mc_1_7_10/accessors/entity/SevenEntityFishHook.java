package br.com.gamemods.minecity.forge.mc_1_7_10.accessors.entity;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.entity.projectile.IEntityFishHook;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.SevenInterfaceTransformer;
import net.minecraft.entity.projectile.EntityFishHook;

@Referenced(at = SevenInterfaceTransformer.class)
public interface SevenEntityFishHook extends IEntityFishHook, SevenEntity
{
    @Override
    default IEntityPlayerMP getAnger()
    {
        return (IEntityPlayerMP) ((EntityFishHook) this).field_146042_b;
    }
}
