package br.com.gamemods.minecity.forge.base.accessors.block;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ForgeInterfaceTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.SingleBlockReaction;
import net.minecraft.block.BlockCrops;

@Referenced(at = ForgeInterfaceTransformer.class)
public interface IBlockCrops extends SimpleCrop
{
    @Override
    default BlockCrops getForgeBlock()
    {
        return (BlockCrops) this;
    }

    default int getMaxAge()
    {
        return getForgeBlock().getMaxAge();
    }

    @Override
    default boolean isHarvestAge(int age)
    {
        return age == getMaxAge();
    }

    @Override
    default Reaction reactBlockPlace(ForgePlayer<?, ?, ?> player, IBlockSnapshot snap)
    {
        // Allow to use bone meal
        IItemStack stack = player.cmd.sender.getStackInHand(player.offHand);
        if(stack != null && stack.getItem() != getISeed())
            return new SingleBlockReaction(snap.getPosition(player.getServer()), PermissionFlag.HARVEST);

        return new SingleBlockReaction(snap.getPosition(player.getServer()), PermissionFlag.MODIFY);
    }
}
