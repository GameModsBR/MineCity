package br.com.gamemods.minecity.forge.base.protection.thaumcraft;

import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.accessors.block.IBlockClickExtendsOpen;
import br.com.gamemods.minecity.forge.base.accessors.block.IBlockSnapshot;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import net.minecraft.world.IBlockAccess;

@Referenced(at = ModInterfacesTransformer.class)
public interface IBlockArcaneDoor extends OwnedDevice, IBlockClickExtendsOpen
{
    int getFullMetadata(IBlockAccess world, int x, int y, int z);

    @Override
    default Reaction reactRightClick(BlockPos pos, IState state, IEntityPlayerMP player, IItemStack stack,
                                     boolean offHand, Direction face)
    {
        return OwnedDevice.super.reactRightClick(pos, state, player, stack, offHand, face);
    }

    @Override
    default Reaction reactBlockPlace(ForgePlayer<?, ?, ?> player, IBlockSnapshot snap, IItemStack hand,
                                     boolean offHand)
    {
        OwnedDevice.super.onBlockPlace(player, snap, hand, offHand);
        return IBlockClickExtendsOpen.super.reactBlockPlace(player, snap, hand, offHand);
    }
}
