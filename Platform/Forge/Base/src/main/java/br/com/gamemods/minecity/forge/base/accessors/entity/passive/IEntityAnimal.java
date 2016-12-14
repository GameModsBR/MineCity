package br.com.gamemods.minecity.forge.base.accessors.entity.passive;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityAgeable;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ForgeInterfaceTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.NoReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.SingleBlockReaction;
import net.minecraft.entity.passive.EntityAnimal;
import org.jetbrains.annotations.NotNull;

@Referenced(at = ForgeInterfaceTransformer.class)
public interface IEntityAnimal extends IEntityAgeable
{
    @Override
    default EntityAnimal getForgeEntity()
    {
        return (EntityAnimal) this;
    }

    @NotNull
    @Override
    default Type getType()
    {
        return Type.ANIMAL;
    }

    default boolean isBreedingItem(IItemStack stack)
    {
        return ((EntityAnimal) this).isBreedingItem(stack.getStack());
    }

    @Override
    default Reaction reactPlayerInteractLiving(ForgePlayer<?, ?, ?> player, IItemStack stack, boolean offHand)
    {
        if(stack != null && !isChild() && isBreedingItem(stack))
            return new SingleBlockReaction(getBlockPos(player.getServer()), PermissionFlag.PVC);

        return NoReaction.INSTANCE;
    }
}
