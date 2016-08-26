package br.com.gamemods.minecity.forge.mc_1_7_10.accessors;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.accessors.IBlock;
import br.com.gamemods.minecity.forge.base.accessors.IProp;
import br.com.gamemods.minecity.forge.base.accessors.IState;
import br.com.gamemods.minecity.forge.base.accessors.IWorldServer;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.SevenBlockTransformer;
import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;

import java.util.Collection;
import java.util.NoSuchElementException;

@Referenced(at = SevenBlockTransformer.class)
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
}
