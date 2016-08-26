package br.com.gamemods.minecity.forge.base.core.transformer.forge.block;

import br.com.gamemods.minecity.forge.base.core.transformer.InsertInterfaceTransformer;

public class BlockSnapshotTransformer extends InsertInterfaceTransformer
{
    public BlockSnapshotTransformer(String interfaceClass)
    {
        super("net.minecraftforge.common.util.BlockSnapshot", interfaceClass);
    }
}
