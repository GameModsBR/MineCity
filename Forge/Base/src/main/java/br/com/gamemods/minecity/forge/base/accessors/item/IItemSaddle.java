package br.com.gamemods.minecity.forge.base.accessors.item;

import br.com.gamemods.minecity.api.permission.Permissible;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.entity.passive.IEntityPig;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ForgeInterfaceTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.NoReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.SingleBlockReaction;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.util.DamageSource;

import java.util.List;

@Referenced(at = ForgeInterfaceTransformer.class)
public interface IItemSaddle extends IItem
{
    @Override
    default Reaction reactInteractEntity(IEntityPlayerMP player, IEntity target, IItemStack stack,
                                         boolean offHand)
    {
        if(!(target instanceof EntityPig))
            return NoReaction.INSTANCE;

        IEntityPig pig = (IEntityPig) target;
        if(pig.isSaddled() || pig.isChild())
            return NoReaction.INSTANCE;

        SingleBlockReaction react = new SingleBlockReaction(pig.getBlockPos(player.getServer()), PermissionFlag.MODIFY);
        react.addDenialListener((reaction, permissible, flag, pos, message) -> {
            pig.setSaddled(true);
            pig.setSaddled(false);
            player.sendInventoryContents();
        });
        return react;
    }

    @Override
    default Reaction reactPlayerAttack(MineCityForge mod, Permissible player, IItemStack stack, IEntity entity,
                                       DamageSource source, float amount, List<Permissible> attackers)
    {
        if(player instanceof IEntityPlayerMP)
            return reactInteractEntity((IEntityPlayerMP) player, entity, stack, false);
        return NoReaction.INSTANCE;
    }

    @Override
    default Reaction reactPlayerAttackDirect(IEntityPlayerMP player, IEntity target, IItemStack stack,
                                             boolean offHand)
    {
        return reactInteractEntity(player, target, stack, offHand);
    }
}
