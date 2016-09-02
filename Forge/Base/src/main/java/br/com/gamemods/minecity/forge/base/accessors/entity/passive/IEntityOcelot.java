package br.com.gamemods.minecity.forge.base.accessors.entity.passive;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ForgeInterfaceTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.SingleBlockReaction;
import net.minecraft.entity.passive.EntityOcelot;

@Referenced(at = ForgeInterfaceTransformer.class)
public interface IEntityOcelot extends IEntityTameable
{
    @Override
    default EntityOcelot getForgeEntity()
    {
        return (EntityOcelot) this;
    }

    @Override
    default Reaction reactPlayerInteraction(ForgePlayer<?, ?, ?> player, IItemStack stack, boolean offHand)
    {
        if(stack != null && !isTamed() && stack.getIItem().getUnlocalizedName().equals("item.fish"))
            return new SingleBlockReaction(getBlockPos(player.getServer()), PermissionFlag.MODIFY);

        return IEntityTameable.super.reactPlayerInteraction(player, stack, offHand);
    }
}
