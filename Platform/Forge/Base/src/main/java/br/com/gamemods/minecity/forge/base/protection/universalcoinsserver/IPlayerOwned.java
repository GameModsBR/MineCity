package br.com.gamemods.minecity.forge.base.protection.universalcoinsserver;

import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;

import java.util.UUID;

@Referenced(at = ModInterfacesTransformer.class)
public interface IPlayerOwned
{
    UUID getOwnerId();
}
