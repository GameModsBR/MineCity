package br.com.gamemods.minecity.forge.base.protection.opencomputers;

import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.opencomputers.UpgradePistonTransformer;

@Referenced(at = UpgradePistonTransformer.class)
public interface IUpgradePiston extends Hosted
{
    @Referenced(at = UpgradePistonTransformer.class)
    IEnvironmentHost host();
}
