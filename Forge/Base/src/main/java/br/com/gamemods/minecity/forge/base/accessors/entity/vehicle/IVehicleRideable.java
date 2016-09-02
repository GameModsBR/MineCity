package br.com.gamemods.minecity.forge.base.accessors.entity.vehicle;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ForgeInterfaceTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.NoReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.SingleBlockReaction;

@Referenced(at = ForgeInterfaceTransformer.class)
public interface IVehicleRideable extends IVehicle
{
    @Override
    default Reaction reactPlayerInteraction(ForgePlayer<?, ?, ?> player, IItemStack stack, boolean offHand)
    {
        if(player.identity().equals(getVehicleOwner()))
            return NoReaction.INSTANCE;

        return new SingleBlockReaction(getBlockPos(player.getServer()), PermissionFlag.RIDE);
    }
}
