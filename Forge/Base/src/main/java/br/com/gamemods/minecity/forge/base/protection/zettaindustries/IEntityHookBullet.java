package br.com.gamemods.minecity.forge.base.protection.zettaindustries;

import br.com.gamemods.minecity.api.permission.Permissible;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.IRayTraceResult;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityLivingBase;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItem;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.forge.base.protection.immersiveengineering.IEntityRevolverShot;
import br.com.gamemods.minecity.forge.base.protection.immersiveengineering.IItemWireCoil;
import br.com.gamemods.minecity.forge.base.protection.immersiveengineering.ImmersiveHooks;
import br.com.gamemods.minecity.forge.base.protection.reaction.NoReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;

import java.util.List;

@Referenced(at = ModInterfacesTransformer.class)
public interface IEntityHookBullet extends IEntityRevolverShot
{
    @Override
    default Reaction reactImpactPre(MineCityForge mod, IRayTraceResult traceResult, Permissible who,
                                    List<Permissible> relative)
    {
        if(traceResult.getHitType() != 1)
            return NoReaction.INSTANCE;

        IEntityLivingBase shooter = ImmersiveHooks.getShootingEntity(this);
        if(!(shooter instanceof IEntityPlayerMP))
            return NoReaction.INSTANCE;

        IEntityPlayerMP player = (IEntityPlayerMP) shooter;
        int next = (player.getSelectedSlot() + 1) % 10;
        IItemStack stack = player.getStackInSlot(next);
        if(stack == null)
            return NoReaction.INSTANCE;

        IItem item = stack.getIItem();
        if(!(item instanceof IItemWireCoil))
            return NoReaction.INSTANCE;

        BlockPos hit = traceResult.getHitBlockPos(mod.world(getIWorld()));
        return item.reactRightClickBlockFirstUse(player, stack, false, player.getIWorld().getIState(hit), hit, traceResult.getHitSide());
    }
}
