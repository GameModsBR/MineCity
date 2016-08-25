package br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.InsertInterfaceTransformer;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod;

@Referenced
public class ServerConfigurationManagerTransformer extends InsertInterfaceTransformer
{
    @Referenced(at = MineCitySevenCoreMod.class)
    public ServerConfigurationManagerTransformer()
    {
        super(
                "net.minecraft.server.management.ServerConfigurationManager",
                "br.com.gamemods.minecity.forge.mc_1_7_10.accessors.SevenPlayerList"
        );
    }
}
