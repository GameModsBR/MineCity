package br.com.gamemods.minecity.forge.mc_1_10_2.core.transformer.forge;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.BlockSnapshotTransformer;
import br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod;

@Referenced
public class FrostBlockSnapshotTransformer extends BlockSnapshotTransformer
{
    @Referenced(at = MineCityFrostCoreMod.class)
    public FrostBlockSnapshotTransformer()
    {
        super("br.com.gamemods.minecity.forge.mc_1_10_2.accessors.FrostBlockSnapshot");
    }
}
