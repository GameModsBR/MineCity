package br.com.gamemods.minecity.forge.mc_1_10_2.core.transformer.forge;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.entity.OnImpactTransformer;
import br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod;
import br.com.gamemods.minecity.forge.mc_1_10_2.protection.MineCityFrostHooks;

@Referenced
public class FrostOnImpactTransformer extends OnImpactTransformer
{
    @Referenced(at = MineCityFrostCoreMod.class)
    public FrostOnImpactTransformer()
    {
        super(MineCityFrostHooks.class.getName());
    }
}
