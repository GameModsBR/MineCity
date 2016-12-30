package br.com.gamemods.minecity.sponge.core.mixed;

import org.spongepowered.api.item.inventory.ItemStack;

public interface MixedEntityLivingBase
{
    boolean isFlyingOnElytra();
    ItemStack getActiveStack();
}
