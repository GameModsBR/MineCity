package br.com.gamemods.minecity.forge.base.protection.industrialcraft;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.SingleBlockReaction;

@Referenced(at = ModInterfacesTransformer.class)
public interface ICropWeeds extends ICropCard
{
    @Override
    default Reaction reactLeftClick(BlockPos pos, CropTile crop, IEntityPlayerMP player)
    {
        return new SingleBlockReaction(pos, PermissionFlag.MODIFY);
    }
}
