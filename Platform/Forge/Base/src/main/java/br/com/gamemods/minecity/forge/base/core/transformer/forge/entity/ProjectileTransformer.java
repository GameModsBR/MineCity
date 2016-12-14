package br.com.gamemods.minecity.forge.base.core.transformer.forge.entity;

import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.InsertSetterGetterTransformer;

import java.util.Arrays;

@Referenced
public class ProjectileTransformer extends InsertSetterGetterTransformer
{
    @Referenced("br.com.gamemods.minecity.forge.mc_1_10_2.core.MineCityFrostCoreMod")
    @Referenced("br.com.gamemods.minecity.forge.mc_1_7_10.core.MineCitySevenCoreMod")
    public ProjectileTransformer()
    {
        super(
                "br.com.gamemods.minecity.forge.base.accessors.entity.projectile.ProjectileShooter",
                "mineCityShooter",
                "br.com.gamemods.minecity.forge.base.accessors.entity.projectile.EntityProjectile",
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
                        "net.minecraft.entity.projectile.EntityFireball",
                        "net.minecraft.entity.item.EntityItem",
                        "net.minecraft.entity.projectile.EntityThrowable",
                        "ic2.core.item.tool.EntityMiningLaser"
                )
        );
    }
}
