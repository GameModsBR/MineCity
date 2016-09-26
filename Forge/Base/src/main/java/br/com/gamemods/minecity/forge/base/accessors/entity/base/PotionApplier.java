package br.com.gamemods.minecity.forge.base.accessors.entity.base;

import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface PotionApplier
{
    @NotNull
    default Action getPotionAction(IEntityLivingBase mcEntity, IPotionEffect mcEffect, Class<?> sourceClass,
                                   String methodName, String methodDesc, List<?> methodParams)
    {
        return Action.SIMULATE_POTION;
    }

    @Nullable
    default IEntity getPotionSource(IEntityLivingBase entity, IPotionEffect mcEffect, Class<?> sourceClass,
                                    String methodName, String methodDesc, List<?> methodParams)
    {
        if(this instanceof IEntity)
            return (IEntity) this;
        return null;
    }

    default DamageSource getPotionDamageSource(IEntity sourceEntity, IEntityLivingBase entity, IPotionEffect mcEffect,
                                               Class<?> sourceClass, String methodName, String methodDesc,
                                               List<?> methodParams)
    {
        if(sourceEntity != null)
            return new EntityDamageSource("generic", (Entity) sourceEntity);

        return new DamageSource("generic");
    }

    enum Action
    {
        NOTHING, SIMULATE_POTION, SIMULATE_DAMAGE_VERBOSE, SIMULATE_DAMAGE_SILENT
    }
}
