package br.com.gamemods.minecity.forge.mc_1_10_2.core.transformer.forge;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.accessors.entity.IVehicle;
import br.com.gamemods.minecity.forge.base.core.transformer.InsertDamageHookTransformer;
import br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod;
import br.com.gamemods.minecity.forge.mc_1_10_2.protection.MineCityFrostHooks;

@Referenced
public class FrostEntityMinecartTransformer extends InsertDamageHookTransformer
{
    @Referenced(at = MineCityFrostCoreMod.class)
    public FrostEntityMinecartTransformer()
    {
        super("net.minecraft.entity.item.EntityMinecart", MineCityFrostHooks.class, "onVehicleDamage", IVehicle.class);
    }
}
