package br.com.gamemods.minecity.forge.base.protection.immersiveengineering;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.block.IBlockSnapshot;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.block.SimpleCrop;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItem;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.reactive.reaction.DoubleBlockReaction;
import br.com.gamemods.minecity.reactive.reaction.MultiBlockReaction;
import br.com.gamemods.minecity.reactive.reaction.Reaction;

import java.util.List;
import java.util.stream.Collectors;

@Referenced(at = ModInterfacesTransformer.class)
public interface IBlockIECrop extends SimpleCrop
{
    @Override
    default boolean shouldReplant(int age)
    {
        return age == 4;
    }

    @Override
    default boolean isHarvestAge(int age)
    {
        return age >= 4;
    }

    @Override
    default IItem getISeed(IWorldServer world)
    {
        return ImmersiveHooks.getItemSeeds();
    }

    @Override
    default Reaction reactBlockGrow(MineCityForge mod, IState state, BlockPos block,
                                    List<IBlockSnapshot> changes, IEntityPlayerMP boneMealPlayer)
    {
        if(boneMealPlayer == null)
            return SimpleCrop.super.reactBlockGrow(mod, state, block, changes, null);

        return new MultiBlockReaction(PermissionFlag.HARVEST, changes.stream().map(snap-> snap.getPosition(mod)).collect(Collectors.toList()));
    }

    @Override
    default Reaction reactBlockPlace(ForgePlayer<?, ?, ?> player, IBlockSnapshot snap, IItemStack hand, boolean offHand)
    {
        BlockPos snapPos = snap.getPosition(player.getServer());
        IState under = snap.getIWorld().getIState(snapPos.x, snapPos.y -1, snapPos.z);
        if(under.getIBlock() == this && under.getIntValueOrMeta("age") == 4)
            return new DoubleBlockReaction(PermissionFlag.HARVEST, snapPos, snapPos.add(Direction.DOWN));

        return SimpleCrop.super.reactBlockPlace(player, snap, hand, offHand);
    }
}
