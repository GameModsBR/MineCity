package br.com.gamemods.minecity.forge.base.protection.forgemultipart;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.reactive.reaction.Reaction;
import br.com.gamemods.minecity.reactive.reaction.SingleBlockReaction;

@Referenced(at = ModInterfacesTransformer.class)
public interface IButtonPart extends ITMultiPart
{
    @Override
    default Reaction reactPlayerActivate(IEntityPlayerMP player, IItemStack stack)
    {
        return new SingleBlockReaction(tileI().getBlockPos(player.getServer()), PermissionFlag.CLICK);
    }
}
