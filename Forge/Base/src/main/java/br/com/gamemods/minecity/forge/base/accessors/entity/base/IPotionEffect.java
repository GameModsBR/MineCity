package br.com.gamemods.minecity.forge.base.accessors.entity.base;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ForgeInterfaceTransformer;
import net.minecraft.potion.PotionEffect;

@Referenced(at = ForgeInterfaceTransformer.class)
public interface IPotionEffect
{
    default PotionEffect getForgeObject()
    {
        return (PotionEffect) this;
    }

    default boolean isNegative()
    {
        return getForgeObject().getPotion().isBadEffect();
    }

    default String getName()
    {
        return getForgeObject().getEffectName();
    }
}
