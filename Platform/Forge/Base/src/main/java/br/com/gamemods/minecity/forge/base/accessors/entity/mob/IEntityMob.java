package br.com.gamemods.minecity.forge.base.accessors.entity.mob;

import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityCreature;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ForgeInterfaceTransformer;
import org.jetbrains.annotations.NotNull;

@Referenced(at = ForgeInterfaceTransformer.class)
public interface IEntityMob extends IEntityCreature
{
    @NotNull
    @Override
    default Type getType()
    {
        return Type.MONSTER;
    }
}
