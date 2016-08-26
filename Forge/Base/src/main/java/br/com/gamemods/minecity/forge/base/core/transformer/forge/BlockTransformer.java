package br.com.gamemods.minecity.forge.base.core.transformer.forge;

import br.com.gamemods.minecity.forge.base.core.transformer.InsertInterfaceTransformer;

public class BlockTransformer extends InsertInterfaceTransformer
{
    public BlockTransformer()
    {
        super("net.minecraft.block.Block", "br.com.gamemods.minecity.forge.base.accessors.IBlock");
    }

    public BlockTransformer(String interfaceName)
    {
        super("net.minecraft.block.Block", interfaceName);
    }
}
