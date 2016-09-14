package br.com.gamemods.minecity.forge.mc_1_7_10.accessors.block;

import br.com.gamemods.minecity.forge.base.accessors.block.IBlock;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.item.IItem;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.SevenInterfaceTransformer;
import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

@Referenced(at = SevenInterfaceTransformer.class)
public interface SevenBlock extends IBlock
{
    @Override
    default Block getForgeBlock()
    {
        return (Block) this;
    }

    default SevenBlockState getDefaultIState()
    {
        return new SevenBlockState(this, 0);
    }

    default boolean isOpaqueCube()
    {
        return getForgeBlock().isOpaqueCube();
    }

    @Override
    default boolean isReplaceable(IWorldServer world, int x, int y, int z)
    {
        return ((Block) this).isReplaceable((IBlockAccess) world, x, y, z);
    }

    default boolean isSolid()
    {
        return ((Block) this).getMaterial().isSolid();
    }

    @Override
    default IItem getItemDropped(IState state, Random rand, int fortune)
    {
        return (IItem) getForgeBlock().getItemDropped(state.getIntValueOrMeta("metadata"), rand, fortune);
    }

    @SuppressWarnings("unchecked")
    @Override
    default List<IItemStack> getDrops(IWorldServer world, IState state, int fortune, int x, int y, int z)
    {
        return (List) ((Block) this).getDrops((World) world, x, y, z, state.getIntValueOrMeta("metadata"), fortune);
    }

    /**
     * This method is unavailable in Minecraft 1.7.10 servers
     * @throws AbstractMethodError Always thrown. Find an other alternative in 1.7.10
     */
    @Override
    @Deprecated
    IItemStack getItemStack(IState state, IWorldServer world, int x, int y, int z) throws AbstractMethodError;

    @Override
    default float getHardness(IState state, IWorldServer world, int x, int y, int z)
    {
        return ((Block) this).getBlockHardness((World) world, z, y, z);
    }
}
