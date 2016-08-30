package br.com.gamemods.minecity.forge.mc_1_10_2.core.transformer.forge;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.entity.EntityXPOrbTransformer;
import br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod;
import br.com.gamemods.minecity.forge.mc_1_10_2.protection.MineCityFrostHooks;

@Referenced
public class FrostEntityXPOrbTransformer extends EntityXPOrbTransformer
{
    @Referenced(at = MineCityFrostCoreMod.class)
    public FrostEntityXPOrbTransformer()
    {
        super(MineCityFrostHooks.class.getName());
    }
}
