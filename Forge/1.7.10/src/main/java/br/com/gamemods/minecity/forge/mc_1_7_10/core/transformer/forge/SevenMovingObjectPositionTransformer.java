package br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.InsertInterfaceTransformer;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod;

@Referenced
public class SevenMovingObjectPositionTransformer extends InsertInterfaceTransformer
{
    @Referenced(at = MineCitySevenCoreMod.class)
    public SevenMovingObjectPositionTransformer()
    {
        super(
                "net.minecraft.util.MovingObjectPosition",
                "br.com.gamemods.minecity.forge.mc_1_7_10.accessors.SevenMovingObjectPositionTransformer"
        );
    }
}
