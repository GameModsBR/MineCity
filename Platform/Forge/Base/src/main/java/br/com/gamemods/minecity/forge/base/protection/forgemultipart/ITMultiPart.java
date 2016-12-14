package br.com.gamemods.minecity.forge.base.protection.forgemultipart;

import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.NoReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;

@Referenced(at = ModInterfacesTransformer.class)
public interface ITMultiPart
{
    default ITileMultiPart tileI()
    {
        return MultiPartHooks.tile(this);
    }

    default Reaction reactPlayerClick(IEntityPlayerMP player, IItemStack stack)
    {
        return NoReaction.INSTANCE;
    }

    default Reaction reactPlayerActivate(IEntityPlayerMP player, IItemStack stack)
    {
        return NoReaction.INSTANCE;
    }
}
