package br.com.gamemods.minecity.forge.base.protection.industrialcraft;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.accessors.block.IBlockModifyExtendsOpen;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.reactive.reaction.NoReaction;
import br.com.gamemods.minecity.reactive.reaction.Reaction;
import br.com.gamemods.minecity.reactive.reaction.SingleBlockReaction;

@Referenced(at = ModInterfacesTransformer.class)
public interface IBlockCable extends IBlockMultiID, IBlockModifyExtendsOpen
{
    @Override
    default Reaction reactLeftClick(IEntityPlayerMP player, IState state, BlockPos pos, Direction face, IItemStack stack, boolean offHand)
    {
        if(stack != null && stack.getIItem() instanceof IItemToolCutter)
            return new SingleBlockReaction(pos, PermissionFlag.MODIFY);
        return NoReaction.INSTANCE;
    }
}
