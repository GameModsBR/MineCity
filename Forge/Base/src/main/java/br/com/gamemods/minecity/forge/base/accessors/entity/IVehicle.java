package br.com.gamemods.minecity.forge.base.accessors.entity;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.permission.Permissible;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.forge.base.protection.reaction.NoReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.SingleBlockReaction;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public interface IVehicle extends IEntity
{
    @NotNull
    @Override
    default Type getType()
    {
        return Type.VEHICLE;
    }

    default void setVehicleOwner(PlayerID id)
    {
        NBTTagCompound nbt = getForgeEntity().getEntityData();
        UUID uniqueID = id.getUniqueId();
        nbt.setLong("MineCityOwnerUUIDMost", uniqueID.getMostSignificantBits());
        nbt.setLong("MineCityOwnerUUIDLeast", uniqueID.getLeastSignificantBits());
        nbt.setString("MineCityOwner", id.getName());
    }

    default boolean setVehicleOwnerIfAbsent(PlayerID id)
    {
        NBTTagCompound nbt = getForgeEntity().getEntityData();
        if(nbt.getLong("MineCityOwnerUUIDMost") == 0)
        {
            setVehicleOwner(id);
            return true;
        }

        return false;
    }

    default PlayerID getVehicleOwner()
    {
        Entity entity = getForgeEntity();
        NBTTagCompound nbt = entity.getEntityData();
        if(!nbt.hasKey("MineCityOwnerUUIDMost"))
            return null;

        return new PlayerID(
                new UUID(nbt.getLong("MineCityOwnerUUIDMost"), nbt.getLong("MineCityOwnerUUIDLeast")),
                nbt.getString("MineCityOwner")
        );
    }

    @Override
    default Reaction reactPlayerAttack(MineCityForge mod, Permissible player, IItemStack stack,
                                       DamageSource source, float amount, List<Permissible> attackers)
    {
        if(player.identity().equals(getVehicleOwner()))
            return NoReaction.INSTANCE;

        return new SingleBlockReaction(getBlockPos(mod), PermissionFlag.MODIFY);
    }

    @Override
    default Reaction reactPlayerInteraction(ForgePlayer<?, ?, ?> player, IItemStack stack, boolean offHand)
    {
        if(player.identity().equals(getVehicleOwner()))
            return NoReaction.INSTANCE;

        return new SingleBlockReaction(getBlockPos(player.getServer()), PermissionFlag.MODIFY);
    }

    @Override
    default Reaction reactPlayerPull(MineCityForge mod, Permissible player, IEntity other,
                                     List<Permissible> relative)
    {
        if(player.identity().equals(getVehicleOwner()))
            return NoReaction.INSTANCE;

        return new SingleBlockReaction(getBlockPos(mod), PermissionFlag.MODIFY);
    }
}
