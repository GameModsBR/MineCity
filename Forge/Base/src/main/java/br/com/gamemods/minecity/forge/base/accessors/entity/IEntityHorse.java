package br.com.gamemods.minecity.forge.base.accessors.entity;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ForgeInterfaceTransformer;
import net.minecraft.entity.passive.EntityHorse;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Referenced(at = ForgeInterfaceTransformer.class)
public interface IEntityHorse extends IEntityAgeable
{
    @Override
    default EntityHorse getForgeEntity()
    {
        return (EntityHorse) this;
    }

    @Nullable
    @Override
    default UUID getEntityOwnerId()
    {
        return ((EntityHorse) this).getOwnerUniqueId();
    }

    @Nullable
    @Override
    default IEntityLivingBase getEntityOwner()
    {
        EntityHorse horse = (EntityHorse) this;
        UUID uuid = horse.getOwnerUniqueId();
        if(uuid == null)
            return null;

        return (IEntityLivingBase) horse.worldObj.getPlayerEntityByUUID(uuid);
    }
}
