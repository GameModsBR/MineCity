package br.com.gamemods.minecity.forge.base.accessors.item;

import br.com.gamemods.minecity.api.permission.FlagHolder;
import br.com.gamemods.minecity.api.permission.Permissible;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.shape.PrecisePoint;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntity;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.entity.item.IEntityItem;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.forge.base.core.Referenced;
import br.com.gamemods.minecity.forge.base.core.transformer.forge.ForgeInterfaceTransformer;
import br.com.gamemods.minecity.forge.base.protection.reaction.NoReaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.Reaction;
import br.com.gamemods.minecity.forge.base.protection.reaction.SingleBlockReaction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemSeedFood;
import net.minecraft.util.DamageSource;

import java.util.List;

@Referenced(at = ForgeInterfaceTransformer.class)
public interface IItem
{
    default Item getForgeItem()
    {
        return (Item) this;
    }

    default Reaction reactRightClickBlock(IEntityPlayerMP player, IItemStack stack, boolean offHand, IState state, BlockPos pos, Direction face)
    {
        return NoReaction.INSTANCE;
    }

    default String getUnlocalizedName()
    {
        return getForgeItem().getUnlocalizedName();
    }

    default Reaction reactRightClick(IEntityPlayerMP player, IItemStack stack, boolean offHand)
    {
        return NoReaction.INSTANCE;
    }

    default Reaction reactInteractEntity(IEntityPlayerMP player, IEntity target, IItemStack stack, boolean offHand)
    {
        return NoReaction.INSTANCE;
    }

    default Reaction reactPlayerAttack(MineCityForge mod, Permissible player, IItemStack stack, IEntity entity,
                                       DamageSource source, float amount, List<Permissible> attackers)
    {
        return NoReaction.INSTANCE;
    }

    default Reaction reactInteractEntityPrecisely(IEntityPlayerMP player, IEntity target, IItemStack stack, boolean offHand, PrecisePoint point)
    {
        return NoReaction.INSTANCE;
    }

    default Reaction onPlayerPickup(IEntityPlayerMP entity, IEntity item)
    {
        return new SingleBlockReaction(item.getBlockPos(entity.getServer()), PermissionFlag.PICKUP);
    }

    default Reaction reactItemToss(ForgePlayer<?,?,?> player, IItemStack stack, IEntityItem entityItem)
    {
        MineCityForge mod = player.getServer();
        IEntityPlayerMP entityPlayer = player.cmd.sender;
        SingleBlockReaction reaction = new SingleBlockReaction(entityItem.getBlockPos(mod), PermissionFlag.PICKUP);
        reaction.addDenialListener((reaction1, permissible, flag, p, message) ->
                mod.callSyncMethod(() -> {
                    if(!entityItem.isDead() && entityItem.getPickupDelay() < 32000)
                    {
                        entityPlayer.attemptToReturn(stack);
                        entityPlayer.sendChanges();
                        player.send(FlagHolder.wrapDeny(message));
                        entityItem.setDead();
                    }
                })
        );
        return reaction;
    }

    default boolean isHarvest(IItemStack stack)
    {
        return this instanceof ItemSeedFood || this instanceof ItemFood
                || getUnlocalizedName().equals("item.wheat")
                ;
    }
}
