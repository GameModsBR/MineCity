package br.com.gamemods.minecity.forge.base.protection.thaumcraft;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.accessors.block.IBlockOpenReactor;
import br.com.gamemods.minecity.forge.base.accessors.block.IBlockSnapshot;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.BlockAndSidesReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.DoubleBlockReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import org.jetbrains.annotations.Nullable;

@Referenced(at = ModInterfacesTransformer.class)
public interface IBlockWoodenDevice extends IBlockOpenReactor, IBlockPlaceMetaReaction, OwnedDevice
{
    @Nullable
    @Override
    default Reaction reactPlace(BlockPos pos, int meta)
    {
        switch(meta)
        {
            case 4:
                return new BlockAndSidesReaction(PermissionFlag.MODIFY, pos);
            case 5:
                return new DoubleBlockReaction(PermissionFlag.MODIFY, pos, pos.add(Direction.DOWN));
        }

        return null;
    }

    @Override
    default Reaction reactBlockPlace(ForgePlayer<?, ?, ?> player, IBlockSnapshot snap, IItemStack hand,
                                     boolean offHand)
    {
        OwnedDevice.super.onBlockPlace(player, snap, hand, offHand);
        return IBlockPlaceMetaReaction.super.reactBlockPlace(player, snap, hand, offHand);
    }

    @Override
    default Reaction reactRightClick(BlockPos pos, IState state, IEntityPlayerMP player, IItemStack stack,
                                     boolean offHand, Direction face)
    {
        if(pos.world.getInstance(IWorldServer.class).getTileEntity(pos) instanceof ITileOwned)
            return OwnedDevice.super.reactRightClick(pos, state, player, stack, offHand, face);

        return IBlockOpenReactor.super.reactRightClick(pos, state, player, stack, offHand, face);
    }
}
