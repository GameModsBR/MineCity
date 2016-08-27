package br.com.gamemods.minecity.forge.base.accessors.entity;

import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.api.world.EntityPos;
import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.entity.EntityTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.NoReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldServer;

import java.util.List;
import java.util.UUID;

@Referenced(at = EntityTransformer.class)
public interface IEntity
{
    default Entity getForgeEntity()
    {
        return (Entity) this;
    }

    default Entity getVehicle()
    {
        return ((Entity) this).getRidingEntity();
    }

    default List<Entity> getPassengers()
    {
        Entity entity = (Entity) this;
        return entity.getPassengers();
    }

    @SuppressWarnings("unchecked")
    default List<IEntity> getIPassengers()
    {
        return (List) getPassengers();
    }

    default String getName()
    {
        return getForgeEntity().getName();
    }

    default UUID getUniqueID()
    {
        return getForgeEntity().getUniqueID();
    }

    default WorldServer getWorld()
    {
        return (WorldServer) ((Entity) this).getEntityWorld();
    }

    default IWorldServer getIWorld()
    {
        return (IWorldServer) ((Entity) this).getEntityWorld();
    }

    default EntityPos getEntityPos(MineCityForge mod)
    {
        Entity entity = (Entity) this;
        return new EntityPos(mod.world(entity.worldObj), entity.posX, entity.posY, entity.posZ, entity.rotationPitch, entity.rotationYaw);
    }

    default void dismount()
    {
        ((Entity) this).dismountRidingEntity();
    }

    default Message teleport(MineCityForge mod, EntityPos pos)
    {
        Entity sender = (Entity) this;
        WorldDim current = mod.world(sender.worldObj);
        if(current.equals(pos.world))
        {
            dismount();
            sender.setPositionAndRotation(pos.x, pos.y, pos.z, pos.yaw, pos.pitch);
            sender.setPositionAndUpdate(pos.x, pos.y, pos.z);
            return null;
        }

        return new Message("action.teleport.unsupported-world-transfer", "The destiny is in a different world");
    }

    default Message teleport(MineCityForge mod, BlockPos pos)
    {
        Entity sender = (Entity) this;
        WorldDim current = mod.world(sender.worldObj);
        double x = pos.x + 0.5, y = pos.y + 0.5, z = pos.z + 0.5;
        if(current.equals(pos.world))
        {
            dismount();
            sender.setPositionAndUpdate(x, y, z);
            return null;
        }

        return new Message("action.teleport.unsupported-world-transfer", "The destiny is in a different world");
    }

    default BlockPos getBlockPos(MineCityForge mod)
    {
        Entity entity = (Entity) this;
        return new BlockPos(mod.world(entity.worldObj), (int) entity.posX, (int) entity.posY, (int) entity.posZ);
    }

    default Direction getCardinalDirection()
    {
        return Direction.cardinal8.get(
                MathHelper.floor_double((double)((((Entity)this).rotationYaw + 180.0F) * 8.0F / 360.0F) + 0.5D) & 7
        );
    }

    default Reaction reactPlayerInteraction(ForgePlayer<?,?,?> player, IItemStack stack, boolean offHand)
    {
        return NoReaction.INSTANCE;
    }
}
