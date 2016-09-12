package br.com.gamemods.minecity.forge.base.protection.zettaindustries;

import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.zettaindustries.BlockSulfurTransformer;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.zettaindustries.QuarryFixerBlockTransformer;
import br.com.gamemods.minecity.structure.ClaimedChunk;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import java.util.Optional;

@Referenced
public class ZettaHooks
{
    private static BlockPos lastFrom;
    private static ClaimedChunk lastFromClaim;

    public static Optional<Message> onBlockChangeOther(World world, int x, int y, int z, int fx, int fy, int fz)
    {
        MineCityForge mod = ModEnv.entityProtections.mod;
        WorldDim dim = mod.world(world);

        BlockPos from = lastFrom;
        if(from == null || from.x != fx || from.z != fz || from.y != fy || !from.world.equals(dim))
            lastFrom = from = new BlockPos(dim, fx, fy, fz);

        ClaimedChunk fromClaim = lastFromClaim = mod.mineCity.provideChunk(from.getChunk(), lastFromClaim);

        BlockPos to = new BlockPos(from, x, y, z);
        ClaimedChunk toClaim = mod.mineCity.provideChunk(to.getChunk(), fromClaim);

        return toClaim.getFlagHolder(to).can(fromClaim.getFlagHolder(from).owner(), PermissionFlag.MODIFY);
    }

    @Referenced(at = QuarryFixerBlockTransformer.class)
    public static boolean onQuarryChange(World world, int x, int y, int z, int fx, int fy, int fz, EntityPlayer player)
    {
        if(world.isRemote)
            return true;

        if(player == null)
            return onBlockChangeOther(world, x, y, z, fx, fy, fz).isPresent();

        MineCityForge mod = ModEnv.entityProtections.mod;
        WorldDim dim = mod.world(world);
        int cx = x >> 4;
        int cz = z >> 4;
        ClaimedChunk claim = lastFromClaim;
        if(claim == null || claim.isInvalid() || claim.chunk.x != cx || claim.chunk.z != cz)
            lastFromClaim = claim = mod.mineCity.provideChunk(new ChunkPos(dim, cx, cz));

        return claim.getFlagHolder(x, y, z).can((IEntityPlayerMP) player, PermissionFlag.MODIFY).isPresent();
    }

    @Referenced(at = BlockSulfurTransformer.class)
    public static boolean onSulfurChange(World world, int x, int y, int z, int fx, int fz)
    {
        return world.isRemote || onBlockChangeOther(world, fx, y, fz, x, y, z).isPresent();
    }
}
