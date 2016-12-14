package br.com.gamemods.minecity.forge.mc_1_7_10.accessors.world;

import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntity;
import br.com.gamemods.minecity.forge.base.accessors.world.IExplosion;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.SevenInterfaceTransformer;
import net.minecraft.world.Explosion;

@Referenced(at = SevenInterfaceTransformer.class)
public interface SevenExplosion extends IExplosion
{
    @Override
    default double getExplosionX()
    {
        return ((Explosion) this).explosionX;
    }

    @Override
    default double getExplosionY()
    {
        return ((Explosion) this).explosionY;
    }

    @Override
    default double getExplosionZ()
    {
        return ((Explosion) this).explosionZ;
    }

    @Override
    default IEntity getExploder()
    {
        return (IEntity) ((Explosion) this).exploder;
    }
}
