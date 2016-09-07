package br.com.gamemods.minecity.forge.base.protection.enderstorage;

import br.com.gamemods.minecity.api.permission.Identity;
import br.com.gamemods.minecity.forge.base.accessors.block.ITileEntity;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import org.jetbrains.annotations.Nullable;

@Referenced(at = ModInterfacesTransformer.class)
public interface ITileFrequencyOwner extends ITileEntity
{
    @Nullable
    default String getOwnerName()
    {
        return EnderStorageAccessor.getTileFrequencyOwner$owner(this);
    }

    default boolean isOwner(Identity<?> identity)
    {
        if(identity.getType() != Identity.Type.PLAYER)
            return false;

        String owner = getOwnerName();
        return owner != null && identity.getName().equalsIgnoreCase(owner);
    }

    default boolean isPublic()
    {
        return "global".equals(getOwnerName());
    }
}
