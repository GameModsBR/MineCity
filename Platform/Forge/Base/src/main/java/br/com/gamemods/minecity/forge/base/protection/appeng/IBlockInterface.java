package br.com.gamemods.minecity.forge.base.protection.appeng;

import br.com.gamemods.minecity.api.permission.Permissible;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.reactive.reaction.MultiBlockReaction;
import br.com.gamemods.minecity.reactive.reaction.Reaction;
import br.com.gamemods.minecity.reactive.reaction.SingleBlockReaction;

import java.util.stream.Collectors;

@Referenced(at = ModInterfacesTransformer.class)
public interface IBlockInterface extends IAEBaseTileBlock
{
    @Override
    default Reaction reactPrePlace(Permissible who, IItemStack stack, BlockPos pos)
    {
        return new SingleBlockReaction(pos, PermissionFlag.MODIFY).combine(
                MultiBlockReaction.create(PermissionFlag.MODIFY,
                        Direction.block.stream()
                            .map(pos::add).collect(Collectors.toList())
                )
        );
    }
}
