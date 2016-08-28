package br.com.gamemods.minecity.forge.base.accessors.item;

import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.entity.IEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ForgeInterfaceTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.NoReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import net.minecraft.item.Item;

@Referenced(at = ForgeInterfaceTransformer.class)
public interface IItem
{
    default Item getForgeItem()
    {
        return (Item) this;
    }

    default Reaction reactRightClickBlock(IEntityPlayerMP player, IItemStack stack, boolean offHand, IState state, BlockPos pos, Direction face)
    {
        return NoReaction.INSTANCE;
    }

    default String getUnlocalizedName()
    {
        return getForgeItem().getUnlocalizedName();
    }

    default Reaction reactRightClick(IEntityPlayerMP player, IItemStack stack, boolean offHand)
    {
        return NoReaction.INSTANCE;
    }

    default Reaction reactInteractEntity(IEntityPlayerMP player, IEntity target, IItemStack stack, boolean offHand)
    {
        return NoReaction.INSTANCE;
    }
}
