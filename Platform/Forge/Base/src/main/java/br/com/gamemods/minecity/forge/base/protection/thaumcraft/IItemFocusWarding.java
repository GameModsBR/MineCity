package br.com.gamemods.minecity.forge.base.protection.thaumcraft;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.permission.FlagHolder;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.ChunkPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.forge.base.ForgeUtil;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.IMinecraftServer;
import br.com.gamemods.minecity.forge.base.accessors.IRayTraceResult;
import br.com.gamemods.minecity.forge.base.accessors.block.ITileEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.reactive.reaction.MultiBlockReaction;
import br.com.gamemods.minecity.reactive.reaction.NoReaction;
import br.com.gamemods.minecity.reactive.reaction.ObservedReaction;
import br.com.gamemods.minecity.reactive.reaction.Reaction;
import br.com.gamemods.minecity.structure.ClaimedChunk;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Referenced(at = ModInterfacesTransformer.class)
public interface IItemFocusWarding extends IItemFocusBasic
{
    @Override
    default Reaction reactFocusRightClick(IItemStack stack, IWorldServer world, IEntityPlayerMP player, IRayTraceResult result)
    {
        if(result == null || result.getHitType() != 1)
            return NoReaction.INSTANCE;

        WorldDim dim = player.getServer().world(world);
        BlockPos hit = result.getHitBlockPos(dim);

        if(!world.isNormalCube(hit, true))
            return NoReaction.INSTANCE;

        MineCityForge mod = player.getServer();
        IMinecraftServer server = world.getServer();
        UUID id = player.getUniqueID();
        ITileEntity baseTile = world.getTileEntity(hit);
        if(baseTile != null)
        {
            if(!(baseTile instanceof ITileWarded))
                return NoReaction.INSTANCE;

            ITileWarded warded = (ITileWarded) baseTile;
            UUID placedBy = warded.getPlacedBy();
            if(id.equals(placedBy))
                return NoReaction.INSTANCE;

            PlayerID owner = warded.getOwner();
            BlockPos pos = warded.getBlockPos(mod);
            ClaimedChunk chunk = mod.mineCity.provideChunk(pos.getChunk());
            FlagHolder fh = chunk.getFlagHolder(pos);
            if(player.identity().equals(fh.owner()) || owner == null
                    || fh.can(owner, PermissionFlag.MODIFY).isPresent() && !fh.can(player, PermissionFlag.MODIFY).isPresent()
            ){
                PlayerID identity = player.identity();
                warded.setOwner(identity);
                if(!player.isSneaking())
                {
                    Set<BlockPos> scanned = new HashSet<>();
                    scanned.add(pos);

                    Queue<BlockPos> scan = new ArrayDeque<>();
                    Direction.block.forEach(dir-> scan.add(pos.add(dir)));

                    ClaimedChunk lastClaim = chunk;
                    while(!scan.isEmpty())
                    {
                        BlockPos current = scan.remove();
                        scanned.add(current);

                        ITileEntity scanTile = world.getTileEntity(current);
                        if(!(scanTile instanceof ITileWarded))
                            continue;

                        ITileWarded other = (ITileWarded) scanTile;
                        UUID otherOwner = other.getPlacedBy();
                        if(!(otherOwner == null || otherOwner.equals(placedBy)))
                            continue;

                        ClaimedChunk claim = lastClaim = mod.mineCity.provideChunk(
                                current.getChunk(),
                                lastClaim
                        );

                        if(claim.getFlagHolder(current) != fh)
                            continue;

                        other.setOwner(identity);
                        Direction.block.stream().map(current::add).filter(p-> !scanned.contains(p)).forEach(scan::add);
                    }
                }

                return NoReaction.INSTANCE;
            }
            else
            {
                return NoReaction.INSTANCE;
            }
        }

        AtomicReference<BlockPos> lastPos = new AtomicReference<>();
        List<BlockPos> blocks = ThaumHooks.getArchitectBlocks(
                this, stack,
                world, hit.x, hit.y, hit.z,
                ForgeUtil.toForge(result.getHitSide()),player)
                .stream().map(coord ->
                {
                    BlockPos pos = coord.toPos(dim, lastPos.get());
                    lastPos.set(pos);
                    return pos;
                }).collect(Collectors.toList());

        Map<UUID, PlayerID> idMap = new ConcurrentHashMap<>(2);
        AtomicReference<ClaimedChunk> lastClaim = new AtomicReference<>();
        Runnable adjustOwners = ()-> blocks.stream().map(world::getTileEntity)
                .filter(ITileWarded.class::isInstance).map(ITileWarded.class::cast)
                .filter(tile ->
                {
                    UUID placedBy = tile.getPlacedBy();
                    if(placedBy == null)
                        return true;

                    if(placedBy.equals(id))
                        return false;

                    PlayerID placer = idMap.computeIfAbsent(placedBy, server::getPlayerId);

                    int x = tile.getPosX();
                    int y = tile.getPosY();
                    int z = tile.getPosZ();
                    ClaimedChunk claim = mod.mineCity.provideChunk(new ChunkPos(dim, x >> 4, z >> 4), lastClaim.get());
                    lastClaim.set(claim);
                    if(claim.getFlagHolder(x, y, z)
                            .can(placer, PermissionFlag.MODIFY).isPresent())
                    {
                        tile.setOwner(player.identity());
                        return true;
                    }

                    return false;
                })
                .forEach(tile-> tile.setPlacedBy(id))
                ;

        return new ObservedReaction(MultiBlockReaction.create(PermissionFlag.MODIFY, blocks)).addAllowListener(r->
        {
            adjustOwners.run();
            mod.callSyncMethod(adjustOwners); // Yep, twice, before and after the focus execution
        });
    }
}
