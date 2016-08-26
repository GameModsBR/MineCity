package br.com.gamemods.minecity.forge.mc_1_10_2.accessors.block;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.accessors.block.IProp;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.mc_1_10_2.core.transformer.forge.FrostIBlockStateTransformer;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.fml.common.registry.GameData;

import java.util.Collection;
import java.util.Map;

@Referenced(at = FrostIBlockStateTransformer.class)
public interface FrostState extends IState
{
    default IBlockState getForgeState()
    {
        return (IBlockState) this;
    }

    default Block getForgeBlock()
    {
        return ((IBlockState) this).getBlock();
    }

    default FrostBlock getIBlock()
    {
        return (FrostBlock) ((IBlockState) this).getBlock();
    }

    default boolean isOpaqueCube()
    {
        return getForgeState().isOpaqueCube();
    }

    @Override
    default int getStateId()
    {
        return GameData.getBlockStateIDMap().get((IBlockState) this);
    }

    @SuppressWarnings("unchecked")
    @Override
    default Collection<IProp<?>> getPropertyKeys()
    {
        return (Collection) ((IBlockState) this).getPropertyNames();
    }

    @SuppressWarnings("unchecked")
    @Override
    default <T extends Comparable<T>> T getValue(IProp<T> prop)
    {
        return ((IBlockState) this).getValue((IProperty<T>) prop);
    }

    @SuppressWarnings("unchecked")
    @Override
    default Map<IProp<?>, Comparable<?>> getProps()
    {
        return (Map) ((IBlockState) this).getProperties();
    }
}
