package br.com.gamemods.minecity.forge.base.protection.immersiveintegrations;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.accessors.block.IBlockModifyExtendsOpen;
import br.com.gamemods.minecity.forge.base.accessors.block.IBlockSnapshot;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.MultiBlockReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.SingleBlockReaction;

import java.util.ArrayList;

@Referenced(at = ModInterfacesTransformer.class)
public interface IBlockItemRobin extends IBlockModifyExtendsOpen
{
    @Override
    default Reaction reactBlockPlace(ForgePlayer<?, ?, ?> player, IBlockSnapshot snap)
    {
        ArrayList<BlockPos> pos = new ArrayList<>(7);
        BlockPos base = snap.getPosition(player.getServer());
        Direction.block.stream().map(base::add).forEachOrdered(pos::add);
        return new SingleBlockReaction(base, PermissionFlag.MODIFY).combine(new MultiBlockReaction(PermissionFlag.OPEN, pos));
    }
}
