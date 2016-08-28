package br.com.gamemods.minecity.forge.base.accessors.item;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.accessors.block.IBlock;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ForgeInterfaceTransformer;
import net.minecraft.item.ItemBlock;

@Referenced(at = ForgeInterfaceTransformer.class)
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
