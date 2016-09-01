package br.com.gamemods.minecity.forge.mc_1_10_2.core.transformer.forge;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.entity.EntityArmorStandTransformer;
import br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod;
import br.com.gamemods.minecity.forge.mc_1_10_2.protection.MineCityFrostHooks;

/**
 * @deprecated Used to fire a custom event but there's a forge event available for the same thing.
 */
@Deprecated
@Referenced
public class FrostEntityArmorStandTransformer extends EntityArmorStandTransformer
{
    @Referenced(at = MineCityFrostCoreMod.class)
    public FrostEntityArmorStandTransformer()
    {
        super(MineCityFrostHooks.class.getName());
    }
}
