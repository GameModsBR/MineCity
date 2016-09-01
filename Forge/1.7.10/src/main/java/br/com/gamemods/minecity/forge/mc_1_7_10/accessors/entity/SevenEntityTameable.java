package br.com.gamemods.minecity.forge.mc_1_7_10.accessors.entity;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.accessors.entity.passive.IEntityTameable;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.SevenInterfaceTransformer;
import net.minecraft.entity.passive.EntityTameable;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Referenced(at = SevenInterfaceTransformer.class)
public interface SevenEntityTameable extends IEntityTameable
{
    @Nullable
    @Override
    default UUID getEntityOwnerId()
    {
        String uuid = ((EntityTameable) this).func_152113_b();
        //noinspection ConstantConditions
        if(uuid == null)
            return null;
        return UUID.fromString(uuid);
    }
}
