package br.com.gamemods.minecity.forge.mc_1_10_2.core.transformer.forge;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.entity.EntityEnderCrystalTransformer;
import br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod;
import br.com.gamemods.minecity.forge.mc_1_10_2.protection.MineCityFrostHooks;

@Referenced
public class FrostEntityEnderCrystalTransformer extends EntityEnderCrystalTransformer
{
    @Referenced(at = MineCityFrostCoreMod.class)
    public FrostEntityEnderCrystalTransformer()
    {
        super(MineCityFrostHooks.class);
    }
}
