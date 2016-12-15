package br.com.gamemods.minecity.forge.base.protection.industrialcraft;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.block.ITileEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.ForgeSingleBlockReaction;
import br.com.gamemods.minecity.reactive.reaction.NoReaction;
import br.com.gamemods.minecity.reactive.reaction.Reaction;

@Referenced(at = ModInterfacesTransformer.class)
public interface IItemWeedingTrowel extends IItemIC2
{
    @Override
    default Reaction reactRightClickBlockFirstUse(IEntityPlayerMP player, IItemStack stack, boolean offHand,
                                                  IState state, BlockPos pos, Direction face)
    {
        IWorldServer world = pos.world.getInstance(IWorldServer.class);
        ITileEntity tile = world.getTileEntity(pos);
        if(!(tile instanceof CropTile))
            return NoReaction.INSTANCE;

        CropTile cropTile = (CropTile) tile;
        ICropCard plant = cropTile.getCropPlanted();
        if(plant instanceof ICropWeeds)
        {
            if(cropTile.getCropSize() <= 1)
                return new ForgeSingleBlockReaction(pos, PermissionFlag.MODIFY).allowToPickupHarvest(player);
        }

        if(ModEnv.seven)
            return new ForgeSingleBlockReaction(pos, PermissionFlag.HARVEST).allowToPickupHarvest(player);
        else
            return NoReaction.INSTANCE;
    }

    @Override
    default boolean isHarvest(IItemStack stack)
    {
        return false;
    }
}
