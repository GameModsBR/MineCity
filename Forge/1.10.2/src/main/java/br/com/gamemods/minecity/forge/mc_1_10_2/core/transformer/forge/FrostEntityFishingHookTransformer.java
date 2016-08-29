package br.com.gamemods.minecity.forge.mc_1_10_2.core.transformer.forge;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.entity.EntityFishingHookTransformer;
import br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod;
import br.com.gamemods.minecity.forge.mc_1_10_2.protection.MineCityFrostHooks;
import net.minecraft.util.math.RayTraceResult;

@Referenced
public class FrostEntityFishingHookTransformer extends EntityFishingHookTransformer
{
    @Referenced(at = MineCityFrostCoreMod.class)
    public FrostEntityFishingHookTransformer()
    {
        super(RayTraceResult.class.getName(), MineCityFrostHooks.class.getName());
    }
}
