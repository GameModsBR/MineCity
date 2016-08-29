package br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.entity;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.entity.EntityEnderCrystalTransformer;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod;
import br.com.gamemods.minecity.forge.mc_1_7_10.protection.MineCitySevenHooks;

@Referenced
public class SevenEntityEnderCrystalTransformer extends EntityEnderCrystalTransformer
{
    @Referenced(at = MineCitySevenCoreMod.class)
    public SevenEntityEnderCrystalTransformer()
    {
        super(MineCitySevenHooks.class);
    }
}
