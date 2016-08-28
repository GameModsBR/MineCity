package br.com.gamemods.minecity.forge.mc_1_7_10.protection;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.entity.SevenEntityBoatTransformer;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.entity.SevenEntityPotionTransformer;
import br.com.gamemods.minecity.forge.mc_1_7_10.event.PotionApplyEvent;
import br.com.gamemods.minecity.forge.mc_1_7_10.event.VehicleDamageEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.MinecraftForge;

@Referenced
public class MineCitySevenHooks
{
    @Referenced(at = SevenEntityBoatTransformer.class)
    public static boolean onVehicleDamage(Entity entity, DamageSource source, float amount)
    {
        return MinecraftForge.EVENT_BUS.post(new VehicleDamageEvent(entity, source, amount));
    }

    @Referenced(at = SevenEntityPotionTransformer.class)
    public static void onPotionApplyEffect(EntityLivingBase entity, PotionEffect effect, EntityPotion potion)
    {
        PotionApplyEvent event = new PotionApplyEvent(entity, effect, potion);
        if(!MinecraftForge.EVENT_BUS.post(event))
            entity.addPotionEffect(effect);
    }

    private MineCitySevenHooks(){}
}
