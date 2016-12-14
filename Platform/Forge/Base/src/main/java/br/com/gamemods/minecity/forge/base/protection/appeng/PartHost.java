package br.com.gamemods.minecity.forge.base.protection.appeng;

import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.appeng.IPartHostTransformer;

@Referenced(at = IPartHostTransformer.class)
public interface PartHost
{
    @Referenced(at = IPartHostTransformer.class)
    BlockPos getPos(MineCityForge mod);
}
