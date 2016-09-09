package br.com.gamemods.minecity.forge.base.accessors.nbt;

import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ForgeInterfaceTransformer;
import net.minecraft.nbt.NBTBase;

@Referenced(at = ForgeInterfaceTransformer.class)
public interface INBTBase
{
    default NBTBase getForgeNBT()
    {
        return (NBTBase) this;
    }
}
