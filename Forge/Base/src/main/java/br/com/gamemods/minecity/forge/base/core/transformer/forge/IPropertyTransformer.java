package br.com.gamemods.minecity.forge.base.core.transformer.forge;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.InsertInterfaceTransformer;

@Referenced
public class IPropertyTransformer extends InsertInterfaceTransformer
{
    @Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
    public IPropertyTransformer()
    {
        super("net.minecraft.block.properties.IProperty", "br.com.gamemods.minecity.forge.base.accessors.IProp");
    }
}
