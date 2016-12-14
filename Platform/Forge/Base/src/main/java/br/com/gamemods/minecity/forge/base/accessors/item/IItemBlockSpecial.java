package br.com.gamemods.minecity.forge.base.accessors.item;

import br.com.gamemods.minecity.forge.base.accessors.block.IBlock;
import net.minecraft.item.ItemBlockSpecial;

public interface IItemBlockSpecial extends ItemBlockBase
{
    @Override
    default ItemBlockSpecial getForgeItem()
    {
        return (ItemBlockSpecial) this;
    }

    @Override
    default IBlock getIBlock()
    {
        return (IBlock) getForgeItem().block;
    }
}
