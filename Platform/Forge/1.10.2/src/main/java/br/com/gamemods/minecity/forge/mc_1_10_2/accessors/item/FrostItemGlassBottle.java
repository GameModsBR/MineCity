package br.com.gamemods.minecity.forge.mc_1_10_2.accessors.item;

import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemGlassBottle;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.protection.reaction.NoReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.SingleBlockReaction;
import br.com.gamemods.minecity.forge.mc_1_10_2.core.transformer.forge.FrostInterfaceTransformer;
import net.minecraft.entity.EntityAreaEffectCloud;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.List;

@Referenced(at = FrostInterfaceTransformer.class)
public interface FrostItemGlassBottle extends IItemGlassBottle
{
    @Override
    default Reaction reactRightClick(IEntityPlayerMP player, IItemStack stack, boolean offHand)
    {
        EntityPlayerMP entity = (EntityPlayerMP) player;
        List<EntityAreaEffectCloud> list = entity.worldObj.getEntitiesWithinAABB(EntityAreaEffectCloud.class, entity.getEntityBoundingBox().expandXyz(2), cloud ->
                cloud != null && cloud.isEntityAlive() && cloud.getOwner() instanceof EntityDragon
        );

        if(!list.isEmpty())
            return new SingleBlockReaction(((IEntity) list.get(0)).getBlockPos(player.getServer()), PermissionFlag.CLICK);

        return NoReaction.INSTANCE;
    }
}
