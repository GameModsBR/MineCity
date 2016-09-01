package br.com.gamemods.minecity.forge.mc_1_10_2.core.transformer.forge;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.block.GrowMonitorTransformer;
import br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod;
import br.com.gamemods.minecity.forge.mc_1_10_2.protection.MineCityFrostHooks;

@Referenced
public class FrostGrowMonitorTransformer extends GrowMonitorTransformer
{
    @Referenced(at = MineCityFrostCoreMod.class)
    public FrostGrowMonitorTransformer()
    {
        super(MineCityFrostHooks.class.getName());
    }
}
