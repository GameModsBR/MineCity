package br.com.gamemods.minecity.forge.base.accessors.block;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.accessors.item.IItem;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.block.BlockCropsTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.SingleBlockReaction;
import net.minecraft.block.BlockCrops;

@Referenced(at = BlockCropsTransformer.class)
public interface IBlockCrops extends IBlock
{
    @Override
    default BlockCrops getForgeBlock()
    {
        return (BlockCrops) this;
    }

    default IItem getISeed()
    {
        BlockCrops crops = (BlockCrops) this;
        return (IItem) crops.getItemDropped(crops.getStateFromMeta(0), null, 0);
    }

    @Override
    default Reaction reactBlockPlace(ForgePlayer<?, ?, ?> player, IBlockSnapshot snap)
    {
        IItemStack stack = player.cmd.sender.getStackInHand(player.offHand);
        if(stack != null && stack.getItem() != getISeed())
            return new SingleBlockReaction(snap.getPosition(player.getServer()), PermissionFlag.HARVEST);

        return new SingleBlockReaction(snap.getPosition(player.getServer()), PermissionFlag.MODIFY);
    }
}
