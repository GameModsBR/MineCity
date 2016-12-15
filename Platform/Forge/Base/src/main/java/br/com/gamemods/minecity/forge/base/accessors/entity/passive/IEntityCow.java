package br.com.gamemods.minecity.forge.base.accessors.entity.passive;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ForgeInterfaceTransformer;
import br.com.gamemods.minecity.reactive.reaction.NoReaction;
import br.com.gamemods.minecity.reactive.reaction.Reaction;
import br.com.gamemods.minecity.reactive.reaction.SingleBlockReaction;

@Referenced(at = ForgeInterfaceTransformer.class)
public interface IEntityCow extends IEntityAnimal
{
    @Override
    default Reaction reactPlayerInteractLiving(ForgePlayer<?, ?, ?> player, IItemStack stack, boolean offHand)
    {
        if(stack != null && !isChild())
        {
            if(stack.getIItem().getUnlocalizedName().equals("item.bucket"))
                return new SingleBlockReaction(getBlockPos(player.getServer()), PermissionFlag.HARVEST);

            if(isBreedingItem(stack))
                return new SingleBlockReaction(getBlockPos(player.getServer()), PermissionFlag.PVC);
        }

        return NoReaction.INSTANCE;
    }
}
