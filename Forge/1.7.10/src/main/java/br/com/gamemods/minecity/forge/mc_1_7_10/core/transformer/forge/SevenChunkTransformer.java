package br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ChunkTransformer;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod;

@Referenced
public class SevenChunkTransformer extends ChunkTransformer
{
    @Referenced(at = MineCitySevenCoreMod.class)
    public SevenChunkTransformer()
    {
        super("br.com.gamemods.minecity.forge.mc_1_7_10.accessors.SevenChunk");
    }
}
