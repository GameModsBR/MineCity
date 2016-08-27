package br.com.gamemods.minecity.forge.base.accessors.item;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.item.ItemStackTransformer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

@Referenced(at = ItemStackTransformer.class)
public interface IItemStack
{
    default ItemStack getStack()
    {
        return (ItemStack) (Object) this;
    }

    default int getMeta()
    {
        return getStack().getMetadata();
    }

    default IItem getIItem()
    {
        return (IItem) getStack().getItem();
    }

    default Item getItem()
    {
        return getStack().getItem();
    }

    default String getUnlocalizedName()
    {
        return getStack().getUnlocalizedName();
    }

    default int getSize()
    {
        return getStack().stackSize;
    }

    default void setSize(int size)
    {
        getStack().stackSize = size;
    }
}
