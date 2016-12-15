package br.com.gamemods.minecity.forge.base.protection.appeng;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.reactive.reaction.MultiBlockReaction;
import br.com.gamemods.minecity.reactive.reaction.Reaction;
import br.com.gamemods.minecity.reactive.reaction.SingleBlockReaction;

import java.util.stream.Collectors;

@Referenced(at = ModInterfacesTransformer.class)
public interface IItemMultiPart extends IAEBaseItem
{
    @Override
    default Reaction reactRightClickBlock(IEntityPlayerMP player, IItemStack stack, boolean offHand,
                                          IState state, BlockPos pos, Direction face)
    {
        IWorldServer world = pos.world.getInstance(IWorldServer.class);
        BlockPos posFace = pos.add(face);
        return new SingleBlockReaction(pos, PermissionFlag.MODIFY).combine(
                MultiBlockReaction.create(PermissionFlag.MODIFY,
                        Direction.block.stream().map(pos::add)
                                .filter(p-> AppengHooks.containsNetworkBlock(world, p))
                                .collect(Collectors.toList())
                ).combine(
                    MultiBlockReaction.create(PermissionFlag.MODIFY,
                        Direction.block.stream().map(posFace::add)
                                .filter(p-> AppengHooks.containsNetworkBlock(world, p))
                                .collect(Collectors.toList())
                ))
        );
    }
}
