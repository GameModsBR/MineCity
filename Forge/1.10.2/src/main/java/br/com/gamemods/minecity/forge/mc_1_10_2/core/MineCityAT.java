package br.com.gamemods.minecity.forge.mc_1_10_2.core;

import net.minecraftforge.fml.common.asm.transformers.AccessTransformer;

import java.io.IOException;

public class MineCityAT extends AccessTransformer
{
    public MineCityAT() throws IOException
    {
        super("minecity_at.cfg");
    }
}
