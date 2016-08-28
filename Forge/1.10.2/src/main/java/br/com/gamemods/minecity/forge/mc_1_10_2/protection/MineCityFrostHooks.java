package br.com.gamemods.minecity.forge.mc_1_10_2.protection;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.mc_1_10_2.core.transformer.forge.FrostEntityBoatTransformer;
import br.com.gamemods.minecity.forge.mc_1_10_2.core.transformer.forge.FrostEntityPotionTransformer;
import br.com.gamemods.minecity.forge.mc_1_10_2.event.PotionApplyEvent;
import br.com.gamemods.minecity.forge.mc_1_10_2.event.VehicleDamageEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.MinecraftForge;

@Referenced
public class MineCityFrostHooks
{
    @Referenced(at = FrostEntityBoatTransformer.class)
    public static boolean onVehicleDamage(Entity entity, DamageSource source, float amount)
    {
        VehicleDamageEvent event = new VehicleDamageEvent(entity, source, amount);
        return MinecraftForge.EVENT_BUS.post(event);
    }

    @Referenced(at = FrostEntityPotionTransformer.class)
    public static void onPotionApplyEffect(EntityLivingBase entity, PotionEffect effect, Entity potion)
    {
        PotionApplyEvent event = new PotionApplyEvent(entity, effect, potion);
        if(!MinecraftForge.EVENT_BUS.post(event))
            entity.addPotionEffect(effect);
    }

    private MineCityFrostHooks(){}
}
