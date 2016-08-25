package br.com.gamemods.minecity.forge.base.accessors;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.IBlockStateTransformer;
import net.minecraft.block.Block;

@Referenced(at = IBlockStateTransformer.class)
public interface IState
{
    IBlock getIBlock();
    Block getForgeBlock();

    boolean isOpaqueCube();
}
