package br.com.gamemods.minecity.forge.base.accessors.item;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.shape.Cuboid;
import br.com.gamemods.minecity.api.shape.PreciseCuboid;
import br.com.gamemods.minecity.api.shape.PrecisePoint;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.entity.IEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.item.ItemHangingEntityTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.NoReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.ShapeBlockReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.SingleBlockReaction;
import net.minecraft.block.BlockRedstoneDiode;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.item.ItemHangingEntity;

import java.lang.reflect.Field;

@Referenced(at = ItemHangingEntityTransformer.class)
public interface IItemHangingEntity extends IItem
{
    @Override
    default Reaction reactRightClickBlock(IEntityPlayerMP player, IItemStack stack, boolean offHand,
                                          IState state, BlockPos pos, Direction face)
    {
        if(face == Direction.DOWN || face == Direction.UP)
            return NoReaction.INSTANCE;

        Field[] fields = ItemHangingEntity.class.getDeclaredFields();
        for(Field f: fields)
        {
            if(f.getType() == Class.class)
            {
                f.setAccessible(true);
                try
                {
                    if(EntityPainting.class == f.get(this))
                    {
                        Direction right = face.right();
                        Direction opposite = face.getOpposite();
                        pos = pos.add(face);

                        int maxWidth = 0;
                        int maxHeight = 0;
                        IWorldServer world = player.getIWorld();
                        PrecisePoint from = new PrecisePoint(pos);
                        art:
                        for(EntityPainting.EnumArt art: EntityPainting.EnumArt.values())
                        {
                            int width = (art.sizeX/16) -1;
                            int height = (art.sizeY/16) -1;
                            if(width <= maxWidth && height <= maxHeight)
                                continue;

                            PreciseCuboid cuboid = new PreciseCuboid(from, from.add(right.x * width, height, right.z * width));
                            if(!world.getCollisionBoxes(cuboid).isEmpty())
                                continue;

                            for(int w = 0; w <= width; w++)
                                for(int h = 0; h <= height; h++)
                                {
                                    int x = pos.x + opposite.x + right.x*w;
                                    int y = pos.y + h;
                                    int z = pos.z + opposite.z + right.z*w;
                                    if(world.isSideSolid(x, y, z, face))
                                        continue;

                                    IState wall = world.getIState(x, y, z);
                                    if(!wall.isSolid() || wall.getIBlock() instanceof BlockRedstoneDiode)
                                        continue art;
                                }

                            for(IEntity entity: world.getEntities(cuboid))
                                if(entity instanceof EntityHanging)
                                    continue art;

                            maxWidth = Math.max(maxWidth, width);
                            maxHeight = Math.max(maxHeight, height);
                        }

                        if(maxHeight == 0 && maxWidth == 0)
                            return new SingleBlockReaction(pos, PermissionFlag.MODIFY);

                        return new ShapeBlockReaction(
                                pos.world,
                                new Cuboid(pos, pos.add(right.x * maxWidth, maxHeight, right.z * maxWidth)),
                                PermissionFlag.MODIFY
                        );
                    }
                    else
                        break;
                }
                catch(ReflectiveOperationException e)
                {
                    break;
                }
            }
        }

        return new SingleBlockReaction(pos.add(face), PermissionFlag.MODIFY);
    }
}
