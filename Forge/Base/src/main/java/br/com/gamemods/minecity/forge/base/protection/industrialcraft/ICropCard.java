package br.com.gamemods.minecity.forge.base.protection.industrialcraft;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.protection.reaction.NoReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.SingleBlockReaction;

public interface ICropCard
{
    boolean canBeHarvested(CropTile tile);

    default Reaction reactRightClick(BlockPos pos, CropTile crop, IEntityPlayerMP player)
    {
        if(!canBeHarvested(crop))
            return NoReaction.INSTANCE;

        return new SingleBlockReaction(pos, PermissionFlag.HARVEST).allowToPickupHarvest(player);
    }
}
