package br.com.gamemods.minecity.forge.mc_1_7_10.accessors.block;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.accessors.block.IBlock;
import br.com.gamemods.minecity.forge.base.accessors.block.IProp;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.item.IItem;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.SevenInterfaceTransformer;
import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Random;

@Referenced(at = SevenInterfaceTransformer.class)
public interface SevenBlock extends IBlock, IState
{
    @Override
    default SevenBlock getIBlock()
    {
        return this;
    }

    @Override
    default Block getForgeBlock()
    {
        return (Block) this;
    }

    default SevenBlock getDefaultIState()
    {
        return this;
    }

    default boolean isOpaqueCube()
    {
        return getForgeBlock().isOpaqueCube();
    }

    @Override
    default int getStateId()
    {
        return 0;
    }

    @Override
    default boolean isReplaceable(IWorldServer world, int x, int y, int z)
    {
        return ((Block) this).isReplaceable((IBlockAccess) world, x, y, z);
    }

    @Override
    default Collection<IProp<?>> getPropertyKeys()
    {
        return SevenMetadataProperty.SINGLETON;
    }

    @Override
    default <T extends Comparable<T>> T getValue(IProp<T> prop)
    {
        if(prop == SevenMetadataProperty.INSTANCE)
            return prop.getValueClass().cast(0);
        throw new NoSuchElementException("Property "+prop+" not found in "+this);
    }

    @Override
    default boolean isSolid()
    {
        return ((Block) this).getMaterial().isSolid();
    }

    @Override
    default IItem getItemDropped(IState state, Random rand, int fortune)
    {
        return (IItem) getForgeBlock().getItemDropped(state.getIntValueOrMeta("metadata"), rand, fortune);
    }

    /**
     * This method is unavailable in Minecraft 1.7.10 servers
     * @throws AbstractMethodError Always thrown. Find an other alternative in 1.7.10
     */
    @Override
    @Deprecated
    IItemStack getItemStack(IState state, IWorldServer world, int x, int y, int z) throws AbstractMethodError;
}
