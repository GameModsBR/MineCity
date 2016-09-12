package br.com.gamemods.minecity.forge.base.protection;

import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.structure.ClaimedChunk;
import net.minecraft.world.World;

import java.util.Optional;

public class ModHooks
{
    private static BlockPos lastFrom;
    private static ClaimedChunk lastFromClaim;
    private static ClaimedChunk lastToClaim;

    public static Optional<Message> onBlockAccessOther(World world, int x, int y, int z, int fx, int fy, int fz, PermissionFlag flag)
    {
        MineCityForge mod = ModEnv.entityProtections.mod;
        WorldDim dim = mod.world(world);

        BlockPos from = lastFrom;
        if(from == null || from.x != fx || from.z != fz || from.y != fy || !from.world.equals(dim))
            lastFrom = from = new BlockPos(dim, fx, fy, fz);

        ClaimedChunk fromClaim = lastFromClaim = mod.mineCity.provideChunk(from.getChunk(), lastFromClaim);

        int tx = x >> 4;
        int tz = z >> 4;
        ClaimedChunk toClaim = lastToClaim;
        if(toClaim == null || toClaim.isInvalid() || toClaim.chunk.x != tx || toClaim.chunk.z != tz || !toClaim.chunk.world.equals(dim))
        {
            if(tx == fromClaim.chunk.x && tz == fromClaim.chunk.z)
                lastToClaim = toClaim = fromClaim;
            else
                lastToClaim = toClaim = mod.mineCity.provideChunk(new ChunkPos(from.world, tx, tz));
        }

        return toClaim.getFlagHolder(x, y, z).can(fromClaim.getFlagHolder(from).owner(), flag);
    }
}
