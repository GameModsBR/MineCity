package br.com.gamemods.minecity.forge.base.protection.enderstorage;

import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.entity.item.IEntityItem;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemBlock;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.NoReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import net.minecraft.item.ItemStack;

@Referenced(at = ModInterfacesTransformer.class)
public interface IItemEnderStorage extends IItemBlock
{
    String getOwner(ItemStack stack);

    @Override
    default Reaction onPlayerPickup(IEntityPlayerMP entity, IEntity drop)
    {
        IEntityItem entityItem = (IEntityItem) drop;
        String owner = getOwner(entityItem.getStack().getStack());
        if(!owner.equals("global") && entity.getName().equals(owner))
            return NoReaction.INSTANCE;

        return IItemBlock.super.onPlayerPickup(entity, drop);
    }
}
