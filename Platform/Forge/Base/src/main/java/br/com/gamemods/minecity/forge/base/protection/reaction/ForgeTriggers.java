package br.com.gamemods.minecity.forge.base.protection.reaction;

import br.com.gamemods.minecity.api.permission.Permissible;
import br.com.gamemods.minecity.api.shape.PrecisePoint;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.entity.item.IEntityItem;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.forge.base.command.ForgePlayerSender;
import br.com.gamemods.minecity.reactive.reaction.TriggeredReaction;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

@Deprecated
public interface ForgeTriggers
{
    @NotNull
    default TriggeredReaction self()
    {
        return (TriggeredReaction) this;
    }

    default TriggeredReaction onDenyUpdateInventory()
    {
        return self().addDenialListener((reaction, permissible, flag, pos, message) -> {
            if(permissible instanceof ForgePlayer)
                permissible = (Permissible) ((ForgePlayer) permissible).player;
            else if(permissible instanceof ForgePlayerSender)
                permissible = (Permissible) ((ForgePlayerSender) permissible).sender;

            if(permissible instanceof IEntityPlayerMP)
            {
                IEntityPlayerMP player = (IEntityPlayerMP) permissible;
                player.getMineCityPlayer().getServer().callSyncMethod(player::sendInventoryContents);
            }
        });
    }

    default TriggeredReaction onDenyCloseScreen(IEntityPlayerMP player)
    {
        return self().addDenialListener((reaction, permissible, flag, pos, message) ->
                player.closeScreen()
        );
    }

    default TriggeredReaction onDenyUpdateBlock(IEntityPlayerMP player)
    {
        return self().addDenialListener((reaction, permissible, flag, pos, message) ->
                player.getServer().callSyncMethod(() ->
                        player.sendBlock(pos)
                )
        );
    }

    default TriggeredReaction onDenyUpdateBlockAndTile(IEntityPlayerMP player)
    {
        return self().addDenialListener((reaction, permissible, flag, pos, message) ->
                player.getServer().callSyncMethod(() ->
                        player.sendBlockAndTile(pos)
                )
        );
    }

    default TriggeredReaction onDenyUpdateBlockAndTileForced(IEntityPlayerMP player)
    {
        return self().addDenialListener((reaction, permissible, flag, pos, message) ->
                player.getServer().callSyncMethod(() ->
                {
                    player.sendFakeAir(pos);
                    player.sendBlockAndTile(pos);
                })
        );
    }

    default TriggeredReaction allowToPickupHarvest(IEntityPlayerMP player)
    {
        return allowToPickup(player, item-> item.getStack().getIItem().isHarvest(item.getStack()));
    }

    default TriggeredReaction allowToPickup(IEntityPlayerMP player, Predicate<IEntityItem> cond)
    {
        return self().addAllowListener((reaction, permissible, flag, pos, message) ->
                player.getServer().consumeItemsOrAddOwnerIf(pos.toEntity(), 1.05, 1, 1, null, player.identity(), cond)
        );
    }

    default TriggeredReaction allowToPickup(IEntityPlayerMP player, PrecisePoint precisePos, Predicate<IEntityItem> cond)
    {
        return allowToPickup(player, precisePos, 1.05, cond);
    }

    default TriggeredReaction allowToPickup(IEntityPlayerMP player, PrecisePoint precisePos, double maxDistance, Predicate<IEntityItem> cond)
    {
        return self().addAllowListener((reaction, permissible, flag, pos, message) ->
                player.getServer().consumeItemsOrAddOwnerIf(precisePos, maxDistance, 1, 1, null, player.identity(), cond)
        );
    }
}
