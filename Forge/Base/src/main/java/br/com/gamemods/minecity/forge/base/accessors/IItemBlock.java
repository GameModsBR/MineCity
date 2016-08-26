package br.com.gamemods.minecity.forge.base.accessors;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ItemBlockTransformer;
import net.minecraft.item.ItemBlock;

@Referenced(at = ItemBlockTransformer.class)
public interface IItemBlock extends IItem
{
    @Override
    default ItemBlock getForgeItem()
    {
        return (ItemBlock) this;
    }

    default IBlock getIBlock()
    {
        return (IBlock) ((ItemBlock) this).getBlock();
    }
}
