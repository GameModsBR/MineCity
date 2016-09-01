package br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.entity;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.accessors.entity.vehicle.IVehicle;
import br.com.gamemods.minecity.forge.base.core.transformer.InsertDamageHookTransformer;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod;
import br.com.gamemods.minecity.forge.mc_1_7_10.protection.MineCitySevenHooks;

@Referenced
public class SevenEntityBoatTransformer extends InsertDamageHookTransformer
{
    @Referenced(at = MineCitySevenCoreMod.class)
    public SevenEntityBoatTransformer()
    {
        super("net.minecraft.entity.item.EntityBoat", MineCitySevenHooks.class, "onVehicleDamage", IVehicle.class);
    }
}
