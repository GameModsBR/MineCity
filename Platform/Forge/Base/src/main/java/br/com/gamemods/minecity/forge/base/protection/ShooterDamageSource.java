package br.com.gamemods.minecity.forge.base.protection;

import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.projectile.ProjectileShooter;
import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class ShooterDamageSource extends DamageSource
{
    @NotNull
    public final ProjectileShooter shooter;
    public ShooterDamageSource(String type, @NotNull ProjectileShooter shooter)
    {
        super(type);
        this.shooter = shooter;
    }

    @Nullable
    @Override
    public Entity getEntity()
    {
        IEntity entity = shooter.getEntity();
        return entity == null? (Entity) shooter.getIndirectEntity() : null;
    }

    @Nullable
    @Override
    public Entity getSourceOfDamage()
    {
        IEntity indirectEntity = shooter.getIndirectEntity();
        return indirectEntity == null? (Entity) shooter.getEntity() : (Entity) indirectEntity;
    }
}
