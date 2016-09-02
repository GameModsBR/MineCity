package br.com.gamemods.minecity.forge.base.accessors.item;

import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ForgeInterfaceTransformer;
import net.minecraft.item.ItemGlassBottle;

@Referenced(at = ForgeInterfaceTransformer.class)
public interface IItemGlassBottle extends IItem
{
    @Override
    default ItemGlassBottle getForgeItem()
    {
        return (ItemGlassBottle) this;
    }
}
