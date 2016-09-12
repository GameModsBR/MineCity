package br.com.gamemods.minecity.forge.base.protection.zettaindustries;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.accessors.block.IBlockOpenReactor;
import br.com.gamemods.minecity.forge.base.accessors.block.IBlockSnapshot;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.MultiBlockReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.NoReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;

import java.util.ArrayList;
import java.util.List;

@Referenced(at = ModInterfacesTransformer.class)
public interface IBigBattery extends IBlockOpenReactor
{
    @Override
    default Reaction reactBlockPlace(ForgePlayer<?, ?, ?> player, IBlockSnapshot snap)
    {
        List<BlockPos> pos = new ArrayList<>(7);
        BlockPos base = snap.getPosition(player.getServer());
        pos.add(base);
        Direction.block.stream().map(base::add).forEachOrdered(pos::add);
        return new MultiBlockReaction(PermissionFlag.MODIFY, pos);
    }

    @Override
    default Reaction reactRightClick(BlockPos pos, IState state, IEntityPlayerMP player, IItemStack stack,
                                     boolean offHand, Direction face)
    {
        return NoReaction.INSTANCE;
    }
}
