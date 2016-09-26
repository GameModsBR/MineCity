package br.com.gamemods.minecity.forge.base.accessors.entity.base;

import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;

import java.util.List;

public interface Incendiary
{
    default Action getIncendiaryAction(IEntity entity, int seconds, Class<?> sourceClass, String sourceMethod,
                             String sourceMethodDesc, List<?> methodParams)
    {
        return Action.SIMULATE_DAMAGE_SILENT;
    }

    default IEntity getIncendiarySource(IEntity entity, int seconds, Class<?> sourceClass, String sourceMethod,
                             String sourceMethodDesc, List<?> methodParams)
    {
        if(this instanceof IEntity)
            return (IEntity) this;

        return null;
    }

    default DamageSource getIncendiaryDamage(IEntity sourceEntity, IEntity entity, int seconds, Class<?> sourceClass,
                             String sourceMethod, String sourceMethodDesc, List<?> methodParams)
    {
        if(sourceEntity != null)
            return new EntityDamageSource("ignition", (Entity) sourceEntity).setFireDamage();

        return new DamageSource("ignition").setFireDamage();
    }

    enum Action
    {
        NOTHING, PLAYER_IGNITION, SIMULATE_DAMAGE_VERBOSE, SIMULATE_DAMAGE_SILENT
    }
}
