package br.com.gamemods.minecity.forge.base.protection.industrialcraft;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.accessors.block.IBlock;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.block.ITileEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.NoReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.SingleBlockReaction;

@Referenced(at = ModInterfacesTransformer.class)
public interface IBlockCropIC2 extends IBlock
{
    @Override
    default Reaction reactRightClick(BlockPos pos, IState state, IEntityPlayerMP player, IItemStack stack,
                                     boolean offHand, Direction face)
    {
        IWorldServer world = pos.world.getInstance(IWorldServer.class);
        ITileEntity tile = world.getTileEntity(pos);
        if(!(tile instanceof CropTile))
            return new SingleBlockReaction(pos, PermissionFlag.MODIFY);

        CropTile crop = (CropTile) tile;
        ICropCard planted = crop.getCropPlanted();
        if(stack != null)
        {
            if(planted == null)
            {
                if(!crop.isCrossingBase())
                    switch(stack.getIItem().getUnlocalizedName())
                    {
                        case "ic2.blockCrop":
                            return new SingleBlockReaction(pos, PermissionFlag.MODIFY);
                    }

                Object seed = ICHooks.getBaseSeed(stack);
                if(seed != null)
                    return new SingleBlockReaction(pos, PermissionFlag.MODIFY);
            }

            switch(stack.getIItem().getUnlocalizedName())
            {
                case "item.dyePowder":
                    if(stack.getMeta() != 15)
                        break;
                case "ic2.itemFertilizer":
                case "item.bucketWater":
                case "ic2.itemCellWater":
                case "item.seeds":
                case "ic2.itemCellHydrant":
                    return new SingleBlockReaction(pos, PermissionFlag.HARVEST);

                case "ic2.itemWeedEx":
                    return new SingleBlockReaction(pos, PermissionFlag.MODIFY);

                default:
                    switch(stack.getUnlocalizedName())
                    {
                        case "ic2.itemCellWater":
                            return new SingleBlockReaction(pos, PermissionFlag.HARVEST);
                    }
            }
        }

        if(planted == null)
            return NoReaction.INSTANCE;

        return planted.reactRightClick(pos, crop, player);
    }

    @Override
    default Reaction reactLeftClick(IEntityPlayerMP player, IState state, BlockPos pos, Direction face,
                                    IItemStack stack, boolean offHand)
    {
        IWorldServer world = pos.world.getInstance(IWorldServer.class);
        ITileEntity tile = world.getTileEntity(pos);
        if(!(tile instanceof CropTile))
            return new SingleBlockReaction(pos, PermissionFlag.MODIFY);

        CropTile crop = (CropTile) tile;
        ICropCard plant = crop.getCropPlanted();
        if(plant == null)
        {
            if(crop.isCrossingBase())
                return new SingleBlockReaction(pos, PermissionFlag.MODIFY)
                        .allowToPickup(player,
                                item -> item.getStack().getIItem().getUnlocalizedName().equals("item.stick")
                        );

            return NoReaction.INSTANCE;
        }

        return plant.reactLeftClick(pos, crop, player);
    }

    @Override
    default Reaction reactBlockBreak(ForgePlayer<?, ?, ?> player, IState state, BlockPos pos)
    {
        return new SingleBlockReaction(pos, PermissionFlag.MODIFY).onDenyUpdateBlockAndTileForced(player.cmd.sender);
    }
}
