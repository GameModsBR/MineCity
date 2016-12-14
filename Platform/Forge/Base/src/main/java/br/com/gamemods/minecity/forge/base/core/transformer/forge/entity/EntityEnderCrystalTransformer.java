package br.com.gamemods.minecity.forge.base.core.transformer.forge.entity;

import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.InsertDamageHookTransformer;

@Referenced
public class EntityEnderCrystalTransformer extends InsertDamageHookTransformer
{
    @Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
    @Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
    public EntityEnderCrystalTransformer()
    {
        super("net.minecraft.entity.item.EntityEnderCrystal", "onEntityDamage", "br.com.gamemods.minecity.forge.base.accessors.entity.item.IEntityEndCrystal");
    }
}
