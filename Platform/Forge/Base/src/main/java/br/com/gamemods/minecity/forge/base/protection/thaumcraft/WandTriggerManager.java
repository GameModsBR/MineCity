package br.com.gamemods.minecity.forge.base.protection.thaumcraft;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.reactive.reaction.Reaction;
import br.com.gamemods.minecity.reactive.reaction.SingleBlockReaction;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Referenced(at = ModInterfacesTransformer.class)
public interface WandTriggerManager
{
    default Reaction reactPerformTrigger(IEntityPlayerMP player, IItemStack wand, BlockPos pos, Direction side, IState state, int event)
    {
        return new SingleBlockReaction(pos, PermissionFlag.MODIFY);
    }

    static Stream<Reaction> getPerformTriggerReactions(IEntityPlayerMP player, IItemStack wand, BlockPos pos, Direction side, IState state)
    {
        List metaKey = Arrays.asList(state.getIBlock(), state.getIntValueOrMeta("metadata"));
        List blockKey = Arrays.asList(state.getIBlock(), -1);
        return ThaumHooks.getTriggers().values().stream()
                .map(m-> Optional.ofNullable(m.get(metaKey)).orElseGet(()-> m.get(blockKey)))
                .filter(l-> l != null)
                .map(l-> ((WandTriggerManager) l.get(0)).reactPerformTrigger(player, wand, pos, side, state, (int) l.get(1)))
                ;
    }

    static boolean hasTriggers(IState state)
    {
        List metaKey = Arrays.asList(state.getIBlock(), state.getIntValueOrMeta("metadata"));
        List blockKey = Arrays.asList(state.getIBlock(), -1);
        return ThaumHooks.getTriggers().values().stream()
                .map(m-> Optional.ofNullable(m.get(metaKey)).orElseGet(()-> m.get(blockKey)))
                .anyMatch(l-> l != null);
    }
}
