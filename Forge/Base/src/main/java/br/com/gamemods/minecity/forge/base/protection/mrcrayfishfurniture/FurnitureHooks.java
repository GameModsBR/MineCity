package br.com.gamemods.minecity.forge.base.protection.mrcrayfishfurniture;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.block.ITileEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.mrcrayfishfurniture.MessageTVServerTransformer;

@Referenced
public class FurnitureHooks
{
    @Referenced(at = MessageTVServerTransformer.class)
    public static boolean onPlayerChangeTVChannel(IEntityPlayerMP player, ITileEntity tile)
    {
        MineCityForge mod = ModEnv.entityProtections.mod;
        BlockPos pos = tile.getBlockPos(mod);
        if(mod.mineCity.provideChunk(pos.getChunk()).getFlagHolder(pos).can(player, PermissionFlag.CLICK).isPresent())
        {
            player.sendFakeAir(pos);
            player.sendBlockAndTile(pos);
            return true;
        }

        return false;
    }
}
