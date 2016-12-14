package br.com.gamemods.minecity.forge.base.accessors.nbt;

import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ForgeInterfaceTransformer;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Set;

@Referenced(at = ForgeInterfaceTransformer.class)
public interface INBTTagCompound extends INBTBase
{
    @Override
    default NBTTagCompound getForgeNBT()
    {
        return (NBTTagCompound) this;
    }

    default Set<String> keys()
    {
        return ((NBTTagCompound) this).getKeySet();
    }
}
