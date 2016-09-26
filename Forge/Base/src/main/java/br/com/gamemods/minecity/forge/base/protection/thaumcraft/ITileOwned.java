package br.com.gamemods.minecity.forge.base.protection.thaumcraft;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.forge.base.accessors.block.ITileEntity;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import net.minecraft.tileentity.TileEntity;

import java.util.List;
import java.util.ListIterator;
import java.util.Optional;

@Referenced(at = ModInterfacesTransformer.class)
public interface ITileOwned extends ITileEntity
{
    default String getOwnerName()
    {
        return ThaumHooks.getOwner(this);
    }

    default List<String> getAccessList()
    {
        return ThaumHooks.getAccessList(this);
    }

    default void setOwner(PlayerID id)
    {
        ThaumHooks.setOwner(this, id.getName());
        List<String> accessList = getAccessList();
        accessList.removeIf(str-> str.charAt(0) == 'O');
        accessList.add("O:"+id.uniqueId);
        ((TileEntity) this).markDirty();
    }

    default boolean isOwner(PlayerID id)
    {
        if(id.getName().equals(getOwnerName()))
            return true;

        if(getAccessList().contains("O:"+id.uniqueId))
        {
            ThaumHooks.setOwner(this, id.getName());
            ((TileEntity) this).markDirty();
            return true;
        }

        return false;
    }

    default boolean hasAccess(PlayerID id)
    {
        String name = id.getName();
        List<String> nameList = getAccessList();
        if(nameList.contains("0"+name) || nameList.contains("1"+name))
            return true;

        Optional<String> opt = nameList.stream().filter(n -> n.startsWith("U:" + id.uniqueId + ":")).findFirst();
        if(!opt.isPresent())
            return false;

        String storedName = opt.get();
        storedName = storedName.substring(storedName.lastIndexOf(':')+1);
        ListIterator<String> iter = nameList.listIterator();
        while(iter.hasNext())
        {
            String entry = iter.next();
            if(entry.equals("1"+storedName) || entry.equals("0"+storedName))
                iter.set(entry.charAt(0)+name);
        }
        ((TileEntity) this).markDirty();
        return true;
    }

    default void registerAccess(PlayerID id)
    {
        String name = id.getName();
        if(getOwnerName().equals(name))
        {
            setOwner(id);
            return;
        }

        List<String> accessList = getAccessList();
        if(!accessList.contains("U:"+id.uniqueId) && (accessList.contains("0"+name) || accessList.contains("1"+name)))
        {
            accessList.add("U:"+id.uniqueId);
            ((TileEntity) this).markDirty();
        }
    }
}
