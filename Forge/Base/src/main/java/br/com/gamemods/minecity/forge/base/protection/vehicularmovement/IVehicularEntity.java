package br.com.gamemods.minecity.forge.base.protection.vehicularmovement;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.permission.Permissible;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityLivingBase;
import br.com.gamemods.minecity.forge.base.accessors.entity.passive.IEntityAnimal;
import br.com.gamemods.minecity.forge.base.accessors.entity.vehicle.IVehicle;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.NoReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.SingleBlockReaction;
import net.minecraft.util.DamageSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

@Referenced(at = ModInterfacesTransformer.class)
public interface IVehicularEntity extends IEntityAnimal, IVehicle
{
    String getOwnerName();
    String getOwnerId();

    @Override
    default Reaction reactPlayerInteraction(ForgePlayer<?, ?, ?> player, IItemStack stack, boolean offHand)
    {
        return reactPlayerInteractLiving(player, stack, offHand);
    }

    @Override
    default Reaction reactPlayerAttack(MineCityForge mod, Permissible player, IItemStack stack,
                                       DamageSource source, float amount, List<Permissible> attackers)
    {
        UUID owner = getEntityOwnerId();
        if(owner != null && owner.equals(player.identity().getUniqueId()))
            return NoReaction.INSTANCE;

        return new SingleBlockReaction(getBlockPos(mod), PermissionFlag.MODIFY);
    }

    @Override
    default Reaction reactPlayerInteractLiving(ForgePlayer<?, ?, ?> player, IItemStack stack, boolean offHand)
    {
        UUID owner = getEntityOwnerId();
        if(owner != null && owner.equals(player.getUniqueId()))
            return NoReaction.INSTANCE;

        BlockPos pos = getBlockPos(player.getServer());
        SingleBlockReaction react = new SingleBlockReaction(pos, PermissionFlag.ENTER);
        react.addAllowListener((reaction, permissible, flag, pos1, message) ->
            player.cmd.sender.mount(this)
        );
        return new SingleBlockReaction(pos, PermissionFlag.RIDE).combine(react);
    }

    @Override
    default PlayerID getVehicleOwner()
    {
        PlayerID owner = IVehicle.super.getVehicleOwner();
        String name = getOwnerName();
        if(owner != null && (name == null || name.isEmpty() || name.equals(owner.getName())))
            return owner;

        UUID id = getEntityOwnerId();
        if(id == null)
            return null;

        if(name == null || name.isEmpty())
            name = "???";

        PlayerID playerID = new PlayerID(id, name);
        setVehicleOwner(playerID);
        return playerID;
    }

    @Nullable
    @Override
    default UUID getEntityOwnerId()
    {
        String ownerId = getOwnerId();
        if(ownerId == null || ownerId.isEmpty())
            return null;

        return UUID.fromString(ownerId);
    }

    @Nullable
    @Override
    default IEntityLivingBase getEntityOwner()
    {
        UUID ownerId = getEntityOwnerId();
        if(ownerId == null)
            return null;

        return getIWorld().getPlayerByUUID(ownerId);
    }

    @Nullable
    @Override
    default PermissionFlag getPlayerAttackType()
    {
        return PermissionFlag.MODIFY;
    }

    @NotNull
    @Override
    default Type getType()
    {
        return Type.VEHICLE;
    }

    @Override
    default boolean isBreedingItem(IItemStack stack)
    {
        return false;
    }
}
