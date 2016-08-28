package br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.entity;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.accessors.entity.IVehicle;
import br.com.gamemods.minecity.forge.base.core.transformer.InsertDamageHookTransformer;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod;
import br.com.gamemods.minecity.forge.mc_1_7_10.protection.MineCitySevenHooks;

@Referenced
public class SevenEntityMinecartTransformer extends InsertDamageHookTransformer
{
    @Referenced(at = MineCitySevenCoreMod.class)
    public SevenEntityMinecartTransformer()
    {
        super("net.minecraft.entity.item.EntityMinecart", MineCitySevenHooks.class, "onVehicleDamage", IVehicle.class);
    }
}
