package br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.MinecraftServerTransformer;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod;

@Referenced
public class SevenMinecraftServerTransformer extends MinecraftServerTransformer
{
    @Referenced(at = MineCitySevenCoreMod.class)
    public SevenMinecraftServerTransformer()
    {
        super("br.com.gamemods.minecity.forge.mc_1_7_10.accessors.SevenMinecraftServer");
    }
}
