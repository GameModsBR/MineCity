package br.com.gamemods.minecity.forge.base.protection.thaumcraft;

import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.forge.base.accessors.block.IBlockSnapshot;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItem;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.reactive.reaction.Reaction;

import java.util.Collection;

@Referenced(at = ModInterfacesTransformer.class)
public interface IItemArcaneDoor extends IItem
{
    @Override
    default Reaction reactBlockMultiPlace(IEntityPlayerMP entity, IItemStack hand, boolean offHand,
                                          BlockPos blockPos, Collection<IBlockSnapshot> snapshots)
    {
        return Reaction.combine(snapshots.stream().map(snap-> snap.getCurrentState().getIBlock().reactBlockPlace(
                entity.getServer().player(entity), snap, hand, offHand
        )));
    }
}
