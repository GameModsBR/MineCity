package br.com.gamemods.minecity.forge.base.protection.industrialcraft;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.reactive.reaction.Reaction;
import br.com.gamemods.minecity.reactive.reaction.SingleBlockReaction;

@Referenced(at = ModInterfacesTransformer.class)
public interface IItemScrapbox extends IItemIC2
{
    @Override
    default Reaction reactRightClick(IEntityPlayerMP player, IItemStack stack, boolean offHand)
    {
        return new SingleBlockReaction(player.getBlockPos(player.getServer()), PermissionFlag.PICKUP);
    }

    @Override
    default boolean isHarvest(IItemStack stack)
    {
        return false;
    }
}
