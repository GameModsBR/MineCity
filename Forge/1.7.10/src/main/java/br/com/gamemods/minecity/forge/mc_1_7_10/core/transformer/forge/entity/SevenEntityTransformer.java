package br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.entity;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.entity.EntityTransformer;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod;

@Referenced
public class SevenEntityTransformer extends EntityTransformer
{
    @Referenced(at = MineCitySevenCoreMod.class)
    public SevenEntityTransformer()
    {
        super("br.com.gamemods.minecity.forge.mc_1_7_10.accessors.entity.SevenEntity");
    }
}
