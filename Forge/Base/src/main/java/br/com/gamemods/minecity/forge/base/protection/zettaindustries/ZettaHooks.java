package br.com.gamemods.minecity.forge.base.protection.zettaindustries;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.zettaindustries.BlockSulfurTransformer;
import br.com.gamemods.minecity.structure.ClaimedChunk;
import net.minecraft.world.World;

@Referenced
public class ZettaHooks
{
    @Referenced(at = BlockSulfurTransformer.class)
    public static boolean onSulfurChange(World world, int x, int y, int z, int fx, int fz)
    {
        MineCityForge mod = ModEnv.entityProtections.mod;
        WorldDim dim = mod.world(world);
        BlockPos from = new BlockPos(dim, fx, y, fz);
        ClaimedChunk fromClaim = mod.mineCity.provideChunk(from.getChunk());

        BlockPos to = new BlockPos(from, x, y, z);
        ClaimedChunk toClaim = mod.mineCity.provideChunk(to.getChunk(), fromClaim);

        return toClaim.getFlagHolder(to).can(fromClaim.getFlagHolder(from).owner(), PermissionFlag.MODIFY).isPresent();
    }
}
