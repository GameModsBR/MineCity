package br.com.gamemods.minecity.forge.base.accessors.entity.base;

import br.com.gamemods.minecity.api.permission.Identity;
import br.com.gamemods.minecity.api.permission.Permissible;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.Referenced;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ForgeInterfaceTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.DoubleBlockReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.NoReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.SingleBlockReaction;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;

import java.util.List;

@Referenced(at = ForgeInterfaceTransformer.class)
public interface IEntityLivingBase extends IEntity
{
    @Override
    default EntityLivingBase getForgeEntity()
    {
        return (EntityLivingBase) this;
    }

    default boolean isElytraFlying()
    {
        return getForgeEntity().isElytraFlying();
    }

    default IItemStack getStackInHand(boolean offHand)
    {
        EntityLivingBase entity = (EntityLivingBase) this;
        if(offHand)
            return (IItemStack) (Object) entity.getHeldItemOffhand();
        else
            return (IItemStack) (Object) entity.getHeldItemMainhand();
    }

    default Reaction reactPlayerApplyPotion(MineCityForge mod, Permissible player, IPotionEffect effect, IEntity potion,
                                            List<Permissible> relative)
    {
        Identity<?> playerId = player.identity();
        if(playerId.equals(identity()))
            return NoReaction.INSTANCE;

        if(playerId.getUniqueId().equals(getEntityOwnerId()))
            return NoReaction.INSTANCE;

        if(!effect.isNegative())
        {
            if(getForgeEntity().getCreatureAttribute() != EnumCreatureAttribute.UNDEAD)
                return NoReaction.INSTANCE;
        }

        if(isNamed())
            return new SingleBlockReaction(getBlockPos(mod), PermissionFlag.MODIFY);

        BlockPos playerPos = null;
        if(player instanceof IEntity)
            playerPos = ((IEntity) player).getBlockPos(mod);

        PermissionFlag flag = getPlayerAttackType();
        if(flag == null)
            return NoReaction.INSTANCE;

        if(playerPos != null)
            return new DoubleBlockReaction(flag, playerPos, getBlockPos(mod));
        else
            return new SingleBlockReaction(getBlockPos(mod), flag);
    }
}
