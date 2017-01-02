package br.com.gamemods.minecity.reactive.reaction;

import br.com.gamemods.minecity.api.CollectionUtil;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.Permissible;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.shape.Point;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.reactive.ReactiveLayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class TriggeredReaction implements Reaction
{
    private List<ReactionListener> denialListeners;
    private List<ReactionListener> allowListeners;

    public void onDenySendBlockUpdateAt(int x, int y, int z)
    {
        addDenialListener((reaction, permissible, flag, pos, message) ->
                ReactiveLayer.getEntityData(permissible).ifPresent(entity-> entity.sendBlockUpdate(x, y, z))
        );
    }

    public void onDenySendBlockUpdateAt(Point first, Point... points)
    {
        addDenialListener((reaction, permissible, flag, pos, message) ->
                ReactiveLayer.getEntityData(permissible).ifPresent(entity->
                        CollectionUtil.combine(first, Arrays.stream(points)).forEach(entity::sendBlockUpdate)
                )
        );
    }

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
}
