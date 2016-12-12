package br.com.gamemods.minecity.reactive.reaction;

import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.Permissible;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;

import java.util.ArrayList;
import java.util.List;

public abstract class TriggeredReaction implements Reaction
{
    private List<ReactionListener> denialListeners;
    private List<ReactionListener> allowListeners;

    public TriggeredReaction addDenialListener(ReactionListener listener)
    {
        if(denialListeners == null)
            denialListeners = new ArrayList<>(1);
        denialListeners.add(listener);
        return this;
    }

    public TriggeredReaction addAllowListener(ReactionListener listener)
    {
        if(allowListeners == null)
            allowListeners = new ArrayList<>(1);
        allowListeners.add(listener);
        return this;
    }

    protected void onDeny(Permissible permissible, PermissionFlag flag, BlockPos pos, Message message)
    {
        if(denialListeners == null)
            return;

        denialListeners.forEach(listener -> listener.postReaction(this, permissible, flag, pos, message));
    }

    protected void onAllow(Permissible permissible, PermissionFlag flag, BlockPos pos)
    {
        if(allowListeners == null)
            return;

        allowListeners.forEach(listener -> listener.postReaction(this, permissible, flag, pos, null));
    }

    /*

    public TriggeredReaction onDenyUpdateInventory()
    {
        return addDenialListener((reaction, permissible, flag, pos, message) -> {
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

    public TriggeredReaction onDenyCloseScreen(IEntityPlayerMP player)
    {
        return addDenialListener((reaction, permissible, flag, pos, message) ->
                player.closeScreen()
        );
    }

    public TriggeredReaction onDenyUpdateBlock(IEntityPlayerMP player)
    {
        return addDenialListener((reaction, permissible, flag, pos, message) ->
                player.getServer().callSyncMethod(() ->
                        player.sendBlock(pos)
                )
        );
    }

    public TriggeredReaction onDenyUpdateBlockAndTile(IEntityPlayerMP player)
    {
        return addDenialListener((reaction, permissible, flag, pos, message) ->
                player.getServer().callSyncMethod(() ->
                    player.sendBlockAndTile(pos)
                )
        );
    }

    public TriggeredReaction onDenyUpdateBlockAndTileForced(IEntityPlayerMP player)
    {
        return addDenialListener((reaction, permissible, flag, pos, message) ->
                player.getServer().callSyncMethod(() ->
                {
                    player.sendFakeAir(pos);
                    player.sendBlockAndTile(pos);
                })
        );
    }

    public TriggeredReaction allowToPickupHarvest(IEntityPlayerMP player)
    {
        return allowToPickup(player, item-> item.getStack().getIItem().isHarvest(item.getStack()));
    }

    public TriggeredReaction allowToPickup(IEntityPlayerMP player, Predicate<IEntityItem> cond)
    {
        return addAllowListener((reaction, permissible, flag, pos, message) ->
                player.getServer().consumeItemsOrAddOwnerIf(pos.toEntity(), 1.05, 1, 1, null, player.identity(), cond)
        );
    }

    public TriggeredReaction allowToPickup(IEntityPlayerMP player, PrecisePoint precisePos, Predicate<IEntityItem> cond)
    {
        return allowToPickup(player, precisePos, 1.05, cond);
    }

    public TriggeredReaction allowToPickup(IEntityPlayerMP player, PrecisePoint precisePos, double maxDistance, Predicate<IEntityItem> cond)
    {
        return addAllowListener((reaction, permissible, flag, pos, message) ->
                player.getServer().consumeItemsOrAddOwnerIf(precisePos, maxDistance, 1, 1, null, player.identity(), cond)
        );
    }
    */
}
