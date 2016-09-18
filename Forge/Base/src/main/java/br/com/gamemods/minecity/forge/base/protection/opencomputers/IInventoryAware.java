package br.com.gamemods.minecity.forge.base.protection.opencomputers;

import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import scala.Option;
import scala.collection.IndexedSeq;

@Referenced(at = ModInterfacesTransformer.class)
public interface IInventoryAware
{
    EntityPlayer fakePlayer();

    IInventory inventory();

    int selectedSlot();

    IndexedSeq<Object> insertionSlots();

    Option<ItemStack> stackInSlot(int slot);
}
