package br.com.gamemods.minecity.forge.base.protection.thaumcraft;

import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.forge.base.accessors.IRayTraceResult;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityLivingBase;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.item.IItemStack;
import br.com.gamemods.minecity.forge.base.accessors.world.IWorldServer;
import br.com.gamemods.minecity.forge.base.core.ModEnv;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.mod.ModInterfacesTransformer;
import br.com.gamemods.minecity.reactive.reaction.NoReaction;
import br.com.gamemods.minecity.reactive.reaction.Reaction;
import net.minecraft.item.ItemStack;

@Referenced(at = ModInterfacesTransformer.class)
public interface IItemFocusTrade extends IItemFocusBasic
{
    ItemStack getPickedBlock(ItemStack wand);

    @Override
    default Reaction reactLivingSwing(IEntityLivingBase living, IItemStack stack)
    {
        IItemStack picked = (IItemStack) (Object) getPickedBlock(stack.getStack());
        if(!(living instanceof IEntityPlayerMP) || picked == null)
            return NoReaction.INSTANCE;

        IEntityPlayerMP player = (IEntityPlayerMP) living;
        IWorldServer world = player.getIWorld();
        IRayTraceResult trace = ThaumHooks.getFocusTradeTraceFromPlayer(this, world, player);
        if(trace == null || trace.getHitType() != 1)
            return NoReaction.INSTANCE;

        BlockPos hit = trace.getHitBlockPos(ModEnv.dimSupplier.apply(world));
        if(world.getTileEntity(hit) != null)
            return NoReaction.INSTANCE;

        IState state = world.getIState(hit);
        return state.getIBlock().reactBlockBreak(player.getServer().player(player), state, hit)
                .combine(picked.getIItem().reactPrePlace(player, picked, hit))
                ;
    }
}
