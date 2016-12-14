package br.com.gamemods.minecity.forge.mc_1_7_10.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.event.entity.living.LivingEvent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Cancelable
public class EntityReceivePotionEffect extends LivingEvent
{
    public final PotionEffect effect;
    public final Object source;
    public final Class<?> sourceClass;
    public final String methodName;
    public final String methodDesc;
    public final List<?> methodParams;

    public EntityReceivePotionEffect(EntityLivingBase mcEntity, PotionEffect mcEffect, Object source, Class<?> sourceClass,
                                     String methodName, String methodDesc, Object[] methodParams)
    {
        super(mcEntity);
        this.effect = mcEffect;
        this.source = source;
        this.sourceClass = sourceClass;
        this.methodName = methodName;
        this.methodDesc = methodDesc;
        this.methodParams = Collections.unmodifiableList(Arrays.asList(methodParams));
    }
}
