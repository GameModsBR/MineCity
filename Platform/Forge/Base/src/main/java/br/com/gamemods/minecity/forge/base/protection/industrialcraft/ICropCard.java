package br.com.gamemods.minecity.forge.base.protection.industrialcraft;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.industrialcraft.CropCardTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.ForgeSingleBlockReaction;
import br.com.gamemods.minecity.reactive.reaction.NoReaction;
import br.com.gamemods.minecity.reactive.reaction.Reaction;
import br.com.gamemods.minecity.reactive.reaction.SingleBlockReaction;

@Referenced(at = CropCardTransformer.class)
public interface ICropCard
{
    @Referenced(at = CropCardTransformer.class)
    boolean canBeHarvested(CropTile tile);

    @Referenced(at = CropCardTransformer.class)
    IItemStack getGain(CropTile tile);

    @Referenced(at = CropCardTransformer.class)
    IItemStack getSeeds(CropTile tile);

    default Reaction reactRightClick(BlockPos pos, CropTile crop, IEntityPlayerMP player)
    {
        if(!canBeHarvested(crop))
            return NoReaction.INSTANCE;

        IItemStack gain = getGain(crop);
        ForgeSingleBlockReaction reaction = new ForgeSingleBlockReaction(pos, PermissionFlag.HARVEST);
        if(gain != null)
            reaction.allowToPickup(player, item-> item.getStack().getIItem() == gain.getIItem() && item.getStack().getMeta() == gain.getMeta());
        return reaction;
    }

    default Reaction reactLeftClick(BlockPos pos, CropTile crop, IEntityPlayerMP player)
    {
        if(!canBeHarvested(crop))
            return new SingleBlockReaction(pos, PermissionFlag.MODIFY);

        return new ForgeSingleBlockReaction(pos, PermissionFlag.MODIFY).allowToPickupHarvest(player);
    }
}
