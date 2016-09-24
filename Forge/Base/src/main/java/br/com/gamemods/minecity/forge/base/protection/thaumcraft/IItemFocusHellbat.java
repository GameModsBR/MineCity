package br.com.gamemods.minecity.forge.base.protection.thaumcraft;

import br.com.gamemods.minecity.forge.base.accessors.IRayTraceResult;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityLivingBase;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.NoReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.ObservedReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;

@Referenced(at = ModInterfacesTransformer.class)
public interface IItemFocusHellbat extends IItemFocusBasic
{
    @Override
    default Reaction reactFocusRightClick(IItemStack stack, IWorldServer world, IEntityPlayerMP player,
                                          IRayTraceResult result)
    {
        IEntity pointed = ThaumHooks.getPointedEntity(world, player, 32, IEntityFireBat.class);
        if(pointed instanceof IEntityLivingBase)
            return new ObservedReaction(pointed.reactPlayerAttackDirect(player, null, true))
                    .addAllowListener(r-> ThaumHooks.fireBatSpawner = player.getUniqueID()
            );

        return NoReaction.INSTANCE;
    }
}
