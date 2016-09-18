package br.com.gamemods.minecity.forge.base.protection.opencomputers;

import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.opencomputers.UpgradeLeashTransformer;
import net.minecraft.entity.Entity;

@Referenced(at = UpgradeLeashTransformer.class)
public interface IUpgradeLeash extends Hosted
{
    Entity host();
}
