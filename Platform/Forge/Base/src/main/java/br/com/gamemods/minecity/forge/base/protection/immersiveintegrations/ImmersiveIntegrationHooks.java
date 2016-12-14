package br.com.gamemods.minecity.forge.base.protection.immersiveintegrations;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.forge.base.accessors.block.ITileEntity;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.immersiveengineering.TileEntityConveyorSorterTransformer;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.immersiveintegration.TileItemRobinTransformer;
import br.com.gamemods.minecity.forge.base.protection.ModHooks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

@Referenced
public class ImmersiveIntegrationHooks
{
    @Referenced(at = TileEntityConveyorSorterTransformer.class)
    @Referenced(at = TileItemRobinTransformer.class)
    public static TileEntity onTileOpen(TileEntity fromTile, TileEntity toTile, World world, int x, int y, int z)
    {
        if(toTile == null)
            return null;

        ITileEntity a = (ITileEntity) fromTile;
        ITileEntity b = (ITileEntity) toTile;

        if(ModHooks.onBlockAccessOther(world, b.getPosX(), b.getPosY(), b.getPosZ(), a.getPosX(), a.getPosY(), a.getPosZ(), PermissionFlag.OPEN).isPresent())
            return null;

        return toTile;
    }
}
