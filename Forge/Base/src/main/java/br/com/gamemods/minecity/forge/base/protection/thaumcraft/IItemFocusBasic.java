package br.com.gamemods.minecity.forge.base.protection.thaumcraft;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.forge.base.accessors.IRayTraceResult;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItem;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.NoReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.SingleBlockReaction;

@Referenced(at = ModInterfacesTransformer.class)
public interface IItemFocusBasic extends IItem
{
    default Reaction reactFocusRightClick(IItemStack stack, IWorldServer world, IEntityPlayerMP player, IRayTraceResult result)
    {
        if(result != null)
        {
            int hitType = result.getHitType();
            if(hitType == 1)
                return new SingleBlockReaction(result.getHitBlockPos(player.getServer().world(world)), PermissionFlag.MODIFY);
            else if(hitType == 2)
                return result.getEntity().reactPlayerAttackDirect(player, stack, false);
        }

        return NoReaction.INSTANCE;
    }
}
