package br.com.gamemods.minecity.forge.mc_1_7_10.accessors.block;

import br.com.gamemods.minecity.forge.base.accessors.block.IProp;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import net.minecraft.block.Block;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.NoSuchElementException;

public class SevenBlockState implements IState
{
    public final SevenBlock block;
    public final int meta;

    public SevenBlockState(SevenBlock block, int meta)
    {
        this.block = block;
        this.meta = meta;
    }

    public SevenBlockState(Block block, int meta)
    {
        this((SevenBlock) block, meta);
    }

    @Override
    public SevenBlock getIBlock()
    {
        return block;
    }

    @Override
    public Block getForgeBlock()
    {
        return (Block) block;
    }

    @Override
    public boolean isOpaqueCube()
    {
        return block.isOpaqueCube();
    }

    @Override
    public int getStateId()
    {
        return meta;
    }

    @Override
    public boolean isSolid()
    {
        return block.isSolid();
    }

    @Override
    public Collection<IProp<?>> getPropertyKeys()
    {
        return Collections.singleton(SevenMetadataProperty.INSTANCE);
    }

    @Override
    public <T extends Comparable<T>> T getValue(IProp<T> prop)
    {
        if(prop == SevenMetadataProperty.INSTANCE)
            return prop.getValueClass().cast(meta);
        throw new NoSuchElementException("Property: "+prop+" was not found in "+this);
    }

    @Override
    public Map<IProp<?>, Comparable<?>> getProps()
    {
        return Collections.singletonMap(SevenMetadataProperty.INSTANCE, meta);
    }

    @Override
    public boolean equals(Object o)
    {
        if(this == o) return true;
        if(o == null || !(o instanceof IState)) return false;

        IState that = (IState) o;
        return meta == that.getStateId() && block.equals(that.getIBlock());
    }

    @Override
    public int hashCode()
    {
        int result = block.hashCode();
        result = 31*result + meta;
        return result;
    }
}
