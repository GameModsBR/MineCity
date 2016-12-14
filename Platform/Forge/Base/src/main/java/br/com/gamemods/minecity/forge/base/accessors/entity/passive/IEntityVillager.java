package br.com.gamemods.minecity.forge.base.accessors.entity.passive;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityAgeable;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ForgeInterfaceTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.SingleBlockReaction;
import org.jetbrains.annotations.NotNull;

@Referenced(at = ForgeInterfaceTransformer.class)
public interface IEntityVillager extends IEntityAgeable
{
    @Override
    default Reaction reactPlayerInteractLiving(ForgePlayer<?, ?, ?> player, IItemStack stack, boolean offHand)
    {
        return new SingleBlockReaction(getBlockPos(player.getServer()), PermissionFlag.HARVEST);
    }

    @NotNull
    @Override
    default Type getType()
    {
        return Type.ANIMAL;
    }
}
