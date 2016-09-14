package br.com.gamemods.minecity.forge.base.protection.mrcrayfishfurniture;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.accessors.block.IBlockOpenReactor;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.BlameOtherInheritedReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.SingleBlockReaction;
import net.minecraft.world.World;

import java.util.Arrays;

@Referenced(at = ModInterfacesTransformer.class)
public interface IBlockBath extends IBlockOpenReactor
{
    int[] getOtherBathCoords(World world, int x, int y, int z);

    default int[] getBathInfo(IWorldServer world, int x, int y, int z)
    {
        int[] coords = getOtherBathCoords((World) world, x, y, z);
        int metadata = world.getIState(x, y, z).getIntValueOrMeta("metadata");

        int[] firstCoords;
        switch(metadata)
        {
            case 0:
                firstCoords = new int[]{x, y, z + 1};
                break;
            case 1:
                firstCoords = new int[]{x - 1, y, z};
                break;
            case 2:
                firstCoords = new int[]{x, y, z - 1};
                break;
            default:
                firstCoords = new int[]{x + 1, y, z};
        }

        boolean first = Arrays.equals(coords, firstCoords);
        firstCoords = Arrays.copyOf(coords, 4);
        firstCoords[3] = first? 1 : 2;
        return firstCoords;
    }

    @Override
    default Reaction reactRightClick(BlockPos pos, IState state, IEntityPlayerMP player, IItemStack stack,
                                     boolean offHand, Direction face)
    {
        IWorldServer world = pos.world.getInstance(IWorldServer.class);
        int[] bathInfo = getBathInfo(world, pos.x, pos.y, pos.z);
        Reaction reaction = new SingleBlockReaction(pos, PermissionFlag.OPEN);
        if(bathInfo[3] == 2)
        {
            BlockPos water = pos.add(Direction.DOWN, 2);
            if(world.getIBlock(water).getUnlocalizedName().equals("tile.water"))
                reaction = new BlameOtherInheritedReaction(player.getServer().mineCity, pos,
                        new SingleBlockReaction(water, PermissionFlag.MODIFY)
                );
        }

        return reaction;
    }
}
