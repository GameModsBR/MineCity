package br.com.gamemods.minecity.forge.base.protection.opencomputers;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import net.minecraft.entity.player.EntityPlayer;

import java.util.UUID;

@Referenced(at = ModInterfacesTransformer.class)
public interface IAgent extends IRotatable, IMachineHost
{
    UUID ownerUUID();
    String ownerName();
    EntityPlayer player();

    default PlayerID ownerId()
    {
        return new PlayerID(ownerUUID(), ownerName());
    }
}
