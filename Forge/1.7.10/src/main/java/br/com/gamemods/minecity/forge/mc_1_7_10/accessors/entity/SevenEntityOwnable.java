package br.com.gamemods.minecity.forge.mc_1_7_10.accessors.entity;

import br.com.gamemods.minecity.forge.base.accessors.entity.base.EntityOwnable;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntity;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.SevenInterfaceTransformer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Referenced(at = SevenInterfaceTransformer.class)
public interface SevenEntityOwnable extends EntityOwnable
{
    @Override
    default void detectOwner()
    {
        getOwnerId();
    }

    @Override
    default UUID getOwnerId()
    {
        IEntity owner = getOwner();
        if(owner != null)
            return owner.getUniqueID();

        IEntityOwnable ownable = (IEntityOwnable) this;
        String name = ownable.func_152113_b();
        if(name.isEmpty())
        {
            setOwnableOwner("", "");
            return null;
        }

        if(name.matches("[a-fA-F0-9]+(-[a-fA-F0-9]+){5}"))
            try
            {
                UUID uuid = UUID.fromString(name);
                setOwnableOwner(uuid.toString(), this instanceof IEntity? ((IEntity) this).getIWorld().getServer().getUsernameCache().getOrDefault(uuid, "???") : "???");
                return uuid;
            }
            catch(Exception ignored)
            {}

        if(this instanceof IEntity)
        {
            NBTTagCompound nbt = ((Entity) this).getEntityData();
            String storedId = nbt.getString("mineCity$ownableOwner");
            if(!storedId.isEmpty())
            {
                String storedName = nbt.getString("mineCity$ownableOwnerName");
                if(storedName.equals("???"))
                    storedName = "";

                if(storedName.isEmpty())
                {
                    nbt.setString("mineCity$ownableOwnerName", name);
                    return UUID.fromString(storedId);
                }

                if(storedName.equalsIgnoreCase(name))
                    return UUID.fromString(storedId);
            }

            Optional<UUID> found = ((IEntity) this).getIWorld().getServer().getUsernameCache().entrySet()
                    .parallelStream().filter(e -> e.getValue().equals(name)).map(Map.Entry::getKey)
                    .findAny();

            if(found.isPresent())
            {
                UUID uuid = found.get();
                setOwnableOwner(uuid.toString(), name);
                return uuid;
            }

            if(storedId.isEmpty())
                return null;

            nbt.setString("mineCity$ownableOwnerName", name);
            return UUID.fromString(storedId);
        }

        return null;
    }

    default void setOwnableOwner(String ownerId, String ownerName)
    {
        if(this instanceof IEntity)
        {
            NBTTagCompound nbt = ((Entity)this).getEntityData();
            String uuid = nbt.getString("mineCity$ownableOwner");
            if(!ownerId.equals(uuid))
                nbt.setString("mineCity$ownableOwner", ownerId);

            String name = nbt.getString("mineCity$ownableOwnerName");
            if(name.isEmpty() || name.equals("???") || !name.equals(ownerName) && !ownerName.equals("???"))
                nbt.setString("mineCity$ownableOwnerName", ownerName);
        }
    }

    @Override
    default IEntity getOwner()
    {
        IEntity owner = EntityOwnable.super.getOwner();
        if(owner != null)
            setOwnableOwner(owner.getUniqueID().toString(), owner.getName());

        return owner;
    }
}
