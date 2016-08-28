package br.com.gamemods.minecity.forge.mc_1_7_10.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.event.entity.living.LivingEvent;

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
