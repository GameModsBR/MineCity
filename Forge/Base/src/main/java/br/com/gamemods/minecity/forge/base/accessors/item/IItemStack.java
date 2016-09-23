package br.com.gamemods.minecity.forge.base.accessors.item;

import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ForgeInterfaceTransformer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

@Referenced(at = ForgeInterfaceTransformer.class)
public interface IItemStack
{
    default ItemStack getStack()
    {
        return (ItemStack) (Object) this;
    }

    default int getMeta()
    {
        return getStack().getItemDamage();
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

    default boolean isTool(String tool)
    {
        return getIItem().getToolClasses(this).contains(tool);
    }

    default NBTTagCompound getTag()
    {
        return getStack().getTagCompound();
    }

    default void setMeta(int meta)
    {
        getStack().setItemDamage(meta);
    }

    default void setTag(NBTTagCompound tag)
    {
        getStack().setTagCompound(tag);
    }
}
