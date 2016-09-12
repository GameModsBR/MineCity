package br.com.gamemods.minecity.forge.base.protection.zettaindustries;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.zettaindustries.BlockSulfurTransformer;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.zettaindustries.QuarryFixerBlockTransformer;
import br.com.gamemods.minecity.forge.base.protection.ModHooks;
import br.com.gamemods.minecity.structure.ClaimedChunk;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

@Referenced
public class ZettaHooks
{
    private static ClaimedChunk lastClaim;

    @Referenced(at = QuarryFixerBlockTransformer.class)
    public static boolean onQuarryChange(World world, int x, int y, int z, int fx, int fy, int fz, EntityPlayer player)
    {
        if(world.isRemote)
            return true;

        if(player == null)
            return ModHooks.onBlockAccessOther(world, x, y, z, fx, fy, fz, PermissionFlag.MODIFY).isPresent();

        MineCityForge mod = ModEnv.entityProtections.mod;
        WorldDim dim = mod.world(world);
        int cx = x >> 4;
        int cz = z >> 4;
        ClaimedChunk claim = lastClaim;
        if(claim == null || claim.isInvalid() || claim.chunk.x != cx || claim.chunk.z != cz)
            lastClaim = claim = mod.mineCity.provideChunk(new ChunkPos(dim, cx, cz));

        return claim.getFlagHolder(x, y, z).can((IEntityPlayerMP) player, PermissionFlag.MODIFY).isPresent();
    }

    @Referenced(at = BlockSulfurTransformer.class)
    public static boolean onSulfurChange(World world, int x, int y, int z, int fx, int fz)
    {
        return world.isRemote || ModHooks.onBlockAccessOther(world, fx, y, fz, x, y, z, PermissionFlag.MODIFY).isPresent();
    }
}
