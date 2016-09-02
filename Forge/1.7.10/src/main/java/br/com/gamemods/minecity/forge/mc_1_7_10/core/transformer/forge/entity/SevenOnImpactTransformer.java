package br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.entity;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.entity.OnImpactTransformer;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod;
import br.com.gamemods.minecity.forge.mc_1_7_10.protection.MineCitySevenHooks;

@Referenced
public class SevenOnImpactTransformer extends OnImpactTransformer
{
    @Referenced(at = MineCitySevenCoreMod.class)
    public SevenOnImpactTransformer()
    {
        super(MineCitySevenHooks.class.getName());
    }
}
