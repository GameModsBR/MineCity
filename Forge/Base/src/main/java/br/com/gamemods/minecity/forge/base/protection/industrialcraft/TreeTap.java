package br.com.gamemods.minecity.forge.base.protection.industrialcraft;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.ForgeUtil;
import br.com.gamemods.minecity.forge.base.accessors.block.IBlockSnapshot;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItem;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.NoReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.SingleBlockReaction;

@Referenced(at = ModInterfacesTransformer.class)
public interface TreeTap extends IItem
{
    @Override
    default Reaction reactRightClickBlock(IEntityPlayerMP player, IItemStack stack, boolean offHand,
                                          IState state, BlockPos pos, Direction face)
    {
        String blockName = state.getIBlock().getUnlocalizedName();
        boolean sameFace;
        boolean wet;
        if(blockName.equals("ic2.rubber_wood"))
        {
            Integer stateVal = state.getEnumOrdinalValue("state");
            if(stateVal == null)
                return new SingleBlockReaction(pos, PermissionFlag.MODIFY);

            Direction to;
            switch(stateVal)
            {
                case 3: wet = false; to = Direction.NORTH; break;
                case 4: wet = false; to = Direction.SOUTH; break;
                case 5: wet = false; to = Direction.WEST; break;
                case 6: wet = false; to = Direction.EAST; break;
                case 7: wet = true; to = Direction.NORTH; break;
                case 8: wet = true; to = Direction.SOUTH; break;
                case 9: wet = true; to = Direction.WEST; break;
                case 10: wet = true; to = Direction.EAST; break;
                default:
                    return NoReaction.INSTANCE;
            }

            sameFace = to == face;
        }
        else if(blockName.equals("blockRubWood"))
        {
            int meta = state.getIntValueOrMeta("metadata");
            if(meta < 2)
                return NoReaction.INSTANCE;

            sameFace = meta % 6 == ForgeUtil.toForge(face);
            wet = meta < 6;
        }
        else
            return NoReaction.INSTANCE;

        if(sameFace)
            return new SingleBlockReaction(pos, wet? PermissionFlag.HARVEST : PermissionFlag.MODIFY)
                    .allowToPickupHarvest(player);

        return NoReaction.INSTANCE;
    }

    @Override
    default Reaction reactBlockPlace(IEntityPlayerMP player, IItemStack stack, boolean offHand,
                                     IBlockSnapshot snap)
    {
        return NoReaction.INSTANCE;
    }
}
