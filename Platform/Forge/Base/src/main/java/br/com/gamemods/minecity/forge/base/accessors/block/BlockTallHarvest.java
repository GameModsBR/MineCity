package br.com.gamemods.minecity.forge.base.accessors.block;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ForgeInterfaceTransformer;
import br.com.gamemods.minecity.reactive.reaction.Reaction;
import br.com.gamemods.minecity.reactive.reaction.SingleBlockReaction;

@Referenced(at = ForgeInterfaceTransformer.class)
public interface BlockTallHarvest extends IBlock
{
    @Override
    default Reaction reactBlockBreak(ForgePlayer<?, ?, ?> player, IState state, BlockPos pos)
    {
        if(pos.world.getInstance(IWorldServer.class).getIBlock(pos.x,pos.y-1,pos.z) != state.getIBlock())
            return new SingleBlockReaction(pos, PermissionFlag.MODIFY);

        SingleBlockReaction react = new SingleBlockReaction(pos, PermissionFlag.HARVEST);
        react.addAllowListener((reaction, permissible, flag, pos1, message) ->
            player.getServer().addItemConsumer(pos1.toEntity(), 2, Integer.MAX_VALUE, 2, (item, remaining) -> {
                IItemStack stack = item.getStack();
                if(stack.getIItem().isHarvest(stack))
                    item.allowToPickup(player.identity());
                return 0;
            })
        );
        return react;
    }

    @Override
    default boolean isHarvest()
    {
        return true;
    }
}
