package br.com.gamemods.minecity.forge.mc_1_7_10.accessors.entity;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.accessors.entity.IEntityArrow;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.SevenInterfaceTransformer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

@Referenced(at = SevenInterfaceTransformer.class)
public interface SevenEntityArrow extends IEntityArrow
{
    @Override
    default IItemStack getIArrowStack()
    {
        return (IItemStack) (Object) new ItemStack(Items.arrow);
    }
}
