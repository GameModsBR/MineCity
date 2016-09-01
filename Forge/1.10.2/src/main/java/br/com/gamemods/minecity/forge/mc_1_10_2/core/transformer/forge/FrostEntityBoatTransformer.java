package br.com.gamemods.minecity.forge.mc_1_10_2.core.transformer.forge;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.accessors.entity.vehicle.IVehicle;
import br.com.gamemods.minecity.forge.base.core.transformer.InsertDamageHookTransformer;
import br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod;
import br.com.gamemods.minecity.forge.mc_1_10_2.protection.MineCityFrostHooks;

@Referenced
public class FrostEntityBoatTransformer extends InsertDamageHookTransformer
{
    @Referenced(at = MineCityFrostCoreMod.class)
    public FrostEntityBoatTransformer()
    {
        super("net.minecraft.entity.item.EntityBoat", MineCityFrostHooks.class, "onVehicleDamage", IVehicle.class);
    }
}
