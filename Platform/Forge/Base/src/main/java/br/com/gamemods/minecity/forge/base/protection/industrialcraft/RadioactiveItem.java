package br.com.gamemods.minecity.forge.base.protection.industrialcraft;

import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.CombatDropItem;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;

@Referenced(at = ModInterfacesTransformer.class)
public interface RadioactiveItem extends CombatDropItem
{
    @Override
    default Reaction onPlayerPickup(IEntityPlayerMP player, IEntity item)
    {
        if(ICHooks.hasCompleteHazmat(player))
            return CombatDropItem.super.onPlayerPickup(player, item);

        return onPlayerPickupDoCombat(player, item, false);
    }
}
