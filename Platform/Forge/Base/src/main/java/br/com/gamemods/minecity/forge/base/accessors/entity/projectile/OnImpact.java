package br.com.gamemods.minecity.forge.base.accessors.entity.projectile;

import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.entity.OnImpactTransformer;

@Referenced
public interface OnImpact
{
    @Referenced(at = OnImpactTransformer.class)
    void mineCityOnImpact(Object object);
}
