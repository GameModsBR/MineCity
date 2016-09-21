package br.com.gamemods.minecity.forge.base.protection.industrialcraft;

import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.block.ITileEntity;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.industrialcraft.TileEntityTeleporterTransformer;
import org.jetbrains.annotations.Nullable;

@Referenced(at = ModInterfacesTransformer.class)
public interface ITileEntityTeleporter extends ITileEntity
{
    @Referenced(at = TileEntityTeleporterTransformer.class)
    boolean isTargetSet();

    @Referenced(at = TileEntityTeleporterTransformer.class)
    int getTargetX();

    @Referenced(at = TileEntityTeleporterTransformer.class)
    int getTargetY();

    @Referenced(at = TileEntityTeleporterTransformer.class)
    int getTargetZ();

    @Nullable
    default BlockPos getTarget(MineCityForge mod)
    {
        if(!isTargetSet())
            return null;

        return new BlockPos(mod.world(getIWorld()), getTargetX(), getTargetY(), getTargetZ());
    }
}
