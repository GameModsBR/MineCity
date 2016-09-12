package br.com.gamemods.minecity.forge.base.protection.immersiveintegrations;

import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.block.ITileEntity;
import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.immersiveintegration.TileItemRobinTransformer;
import br.com.gamemods.minecity.structure.ClaimedChunk;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.util.Optional;

@Referenced
public class ImmersiveIntegrationHooks
{
    private static BlockPos lastFrom;
    private static ClaimedChunk lastFromClaim;

    public static Optional<Message> onBlockAccessOther(World world, int x, int y, int z, int fx, int fy, int fz)
    {
        MineCityForge mod = ModEnv.entityProtections.mod;
        WorldDim dim = mod.world(world);

        BlockPos from = lastFrom;
        if(from == null || from.x != fx || from.z != fz || from.y != fy || !from.world.equals(dim))
            lastFrom = from = new BlockPos(dim, fx, fy, fz);

        ClaimedChunk fromClaim = lastFromClaim = mod.mineCity.provideChunk(from.getChunk(), lastFromClaim);

        BlockPos to = new BlockPos(from, x, y, z);
        ClaimedChunk toClaim = mod.mineCity.provideChunk(to.getChunk(), fromClaim);

        return toClaim.getFlagHolder(to).can(fromClaim.getFlagHolder(from).owner(), PermissionFlag.OPEN);
    }

    @Referenced(at = TileItemRobinTransformer.class)
    public static TileEntity onTileOpen(TileEntity fromTile, TileEntity toTile, World world, int x, int y, int z)
    {
        if(toTile == null)
            return null;

        ITileEntity a = (ITileEntity) fromTile;
        ITileEntity b = (ITileEntity) toTile;

        if(onBlockAccessOther(world, b.getPosX(), b.getPosY(), b.getPosZ(), a.getPosX(), a.getPosY(), a.getPosZ()).isPresent())
            return null;

        return toTile;
    }
}
