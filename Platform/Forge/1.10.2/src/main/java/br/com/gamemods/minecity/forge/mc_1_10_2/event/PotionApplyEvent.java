package br.com.gamemods.minecity.forge.mc_1_10_2.event;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class PotionApplyEvent extends LivingEvent
{
    public final PotionEffect effect;
    public final Entity potion;
    public PotionApplyEvent(EntityLivingBase entity, PotionEffect effect, Entity potion)
    {
        super(entity);
        this.effect = effect;
        this.potion = potion;
    }
}
