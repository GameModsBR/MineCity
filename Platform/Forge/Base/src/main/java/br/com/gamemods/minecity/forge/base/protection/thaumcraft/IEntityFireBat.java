package br.com.gamemods.minecity.forge.base.protection.thaumcraft;

import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.Permissible;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityLivingBase;
import br.com.gamemods.minecity.forge.base.accessors.entity.mob.IEntityMob;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

@Referenced(at = ModInterfacesTransformer.class)
public interface IEntityFireBat extends IEntityMob
{
    boolean getIsSummoned();

    @Nullable
    @Override
    default PermissionFlag getPlayerAttackType()
    {
        return getIsSummoned()? PermissionFlag.PVP : PermissionFlag.PVM;
    }

    @Nullable
    @Override
    default IEntityLivingBase getEntityOwner()
    {
        return ThaumHooks.getOwner(this);
    }

    @Nullable
    @Override
    default UUID getEntityOwnerId()
    {
        IEntityLivingBase owner = getEntityOwner();
        if(owner != null)
            return owner.getUniqueID();

        NBTTagCompound nbt = ((Entity) this).getEntityData();
        if(!nbt.hasKey("mineCity$fbSpawner"))
            return null;

        String uuid = nbt.getString("mineCity$fbSpawner");
        return UUID.fromString(uuid);
    }

    @Override
    default void onEnterWorld(BlockPos pos, IEntity spawner)
    {
        IEntityMob.super.onEnterWorld(pos, spawner);

        IEntityLivingBase owner = getEntityOwner();
        UUID uuid = owner != null? owner.getEntityUUID() : ThaumHooks.fireBatSpawner;
        ThaumHooks.fireBatSpawner = null;
        if(uuid != null)
            ((Entity) this).getEntityData().setString("mineCity$fbSpawner", uuid.toString());
    }

    @NotNull
    @Override
    default Type getType()
    {
        return !getIsSummoned()? Type.MONSTER : Type.PROJECTILE;
    }

    @Override
    default void afterPlayerAttack(MineCityForge mod, Permissible player, IItemStack stack, IEntity entity,
                                   DamageSource source, float amount, List<Permissible> attackers,
                                   Message message)
    {
        if(attackers.contains(this) && getIsSummoned())
            setDead();
    }
}
