package br.com.gamemods.minecity.forge.mc_1_7_10.accessors.entity;

import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.accessors.entity.IEntityHorse;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.SevenInterfaceTransformer;
import net.minecraft.entity.passive.EntityHorse;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Referenced(at = SevenInterfaceTransformer.class)
public interface SevenEntityHorse extends IEntityHorse, SevenEntityLiving
{
    @Nullable
    @Override
    default UUID getEntityOwnerId()
    {
        String uuid = ((EntityHorse) this).func_152119_ch();
        //noinspection ConstantConditions
        if(uuid == null || uuid.isEmpty())
            return null;
        return UUID.fromString(uuid);
    }

    @Override
    default boolean canHaveChest()
    {
        return ((EntityHorse) this).func_110229_cs();
    }
}
