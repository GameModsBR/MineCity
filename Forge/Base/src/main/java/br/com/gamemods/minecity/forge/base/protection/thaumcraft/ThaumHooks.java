package br.com.gamemods.minecity.forge.base.protection.thaumcraft;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.forge.base.accessors.block.ITileEntity;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.thaumcraft.TileNodeTransformer;
import br.com.gamemods.minecity.forge.base.protection.ModHooks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

@Referenced
public class ThaumHooks
{
    @Referenced(at = TileNodeTransformer.class)
    public static boolean onNodeBreak(TileEntity mcTile, World mcWorld, int x, int y, int z)
    {
        ITileEntity tile = (ITileEntity) mcTile;
        return ModHooks.onBlockAccessOther(
                mcWorld, x, y, z,
                tile.getPosX(), tile.getPosY(), tile.getPosZ(),
                PermissionFlag.MODIFY
        ).isPresent();
    }
}
