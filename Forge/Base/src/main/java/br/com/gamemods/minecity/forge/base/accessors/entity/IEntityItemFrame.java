package br.com.gamemods.minecity.forge.base.accessors.entity;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.entity.EntityItemFrameTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.SingleBlockReaction;

@Referenced(at = EntityItemFrameTransformer.class)
public interface IEntityItemFrame extends IEntity
{
    @Override
    default Reaction reactPlayerInteraction(ForgePlayer<?,?,?> player, IItemStack stack, boolean offHand)
    {
        return new SingleBlockReaction(getBlockPos(player.getServer()), PermissionFlag.OPEN);
    }
}
