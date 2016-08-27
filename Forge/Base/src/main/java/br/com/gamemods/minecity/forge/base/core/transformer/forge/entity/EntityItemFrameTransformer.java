package br.com.gamemods.minecity.forge.base.core.transformer.forge.entity;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.InsertInterfaceTransformer;

@Referenced
public class EntityItemFrameTransformer extends InsertInterfaceTransformer
{
    @Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
    @Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
    public EntityItemFrameTransformer()
    {
        this("br.com.gamemods.minecity.forge.base.accessors.entity.IEntityItemFrame");
    }

    public EntityItemFrameTransformer(String interfaceClass)
    {
        super("net.minecraft.entity.item.EntityItemFrame", interfaceClass);
    }
}
