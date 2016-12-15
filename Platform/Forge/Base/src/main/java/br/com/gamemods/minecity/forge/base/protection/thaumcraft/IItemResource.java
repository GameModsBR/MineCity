package br.com.gamemods.minecity.forge.base.protection.thaumcraft;

import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.entity.item.IEntityItem;
import br.com.gamemods.minecity.forge.base.accessors.item.CombatDropItem;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.reactive.reaction.Reaction;

@Referenced(at = ModInterfacesTransformer.class)
public interface IItemResource extends CombatDropItem
{
    @Override
    default Reaction onPlayerPickup(IEntityPlayerMP entity, IEntity item)
    {
        if(item instanceof IEntityItem)
        {
            IEntityItem drop = (IEntityItem) item;
            int meta = drop.getStack().getMeta();
            if(meta == 11 || meta == 12)
                return onPlayerPickupDoCombat(entity, item, true);
        }

        return CombatDropItem.super.onPlayerPickup(entity, item);
    }
}
