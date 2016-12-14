package br.com.gamemods.minecity.forge.base.accessors.world;

import br.com.gamemods.minecity.api.shape.Point;
import br.com.gamemods.minecity.api.shape.PrecisePoint;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.EntityPos;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityLivingBase;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ForgeInterfaceTransformer;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.world.ExplosionTransformer;
import net.minecraft.world.Explosion;

@Referenced(at = ForgeInterfaceTransformer.class)
public interface IExplosion
{
    double getExplosionX();
    double getExplosionY();
    double getExplosionZ();
    IEntity getExploder();

    @Referenced(at = ExplosionTransformer.class)
    IWorldServer getWorld();

    default IEntityLivingBase getWhoPlaced()
    {
        return (IEntityLivingBase) ((Explosion) this).getExplosivePlacedBy();
    }

    default PrecisePoint getExplosionPos()
    {
        return new PrecisePoint(getExplosionX(), getExplosionY(), getExplosionZ());
    }

    default EntityPos getExplosionPos(MineCityForge mod)
    {
        return new EntityPos(mod.world(getWorld()), getExplosionX(), getExplosionY(), getExplosionZ());
    }

    default BlockPos getExplosionBlock(MineCityForge mod)
    {
        return new BlockPos(mod.world(getWorld()), (int)getExplosionX(), (int)getExplosionY(), (int)getExplosionZ());
    }

    default Point getExplosionBlock()
    {
        return new Point((int)getExplosionX(), (int)getExplosionY(), (int)getExplosionZ());
    }
}
