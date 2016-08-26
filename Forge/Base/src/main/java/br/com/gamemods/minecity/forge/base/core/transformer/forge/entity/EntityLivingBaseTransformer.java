package br.com.gamemods.minecity.forge.base.core.transformer.forge.entity;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.InsertInterfaceTransformer;

@Referenced
public class EntityLivingBaseTransformer extends InsertInterfaceTransformer
{
    public EntityLivingBaseTransformer(String interfaceClass)
    {
        super("net.minecraft.entity.EntityLivingBase", interfaceClass);
    }

    @Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
    public EntityLivingBaseTransformer()
    {
        this("br.com.gamemods.minecity.forge.base.accessors.entity.IEntityLivingBase");
    }
}
