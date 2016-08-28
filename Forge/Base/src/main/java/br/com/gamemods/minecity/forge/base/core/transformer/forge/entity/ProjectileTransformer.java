package br.com.gamemods.minecity.forge.base.core.transformer.forge.entity;

import br.com.gamemods.minecity.forge.base.accessors.entity.EntityProjectile;
import br.com.gamemods.minecity.forge.base.accessors.entity.ProjectileShooter;
import br.com.gamemods.minecity.forge.base.core.transformer.InsertSetterGetterTransformer;

import java.util.Arrays;

public class ProjectileTransformer extends InsertSetterGetterTransformer
{
    public ProjectileTransformer()
    {
        super(
                ProjectileShooter.class.getName(),
                "mineCityShooter",
                EntityProjectile.class.getName(),
                "setMineCityShooter", "getMineCityShooter",
                Arrays.asList(
                        "net.minecraft.entity.EntityAreaEffectCloud",
                        "net.minecraft.entity.effect.EntityWeatherEffect",
                        "net.minecraft.entity.projectile.EntityFishHook",
                        "net.minecraft.entity.projectile.EntityArrow",
                        "net.minecraft.entity.projectile.EntityShulkerBullet",
                        "net.minecraft.entity.item.EntityFireworkRocket",
                        "net.minecraft.entity.item.EntityFallingBlock",
                        "net.minecraft.entity.item.EntityEnderEye",
                        "net.minecraft.entity.item.EntityTNTPrimed",
                        "net.minecraft.entity.projectile.EntityFireball"
                )
        );
    }
}
