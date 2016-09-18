package br.com.gamemods.minecity.forge.base.protection.opencomputers;

import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.opencomputers.MagnetProviderTransformer;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.opencomputers.TransposerTransformer;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.opencomputers.UpgradeInventoryControllerTransformer;

@Referenced(at = MagnetProviderTransformer.class)
@Referenced(at = UpgradeInventoryControllerTransformer.class)
@Referenced(at = TransposerTransformer.class)
public interface Hosted
{
    @Referenced(at = MagnetProviderTransformer.class)
    @Referenced(at = UpgradeInventoryControllerTransformer.class)
    @Referenced(at = TransposerTransformer.class)
    Object host();
}
