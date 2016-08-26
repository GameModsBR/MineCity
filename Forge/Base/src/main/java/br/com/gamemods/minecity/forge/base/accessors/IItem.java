package br.com.gamemods.minecity.forge.base.accessors;

import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.protection.NoReaction;
import br.com.gamemods.minecity.forge.base.protection.Reaction;
import net.minecraft.item.Item;

public interface IItem
{
    default Item getForgeItem()
    {
        return (Item) this;
    }

    default Reaction react(IEntityPlayerMP player, IItemStack stack, boolean offHand, IState state, BlockPos pos, Direction face)
    {
        return NoReaction.INSTANCE;
    }
}
