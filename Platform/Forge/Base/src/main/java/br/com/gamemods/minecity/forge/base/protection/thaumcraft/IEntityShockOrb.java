package br.com.gamemods.minecity.forge.base.protection.thaumcraft;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.permission.Identity;
import br.com.gamemods.minecity.api.permission.Permissible;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.IRayTraceResult;
import br.com.gamemods.minecity.forge.base.accessors.block.IBlockSnapshot;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.entity.projectile.IEntityThrowable;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.RevertDeniedReaction;
import br.com.gamemods.minecity.forge.base.protection.vanilla.EntityProtections;
import br.com.gamemods.minecity.forge.base.tile.ITileEntityData;
import br.com.gamemods.minecity.reactive.reaction.ApproveReaction;
import br.com.gamemods.minecity.reactive.reaction.NoReaction;
import br.com.gamemods.minecity.reactive.reaction.Reaction;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Referenced(at = ModInterfacesTransformer.class)
public interface IEntityShockOrb extends IEntityThrowable
{
    Predicate<BlockPos> NOT_AIRY = pos-> {
        IState state = pos.world.getInstance(IWorldServer.class).getIState(pos);
        return !(state.getIBlock() instanceof IBlockAiry) || state.getIntValueOrMeta("metadata") != 10;
    };

    @Override
    default Reaction reactImpactPost(MineCityForge mod, IRayTraceResult traceResult, List<IBlockSnapshot> changes, Permissible who, List<Permissible> relative)
    {
        if(changes.isEmpty())
            return NoReaction.INSTANCE;

        @SuppressWarnings("ConstantConditions")
        PlayerID player = relative.stream().filter(EntityProtections.FILTER_PLAYER).findFirst()
                .map(p-> (PlayerID) p.identity())
                .orElseGet(()-> who != null && who.identity().getType() == Identity.Type.PLAYER? (PlayerID) who.identity() : null)
                ;

        if(player == null)
            return IEntityThrowable.super.reactImpactPost(mod, traceResult, changes, who, relative);

        Reaction reaction = NoReaction.INSTANCE;
        Set<BlockPos> positions = changes.stream().map(snap-> snap.getPosition(mod)).collect(Collectors.toSet());
        if(positions.stream().anyMatch(NOT_AIRY))
        {
            reaction = new RevertDeniedReaction(mod,
                    changes.stream()
                        .filter(snap-> NOT_AIRY.test(snap.getPosition(mod)))
                        .collect(Collectors.toList()),
                    PermissionFlag.MODIFY
            );
        }

        return new ApproveReaction(changes.get(0).getPosition(mod), PermissionFlag.MODIFY).addAllowListener((r, permissible, flag, first, message) ->
                changes.stream().filter(snap-> !NOT_AIRY.test(snap.getPosition(mod))).forEach(snap->{
                    ITileEntityData tile = ModEnv.dataSupplier.get();
                    tile.setOwner(player);
                    snap.getIWorld().setTile(snap.getX(), snap.getY(), snap.getZ(), tile);
                })
        ).combine(reaction);
    }
}
