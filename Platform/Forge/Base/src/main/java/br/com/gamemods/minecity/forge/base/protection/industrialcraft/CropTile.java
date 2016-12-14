package br.com.gamemods.minecity.forge.base.protection.industrialcraft;

import br.com.gamemods.minecity.forge.base.accessors.block.ITileEntity;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.industrialcraft.ICropTileTransformer;

@Referenced(at = ICropTileTransformer.class)
public interface CropTile extends ITileEntity
{
    @Referenced(at = ICropTileTransformer.class)
    boolean isCrossingBase();

    @Referenced(at = ICropTileTransformer.class)
    ICropCard getCropPlanted();

    @Referenced(at = ICropTileTransformer.class)
    int getCropSize();

    @Referenced(at = ICropTileTransformer.class)
    void setCropPlanted(ICropCard crop);
}
