package br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ChunkTransformer;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCityForge7CoreMod;

@Referenced(at = MineCityForge7CoreMod.class)
public class Forge7ChunkTransformer extends ChunkTransformer
{
    public Forge7ChunkTransformer()
    {
        super("br.com.gamemods.minecity.forge.mc_1_7_10.accessors.IChunk7");
    }
}
