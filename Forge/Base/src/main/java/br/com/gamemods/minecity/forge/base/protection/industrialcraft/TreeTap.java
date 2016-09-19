package br.com.gamemods.minecity.forge.base.protection.industrialcraft;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
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
        if(state.getIBlock().getUnlocalizedName().equals("ic2.rubber_wood"))
        {
            boolean wet;
            Direction to;
            switch(state.getEnumOrdinalOrMeta("state"))
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

            if(face == to)
                return new SingleBlockReaction(pos, wet? PermissionFlag.HARVEST : PermissionFlag.MODIFY)
                        .allowToPickupHarvest(player);
        }
        return NoReaction.INSTANCE;
    }

    @Override
    default Reaction reactBlockPlace(IEntityPlayerMP player, IItemStack stack, boolean offHand,
                                     IBlockSnapshot snap)
    {
        return NoReaction.INSTANCE;
    }
}
