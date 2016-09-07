package br.com.gamemods.minecity.forge.base.protection.enderstorage;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.accessors.block.IBlockOpenReactor;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.block.ITileEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.NoReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.SingleBlockReaction;
import net.minecraft.item.ItemStack;

@Referenced(at = ModInterfacesTransformer.class)
public interface IBlockEnderStorage extends IBlockOpenReactor
{
    @Override
    default Reaction reactRightClick(BlockPos pos, IState state, IEntityPlayerMP player, IItemStack stack,
                                     boolean offHand, Direction face)
    {
        ITileEntity tile = player.getIWorld().getTileEntity(pos);
        if(tile instanceof ITileFrequencyOwner && ((ITileFrequencyOwner) tile).isOwner(player.identity()))
            return NoReaction.INSTANCE;

        if(stack != null)
        {
            ItemStack personalItem = EnderStorageAccessor.getPersonalItem();
            if((personalItem != null && personalItem.getItem() == stack.getItem()) || EnderStorageAccessor.getDyeType(stack.getStack()) != -1)
                return new SingleBlockReaction(pos, PermissionFlag.MODIFY);
        }

        return IBlockOpenReactor.super.reactRightClick(pos, state, player, stack, offHand, face);
    }

    @Override
    default Reaction reactBlockBreak(ForgePlayer<?, ?, ?> player, IState state, BlockPos pos)
    {
        ITileEntity tile = player.cmd.sender.getIWorld().getTileEntity(pos);
        if(tile instanceof ITileFrequencyOwner && ((ITileFrequencyOwner) tile).isOwner(player.identity()))
            return NoReaction.INSTANCE;

        return IBlockOpenReactor.super.reactBlockBreak(player, state, pos);
    }
}
