package br.com.gamemods.minecity.forge.base.accessors.entity.item;

import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ForgeInterfaceTransformer;
import net.minecraft.entity.item.EntityXPOrb;
import org.jetbrains.annotations.NotNull;

@Referenced(at = ForgeInterfaceTransformer.class)
public interface IEntityXPOrb extends Pickable
{
    @Override
    default EntityXPOrb getForgeEntity()
    {
        return (EntityXPOrb) this;
    }

    default int getXp()
    {
        return getForgeEntity().xpValue;
    }

    @NotNull
    @Override
    default Type getType()
    {
        return Type.ITEM;
    }
}
