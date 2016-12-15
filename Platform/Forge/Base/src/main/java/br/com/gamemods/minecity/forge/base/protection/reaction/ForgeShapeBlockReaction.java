package br.com.gamemods.minecity.forge.base.protection.reaction;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.shape.Shape;
import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.reactive.reaction.ShapeBlockReaction;

@Deprecated
public class ForgeShapeBlockReaction extends ShapeBlockReaction implements ForgeTriggers
{
    public ForgeShapeBlockReaction(WorldDim world, Shape shape, PermissionFlag flag)
    {
        super(world, shape, flag);
    }
}
