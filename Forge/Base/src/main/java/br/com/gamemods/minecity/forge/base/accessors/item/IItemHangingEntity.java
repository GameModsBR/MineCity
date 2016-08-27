package br.com.gamemods.minecity.forge.base.accessors.item;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.shape.Cuboid;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.entity.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.protection.reaction.NoReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.ShapeBlockReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.SingleBlockReaction;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.item.ItemHangingEntity;

import java.lang.reflect.Field;

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
                        return new ShapeBlockReaction(
                                pos.world,
                                new Cuboid(pos.add(face), pos.add(right.x * 3, 3, right.z * 3)),
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
