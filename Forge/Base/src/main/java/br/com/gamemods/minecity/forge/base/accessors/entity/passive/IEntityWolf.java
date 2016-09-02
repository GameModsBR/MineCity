package br.com.gamemods.minecity.forge.base.accessors.entity.passive;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ForgeInterfaceTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.SingleBlockReaction;
import net.minecraft.entity.passive.EntityWolf;

@Referenced(at = ForgeInterfaceTransformer.class)
public interface IEntityWolf extends IEntityTameable
{
    @Override
    default EntityWolf getForgeEntity()
    {
        return (EntityWolf) this;
    }

    default boolean isAngry()
    {
        return getForgeEntity().isAngry();
    }

    @Override
    default Reaction reactPlayerInteraction(ForgePlayer<?, ?, ?> player, IItemStack stack, boolean offHand)
    {
        if(stack != null && !isTamed() && !isAngry() && stack.getIItem().getUnlocalizedName().equals("item.bone"))
            return new SingleBlockReaction(getBlockPos(player.getServer()), PermissionFlag.MODIFY);

        return IEntityTameable.super.reactPlayerInteraction(player, stack, offHand);
    }
}
