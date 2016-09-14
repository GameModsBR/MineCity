package br.com.gamemods.minecity.forge.base.accessors.entity.base;

import br.com.gamemods.minecity.api.MathUtil;
import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.command.CommandSender;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.EntityID;
import br.com.gamemods.minecity.api.permission.Identity;
import br.com.gamemods.minecity.api.permission.Permissible;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.shape.PrecisePoint;
import br.com.gamemods.minecity.api.world.*;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.IRayTraceResult;
import br.com.gamemods.minecity.forge.base.accessors.block.IBlockSnapshot;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ForgeInterfaceTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.WorldServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Referenced(at = ForgeInterfaceTransformer.class)
public interface IEntity extends MinecraftEntity
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

    @NotNull
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

    default boolean mount(IEntity entity)
    {
        return ((Entity) this).startRiding((Entity) entity);
    }

    default Message teleport(MineCityForge mod, EntityPos pos)
    {
        Entity sender = (Entity) this;
        WorldDim current = mod.world(sender.worldObj);
        if(current.equals(pos.world))
        {
            dismount();
            setPosAndRotation(pos);
            setPosAndUpdate(pos);
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
            setPosAndUpdate(x, y, z);
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
                MathUtil.floor_double((double)((((Entity)this).rotationYaw + 180.0F) * 8.0F / 360.0F) + 0.5D) & 7
        );
    }

    default Reaction reactPlayerInteraction(ForgePlayer<?,?,?> player, IItemStack stack, boolean offHand)
    {
        if(player.getUniqueId().equals(getEntityOwnerId()))
            return NoReaction.INSTANCE;

        switch(getType())
        {
            case STORAGE: return new SingleBlockReaction(getBlockPos(player.getServer()), PermissionFlag.OPEN);
            case VEHICLE: return new SingleBlockReaction(getBlockPos(player.getServer()), PermissionFlag.RIDE);
            default: return NoReaction.INSTANCE;
        }
    }

    default Reaction reactPlayerInteractionPrecise(ForgePlayer player, IItemStack stack, boolean offHand, PrecisePoint point)
    {
        return reactPlayerInteraction(player, stack, offHand);
    }

    default void writeNBT(NBTTagCompound nbt)
    {
        ((Entity) this).writeToNBT(nbt);
    }

    default void readNBT(NBTTagCompound nbt)
    {
        ((Entity) this).readFromNBT(nbt);
    }

    @NotNull
    @Override
    default Type getType()
    {
        if(this instanceof IMob)
            return Type.MONSTER;

        if(this instanceof IAnimals)
            return Type.ANIMAL;

        if(this instanceof IProjectile)
            return Type.PROJECTILE;

        if(this instanceof EntityItem)
            return Type.ITEM;

        return Type.UNCLASSIFIED;
    }

    default void afterPlayerAttack(MineCityForge mod, Permissible player, IItemStack stack, IEntity entity,
                                   DamageSource source, float amount, List<Permissible> attackers, Message message)
    {
        // Do nothing
    }

    default boolean isNamed()
    {
        return getForgeEntity().hasCustomName();
    }

    @Nullable
    default PermissionFlag getPlayerAttackType()
    {
        if(isNamed())
            return PermissionFlag.MODIFY;

        switch(getType())
        {
            case MONSTER: return PermissionFlag.PVM;
            case ANIMAL: return PermissionFlag.PVC;
            case PLAYER: return PermissionFlag.PVP;
            case PROJECTILE: return null;
            default: return PermissionFlag.MODIFY;
        }
    }

    default Reaction reactPlayerPull(MineCityForge mod, Permissible player, IEntity other, List<Permissible> relative)
    {
        PermissionFlag flag = getPlayerAttackType();
        if(flag == null)
            return NoReaction.INSTANCE;

        if(identity().equals(player.identity()))
            return NoReaction.INSTANCE;

        if(isNamed())
            return new SingleBlockReaction(getBlockPos(mod), PermissionFlag.MODIFY);

        BlockPos playerPos;
        if(player instanceof IEntity)
            playerPos = ((IEntity) player).getBlockPos(mod);
        else
        {
            IEntity rel = relative.stream()
                    .filter(Predicate.isEqual(other).negate())
                    .filter(IEntity.class::isInstance).map(IEntity.class::cast)
                    .findFirst().orElse(null);
            if(rel != null)
                playerPos = rel.getBlockPos(mod);
            else
                playerPos = other.getBlockPos(mod);
        }

        return new DoubleBlockReaction(flag, playerPos, getBlockPos(mod));
    }

    default Reaction reactPlayerIgnition(MineCityForge mod, Permissible player, IEntity igniter, int seconds, List<Permissible> attackers)
    {
        PermissionFlag flag = getPlayerAttackType();
        if(flag == null)
            return NoReaction.INSTANCE;

        if(identity().equals(player.identity()))
            return NoReaction.INSTANCE;

        if(isNamed())
            return new SingleBlockReaction(getBlockPos(mod), PermissionFlag.MODIFY);

        BlockPos playerPos;
        if(player instanceof IEntity)
            playerPos = ((IEntity) player).getBlockPos(mod);
        else
            playerPos = igniter.getBlockPos(mod);

        return new DoubleBlockReaction(flag, playerPos, getBlockPos(mod));
    }

    default Reaction reactPlayerAttack(MineCityForge mod, Permissible player, IItemStack stack,
                                       DamageSource source, float amount, List<Permissible> attackers)
    {
        PermissionFlag flag = getPlayerAttackType();
        if(flag == null)
            return NoReaction.INSTANCE;

        if(identity().equals(player.identity()))
            return NoReaction.INSTANCE;

        if(isNamed())
            return new SingleBlockReaction(getBlockPos(mod), PermissionFlag.MODIFY);

        if(player.identity().uniqueId.equals(getEntityOwnerId()))
            return NoReaction.INSTANCE;

        BlockPos playerPos = null;
        if(player instanceof IEntity)
            playerPos = ((IEntity) player).getBlockPos(mod);
        else
        {
            IEntity sod = (IEntity) source.getSourceOfDamage();
            if(sod != null)
                playerPos = sod.getBlockPos(mod);
            else
            {
                sod = attackers.stream().filter(IEntity.class::isInstance).map(IEntity.class::cast).findFirst().orElse(null);
                if(sod != null)
                    playerPos = sod.getBlockPos(mod);
            }
        }

        if(playerPos != null)
            return new DoubleBlockReaction(flag, playerPos, getBlockPos(mod));
        else
            return new SingleBlockReaction(getBlockPos(mod), flag);
    }

    default Reaction reactPlayerModifyWithProjectile(Permissible permissible, IEntity projectile,
                                                     IState state, IWorldServer world, BlockPos pos)
    {
        return NoReaction.INSTANCE;
    }

    @NotNull
    @Override
    default UUID getUniqueId()
    {
        return getUniqueID();
    }

    @Nullable
    @Override
    default CommandSender getCommandSender()
    {
        return null;
    }

    @NotNull
    @Override
    default Identity<UUID> getIdentity()
    {
        return new EntityID(getType(), getUniqueId(), getName());
    }

    @Override
    default boolean kick(Message message)
    {
        return false;
    }

    @NotNull
    @Override
    default Identity<?> identity()
    {
        return getIdentity();
    }

    @Nullable
    default UUID getEntityOwnerId()
    {
        if(this instanceof IEntityOwnable)
            return ((IEntityOwnable) this).getOwnerId();
        return null;
    }

    @Nullable
    default IEntityLivingBase getEntityOwner()
    {
        if(this instanceof IEntityOwnable)
            return (IEntityLivingBase) ((IEntityOwnable) this).getOwner();
        return null;
    }

    default int getFireTicks()
    {
        return ((Entity) this).fire;
    }

    default void setDead()
    {
        getForgeEntity().setDead();
    }

    default void setWhoDamaged(PlayerID identity)
    {
        Entity entity = getForgeEntity();
        NBTTagCompound nbt = entity.getEntityData();
        UUID uuid = identity.getUniqueId();
        nbt.setLong("MineCityLastDmg0", uuid.getMostSignificantBits());
        nbt.setLong("MineCityLastDmg1", uuid.getLeastSignificantBits());
        nbt.setString("MineCityLastDmg2", identity.getName());
        nbt.setInteger("MineCityLastDmg3", entity.ticksExisted);
    }

    default PlayerID getWhoDamaged()
    {
        Entity entity = getForgeEntity();
        NBTTagCompound nbt = entity.getEntityData();
        if(!nbt.hasKey("MineCityLastDmg3"))
            return null;

        int ticks = nbt.getInteger("MineCityLastDmg3");
        int existed = entity.ticksExisted;
        if(existed < ticks || existed - ticks > 20*20)
            return null;

        return new PlayerID(new UUID(nbt.getLong("MineCityLastDmg0"), nbt.getLong("MineCityLastDmg1")), nbt.getString("MineCityLastDmg2"));
    }

    default boolean isDead()
    {
        return ((Entity) this).isDead;
    }

    default Reaction reactPlayerSpawn(MineCityForge mod, Permissible player, BlockPos pos, IEntity spawner,
                                      List<Permissible> relative)
    {
        PermissionFlag playerAttackType = getPlayerAttackType();
        if(playerAttackType == null)
            return NoReaction.INSTANCE;

        return new SingleBlockReaction(pos, playerAttackType);
    }

    default void setPosAndRotation(double x, double y, double z, float yaw, float pitch)
    {
        ((Entity) this).setPositionAndRotation(x, y, z, yaw, pitch);
    }

    default void setPosAndRotation(EntityPos pos)
    {
        setPosAndRotation(pos.x, pos.y, pos.z, pos.yaw, pos.pitch);
    }

    default void setPosAndUpdate(double x, double y, double z)
    {
        ((Entity) this).setPositionAndUpdate(x, y, z);
    }

    default void setPosAndUpdate(PrecisePoint pos)
    {
        setPosAndUpdate(pos.x, pos.y, pos.z);
    }

    default Reaction reactImpactPre(MineCityForge mod, IRayTraceResult traceResult, Permissible who, List<Permissible> relative)
    {
        return NoReaction.INSTANCE;
    }

    default Reaction reactImpactPost(MineCityForge mod, IRayTraceResult traceResult, List<IBlockSnapshot> changes, Permissible who, List<Permissible> relative)
    {
        return new MultiBlockReaction(PermissionFlag.MODIFY,
                changes.stream().map(snap -> snap.getPosition(mod)).collect(Collectors.toList())
        );
    }
}
