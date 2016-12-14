package br.com.gamemods.minecity.forge.base.protection.opencomputers;

import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;

@Referenced(at = ModInterfacesTransformer.class)
public interface IAgentComponent
{
    EntityPlayer fakePlayer();
    IInventory inventory();
}
