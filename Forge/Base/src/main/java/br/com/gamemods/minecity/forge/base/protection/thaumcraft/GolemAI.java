package br.com.gamemods.minecity.forge.base.protection.thaumcraft;

import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityAIBase;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.thaumcraft.GolemHelperAndAITransformer;

@Referenced(at = GolemHelperAndAITransformer.class)
public interface GolemAI extends IEntityAIBase
{
    @Referenced(at = GolemHelperAndAITransformer.class)
    IEntityGolemBase getTheGolem();
}
