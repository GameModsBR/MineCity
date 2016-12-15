package br.com.gamemods.minecity.forge.base.protection.wrcbe;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.forge.base.protection.forgemultipart.ITMultiPart;
import br.com.gamemods.minecity.forge.base.protection.reaction.ForgeSingleBlockReaction;
import br.com.gamemods.minecity.reactive.reaction.Reaction;

@Referenced(at = ModInterfacesTransformer.class)
public interface DevicePart extends ITMultiPart
{
    @Override
    default Reaction reactPlayerActivate(IEntityPlayerMP player, IItemStack stack)
    {
        ForgeSingleBlockReaction reaction = new ForgeSingleBlockReaction(
                tileI().getBlockPos(player.getServer()),
                PermissionFlag.MODIFY
        );
        reaction.onDenyCloseScreen(player);
        return reaction;
    }
}
