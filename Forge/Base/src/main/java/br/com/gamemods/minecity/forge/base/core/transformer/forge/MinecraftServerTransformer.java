package br.com.gamemods.minecity.forge.base.core.transformer.forge;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.InsertInterfaceTransformer;

@Referenced
public class MinecraftServerTransformer extends InsertInterfaceTransformer
{
    public MinecraftServerTransformer(String interfaceClass)
    {
        super("net.minecraft.server.MinecraftServer", interfaceClass);
    }

    @Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
    public MinecraftServerTransformer()
    {
        this("br.com.gamemods.minecity.forge.base.accessors.IMinecraftServer");
    }
}
