package br.com.gamemods.minecity.forge.base.protection;

import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.Permissible;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.forge.base.accessors.IEntityPlayerMP;

import java.util.ArrayList;
import java.util.List;

public abstract class TriggeredReaction implements Reaction
{
    private List<ReactionListener> denialListeners;

    public void addDenialListener(ReactionListener listener)
    {
        if(denialListeners == null)
            denialListeners = new ArrayList<>(2);
        denialListeners.add(listener);
    }

    public void onDenyUpdateInventory()
    {
        addDenialListener((reaction, permissible, flag, pos, message) -> {
            if(permissible instanceof IEntityPlayerMP)
            {
                IEntityPlayerMP player = (IEntityPlayerMP) permissible;
                player.getMineCityPlayer().getServer().callSyncMethod(player::sendInventoryContents);
            }
        });
    }

    protected void onDeny(Permissible permissible, PermissionFlag flag, BlockPos pos, Message message)
    {
        if(denialListeners == null)
            return;

        denialListeners.forEach(listener -> listener.postReaction(this, permissible, flag, pos, message));
    }
}
