package br.com.gamemods.minecity.forge.base.accessors;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ItemDyeTransformer;
import br.com.gamemods.minecity.forge.base.protection.NoReaction;
import br.com.gamemods.minecity.forge.base.protection.Reaction;
import br.com.gamemods.minecity.forge.base.protection.SingleBlockReaction;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockTallGrass;

@Referenced(at = ItemDyeTransformer.class)
public interface IItemDye extends IItem
{
    @Override
    default Reaction react(IEntityPlayerMP player, IItemStack stack, boolean offHand, IState state, BlockPos pos, Direction face)
    {
        assert pos.world.instance != null;
        int meta = stack.getMeta();
        if(meta == 15)
        {
            IBlock block = state.getIBlock();
            if(block instanceof BlockCrops)
                return new SingleBlockReaction(pos, PermissionFlag.HARVEST);
            else if(block instanceof BlockTallGrass &&
                    !(((IWorldServer)pos.world.instance).getIBlock(pos.x, pos.y-1, pos.z) instanceof BlockTallGrass))
                return new SingleBlockReaction(pos.add(Direction.UP), PermissionFlag.MODIFY);
            else
                return NoReaction.INSTANCE;
        }

        if(meta != 3)
            return NoReaction.INSTANCE;

        if(!(state.getIBlock() instanceof BlockOldLog))
            return NoReaction.INSTANCE;

        if(state.getEnumOrdinalOrMeta("variant", m-> m&3) != 3)
            return NoReaction.INSTANCE;

        return new SingleBlockReaction(pos.add(face.getOpposite()), PermissionFlag.MODIFY);
    }
}
