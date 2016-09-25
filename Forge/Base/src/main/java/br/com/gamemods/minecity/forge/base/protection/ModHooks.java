package br.com.gamemods.minecity.forge.base.protection;

import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.Identity;
import br.com.gamemods.minecity.api.permission.Permissible;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.shape.Cuboid;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.block.ITileEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntity;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.protection.vanilla.EntityProtections;
import br.com.gamemods.minecity.structure.ClaimedChunk;
import net.minecraft.world.World;

import java.util.List;
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

    public static Optional<Message> onEntityChangeBiome(IEntity entity, IWorldServer world, int blockX, int blockZ)
    {
        List<Permissible> relatives = ModEnv.entityProtections.getRelatives(entity);
        Permissible something = relatives.stream().filter(EntityProtections.FILTER_PLAYER).findFirst()
                .orElseGet(()-> relatives.stream().filter(p-> p.identity().getType() != Identity.Type.ENTITY).findFirst().orElse(entity));

        return onSomethingChangeBiome(something, world, blockX, blockZ, null);
    }

    public static Optional<Message> onTileEntityChangeBiome(ITileEntity tile, IWorldServer world, int blockX, int blockZ)
    {
        MineCityForge mod = ModEnv.blockProtections.mod;
        BlockPos tilePos = tile.getBlockPos(mod);
        ClaimedChunk tileChunk = mod.mineCity.provideChunk(tilePos.getChunk());
        Identity<?> owner = tileChunk.getFlagHolder(tilePos).owner();

        return onSomethingChangeBiome(owner, world, blockX, blockZ, tileChunk);
    }

    public static Optional<Message> onSomethingChangeBiome(Permissible something, IWorldServer world, int blockX, int blockZ, ClaimedChunk cache)
    {
        MineCityForge mod = ModEnv.entityProtections.mod;
        BlockPos from = new BlockPos(mod.world(world), blockX, 0, blockZ);
        BlockPos to = new BlockPos(from, blockX, 255, blockZ);
        Cuboid area = new Cuboid(from, to);

        ClaimedChunk claim = mod.mineCity.provideChunk(from.getChunk(), cache);

        Optional<Message> denial = claim.getFlagHolder().can(something, PermissionFlag.MODIFY);
        if(denial.isPresent())
            return denial;

        return claim.getPlots().stream().filter(plot-> plot.getShape().overlaps(area))
                .map(plot-> plot.can(something, PermissionFlag.MODIFY))
                .filter(Optional::isPresent).map(Optional::get)
                .findFirst()
                ;
    }
}
