package br.com.gamemods.minecity.forge.base.core.transformer.forge.entity;

import br.com.gamemods.minecity.forge.base.accessors.entity.item.IEntityEndCrystal;
import br.com.gamemods.minecity.forge.base.core.transformer.InsertDamageHookTransformer;

public class EntityEnderCrystalTransformer extends InsertDamageHookTransformer
{
    public EntityEnderCrystalTransformer(Class hookClass)
    {
        super("net.minecraft.entity.item.EntityEnderCrystal", hookClass, "onEntityDamage", IEntityEndCrystal.class);
    }
}
