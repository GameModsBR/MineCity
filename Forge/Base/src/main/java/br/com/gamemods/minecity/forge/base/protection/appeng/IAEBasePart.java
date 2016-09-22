package br.com.gamemods.minecity.forge.base.protection.appeng;

import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.appeng.AEBasePartTransformer;

@Referenced(at = AEBasePartTransformer.class)
public interface IAEBasePart
{
    @Referenced(at = AEBasePartTransformer.class)
    PartHost getHost();
}
