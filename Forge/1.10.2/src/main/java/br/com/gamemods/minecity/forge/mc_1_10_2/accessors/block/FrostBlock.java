package br.com.gamemods.minecity.forge.mc_1_10_2.accessors.block;

import br.com.gamemods.minecity.forge.base.accessors.block.IBlock;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.item.IItem;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.mc_1_10_2.core.transformer.forge.FrostInterfaceTransformer;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.List;
import java.util.Random;

@Referenced(at = FrostInterfaceTransformer.class)
public interface FrostBlock extends IBlock
{
    default FrostState getDefaultIState()
    {
        return (FrostState) ((Block) this).getDefaultState();
    }

    @Override
    default boolean isReplaceable(IWorldServer world, int x, int y, int z)
    {
        return ((Block) this).isReplaceable((IBlockAccess) world, new BlockPos(x, y, z));
    }

    @Override
    default IItem getItemDropped(IState state, Random rand, int fortune)
    {
        return (IItem) getForgeBlock().getItemDropped((IBlockState) state, rand, fortune);
    }

    @SuppressWarnings("deprecation")
    @Override
    default IItemStack getItemStack(IState state, IWorldServer world, int x, int y, int z)
    {
        return (IItemStack)(Object) getForgeBlock().getItem((World) world, new BlockPos(x,y,z), (IBlockState) state);
    }

    @SuppressWarnings("unchecked")
    @Override
    default List<IItemStack> getDrops(IWorldServer world, IState state, int fortune, int x, int y, int z)
    {
        return (List) ((Block) this).getDrops((WorldServer) world, new BlockPos(x, y, z), (IBlockState) state, fortune);
    }

    @SuppressWarnings("deprecation")
    @Override
    default float getHardness(IState state, IWorldServer world, int x, int y, int z)
    {
        return ((Block) this).getBlockHardness((IBlockState) state, (World) world, new BlockPos(x, y, z));
    }
}
