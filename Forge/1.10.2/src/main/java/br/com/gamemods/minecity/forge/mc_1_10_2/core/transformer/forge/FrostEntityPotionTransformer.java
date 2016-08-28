package br.com.gamemods.minecity.forge.mc_1_10_2.core.transformer.forge;

import br.com.gamemods.minecity.forge.base.core.transformer.forge.entity.EntityPotionTransformer;
import br.com.gamemods.minecity.forge.mc_1_10_2.protection.MineCityFrostHooks;
import net.minecraft.util.math.RayTraceResult;

public class FrostEntityPotionTransformer extends EntityPotionTransformer
{
    public FrostEntityPotionTransformer()
    {
        super(RayTraceResult.class.getName(), MineCityFrostHooks.class.getName(), 1);
    }
}
