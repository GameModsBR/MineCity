package br.com.gamemods.minecity.forge.base.protection.appeng;

import br.com.gamemods.minecity.api.permission.Permissible;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.accessors.block.IBlockOpenReactor;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.MultiBlockReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.SingleBlockReaction;

import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Referenced(at = ModInterfacesTransformer.class)
public interface IAEBaseTileBlock extends IBlockOpenReactor
{
    @Override
    default Reaction reactPrePlace(Permissible who, IItemStack stack, BlockPos pos)
    {
        IWorldServer world = pos.world.getInstance(IWorldServer.class);
        return new SingleBlockReaction(pos, PermissionFlag.MODIFY).combine(
                MultiBlockReaction.create(PermissionFlag.MODIFY,
                    Stream.of(Direction.block.stream().map(pos::add)).flatMap(Function.identity())
                        .filter(p-> world.getIBlock(p) instanceof IAEBaseTileBlock)
                        .collect(Collectors.toList())
                )
        );
    }
}
