package br.com.gamemods.minecity.forge.base.protection.thaumcraft;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.permission.FlagHolder;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityCreature;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityLivingBase;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.ForgeReaction;
import br.com.gamemods.minecity.reactive.reaction.*;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Referenced(at = ModInterfacesTransformer.class)
public interface IEntityGolemBase extends IEntityCreature
{
    void setOwner(String name);
    String getOwnerName();
    byte getCore();

    @Nullable
    @Override
    default PermissionFlag getPlayerAttackType()
    {
        return PermissionFlag.MODIFY;
    }

    @Nullable
    @Override
    default IEntityLivingBase getEntityOwner()
    {
        return getIWorld().getPlayerByName(getOwnerName());
    }

    @Nullable
    @Override
    default UUID getEntityOwnerId()
    {
        IEntityLivingBase owner = getEntityOwner();
        if(owner != null)
            return owner.getUniqueID();

        NBTTagCompound nbt = ((Entity) this).getEntityData();
        if(!nbt.hasKey("mineCity$gOwner"))
            return null;

        String uuid = nbt.getString("mineCity$gOwner");
        return UUID.fromString(uuid);
    }

    default void setOwner(PlayerID player)
    {
        setOwner(player.getName());
        ((Entity) this).getEntityData().setString("mineCity$gOwner", player.uniqueId.toString());
        if(getCore() > -1)
            getObservers().forEach(this::sendAllWatchableData);
        else
        {
            getObservers().forEach(this::sendDestroyPacket);
            ModEnv.entityProtections.mod.callSyncMethod(()-> getObservers().forEach(this::sendSpawnPackets));
        }
    }

    @Override
    default void onEnterWorld(BlockPos pos, IEntity spawner)
    {
        IEntityCreature.super.onEnterWorld(pos, spawner);

        UUID entityOwnerId = getEntityOwnerId();
        if(entityOwnerId != null)
            return;

        ModEnv.entityProtections.mod.callSyncMethod(()-> {
            IEntityLivingBase owner = getEntityOwner();
            UUID uuid;
            if(owner != null)
                uuid = owner.getEntityUUID();
            else
            {
                PlayerID id = getIWorld().getServer().getPlayerId(getOwnerName());
                if(id != null)
                    uuid = id.uniqueId;
                else
                    return;
            }

            ((Entity) this).getEntityData().setString("mineCity$gOwner", uuid.toString());
        });
    }

    @Override
    default Reaction reactPlayerAttackDirect(IEntityPlayerMP player, IItemStack stack, boolean offHand)
    {
        if(stack != null && stack.getIItem().getUnlocalizedName().equals("item.GolemBell"))
            return new SingleBlockReaction(getBlockPos(player.getServer()), PermissionFlag.MODIFY);

        return new DoubleBlockReaction(PermissionFlag.MODIFY, getBlockPos(player.getServer()), player.getBlockPos());
    }

    @Override
    default Reaction reactPlayerInteractLiving(ForgePlayer<?, ?, ?> fp, IItemStack stack, boolean offHand)
    {
        IEntityPlayerMP player = fp.cmd.sender;
        if(player.isSneaking())
            return NoReaction.INSTANCE;

        if(stack != null)
        {
            String itemName = stack.getIItem().getUnlocalizedName();
            if(itemName.equals("item.GolemBell"))
                return NoReaction.INSTANCE;

            byte core = getCore();

            if(core == -1 && itemName.equals("item.ItemGolemCore"))
                return new ForgeReaction(transfer(new SingleBlockReaction(getBlockPos(fp.getServer()), PermissionFlag.MODIFY), fp))
                        .onDenyUpdateInventory().addDenialListener((reaction, permissible, flag, pos, message) -> {
                            sendAllWatchableData(player);
                        });

            if(itemName.equals("item.ItemGolemUpgrade") || itemName.equals("item.ItemGolemDecoration"))
                return transfer(new SingleBlockReaction(getBlockPos(fp.getServer()), PermissionFlag.MODIFY), fp);

            if(itemName.equals("item.cookie"))
                return NoReaction.INSTANCE;

        }

        if(getCore() > 1 && (stack == null || !(stack.getIItem() instanceof IItemWandCasting)))
            return transfer(new SingleBlockReaction(getBlockPos(fp.getServer()), PermissionFlag.MODIFY), fp);

        return NoReaction.INSTANCE;
    }

    default TriggeredReaction transfer(TriggeredReaction reaction, ForgePlayer<?,?,?> player)
    {
        String ownerName = getOwnerName();
        UUID id = getEntityOwnerId();
        PlayerID identity = player.identity();
        if(id == null)
        {
            if(ownerName.equalsIgnoreCase(identity.getName()))
            {
                setOwner(identity);
                return reaction;
            }

            PlayerID playerId = getIWorld().getServer().getPlayerId(ownerName);
            if(playerId != null)
            {
                if(playerId.equals(identity))
                {
                    setOwner(identity);
                    return reaction;
                }
                else
                {
                    setOwner(playerId);
                }
            }
        }
        else if(id.equals(identity.getUniqueId()))
        {
            if(!ownerName.equals(identity.getName()))
                setOwner(identity.getName());

            return reaction;
        }

        return reaction.addAllowListener((reaction1, permissible, flag, pos, message) -> {
            FlagHolder fh;
            if(id == null || (fh = player.getServer().mineCity.provideChunk(pos.getChunk()).getFlagHolder(pos))
                    .owner().equals(identity) || fh.can(new PlayerID(id, ownerName), PermissionFlag.MODIFY).isPresent())
            {
                setOwner(identity);
            }
        });
    }
}
