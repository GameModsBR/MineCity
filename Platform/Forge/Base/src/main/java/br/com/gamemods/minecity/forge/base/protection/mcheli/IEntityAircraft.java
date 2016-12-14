package br.com.gamemods.minecity.forge.base.protection.mcheli;

import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import org.jetbrains.annotations.NotNull;

@Referenced(at = ModInterfacesTransformer.class)
public interface IEntityAircraft extends IWEntityContainer
{
    @NotNull
    @Override
    default Type getType()
    {
        return Type.VEHICLE;
    }
}
