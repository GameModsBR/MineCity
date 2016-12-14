package br.com.gamemods.minecity.forge.mc_1_7_10.accessors;

import br.com.gamemods.minecity.forge.base.accessors.nbt.INBTTagCompound;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.mc_1_7_10.core.transformer.forge.SevenInterfaceTransformer;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Set;

@Referenced(at = SevenInterfaceTransformer.class)
public interface SevenNBTTagCompound extends INBTTagCompound
{
    @SuppressWarnings("unchecked")
    @Override
    default Set<String> keys()
    {
        return ((NBTTagCompound) this).func_150296_c();
    }
}
