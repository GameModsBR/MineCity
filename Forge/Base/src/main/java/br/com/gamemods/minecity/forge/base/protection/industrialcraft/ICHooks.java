package br.com.gamemods.minecity.forge.base.protection.industrialcraft;

import br.com.gamemods.minecity.api.shape.Point;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.industrialcraft.EntityParticleTransformer;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

@Referenced
public class ICHooks
{
    @Referenced(at = EntityParticleTransformer.class)
    public static boolean onEntityChangeBlock(Entity entity, World world, Point point)
    {
        return true;
    }
}
