package br.com.gamemods.minecity.forge.base.accessors.entity.item;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ForgeInterfaceTransformer;
import br.com.gamemods.minecity.reactive.reaction.Reaction;
import br.com.gamemods.minecity.reactive.reaction.SingleBlockReaction;

@Referenced(at = ForgeInterfaceTransformer.class)
public interface IEntityLeashKnot extends IEntityHanging
{
    @Override
    default Reaction reactPlayerInteraction(ForgePlayer<?, ?, ?> player, IItemStack stack, boolean offHand)
    {
        return new SingleBlockReaction(getBlockPos(player.getServer()), PermissionFlag.MODIFY);
    }
}
