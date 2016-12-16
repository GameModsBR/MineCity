package br.com.gamemods.minecity.sponge.core.mixin;

import br.com.gamemods.minecity.reactive.ReactiveLayer;
import br.com.gamemods.minecity.reactive.game.item.data.ItemData;
import br.com.gamemods.minecity.sponge.core.mixed.MixedItemType;
import net.minecraft.item.Item;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@SuppressWarnings("ClassHasNoToStringMethod")
@Mixin(Item.class)
public abstract class MixinItem implements MixedItemType
{
    @Nullable
    private ItemData itemData;

    @NotNull
    @Override
    public ItemData getItemData()
    {
        if(itemData != null)
            return itemData;

        return itemData = ReactiveLayer.getItemManipulator().getItemData(this).get();
    }
}
