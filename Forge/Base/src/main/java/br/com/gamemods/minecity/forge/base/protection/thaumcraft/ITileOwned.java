package br.com.gamemods.minecity.forge.base.protection.thaumcraft;

import br.com.gamemods.minecity.forge.base.accessors.block.ITileEntity;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

import java.util.ArrayList;
import java.util.List;

@Referenced(at = ModInterfacesTransformer.class)
public interface ITileOwned extends ITileEntity
{
    default String getOwnerName()
    {
        return toNBT().getString("owner");
    }

    default List<String> getAccessList()
    {
        NBTTagCompound nbt = toNBT();
        NBTTagList nbtList = nbt.getTagList("access", 10);
        int count = nbtList.tagCount();
        ArrayList<String> list = new ArrayList<>(count);

        for(int i = 0; i < count; i++)
            list.add(((NBTTagString)nbtList.get(i)).getString());

        return list;
    }
}
