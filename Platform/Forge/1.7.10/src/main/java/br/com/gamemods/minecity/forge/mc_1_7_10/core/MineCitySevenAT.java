package br.com.gamemods.minecity.forge.mc_1_7_10.core;

import cpw.mods.fml.common.asm.transformers.AccessTransformer;

import java.io.IOException;

public class MineCitySevenAT extends AccessTransformer
{
    public MineCitySevenAT() throws IOException
    {
        super("minecity_at.cfg");
    }
}
