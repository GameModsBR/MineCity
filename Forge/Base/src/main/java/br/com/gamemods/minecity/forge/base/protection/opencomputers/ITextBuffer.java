package br.com.gamemods.minecity.forge.base.protection.opencomputers;

import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.opencomputers.TextBufferTransformer;

@Referenced(at = TextBufferTransformer.class)
public interface ITextBuffer extends Hosted
{
    @Referenced(at = TextBufferTransformer.class)
    IEnvironmentHost host();
}
