package br.com.gamemods.minecity.forge.base.core.transformer;

import net.minecraftforge.fml.common.asm.transformers.AccessTransformer;

import java.io.IOException;

public class MineCityAT extends AccessTransformer
{
    public MineCityAT() throws IOException
    {
        super("minecity_deobat.cfg");
    }
}
