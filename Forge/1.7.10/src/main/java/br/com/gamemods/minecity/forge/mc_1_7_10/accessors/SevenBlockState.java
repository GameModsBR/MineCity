package br.com.gamemods.minecity.forge.mc_1_7_10.accessors;

import br.com.gamemods.minecity.forge.base.accessors.IState;
import net.minecraft.block.Block;

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
