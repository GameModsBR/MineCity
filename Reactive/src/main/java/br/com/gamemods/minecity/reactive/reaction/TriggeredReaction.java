package br.com.gamemods.minecity.reactive.reaction;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.api.CollectionUtil;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.FlagHolder;
import br.com.gamemods.minecity.api.permission.Permissible;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.shape.Point;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.reactive.ReactiveLayer;
import br.com.gamemods.minecity.reactive.game.entity.data.EntityData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public abstract class TriggeredReaction implements Reaction
{
    private List<ReactionListener> denialListeners;
    private List<ReactionListener> allowListeners;

    public TriggeredReaction onDenySendInventoryUpdate()
    {
        return onDenyDo(EntityData::sendInventoryUpdate);
    }

    public TriggeredReaction onDenySendBlockUpdateAt(int x, int y, int z)
    {
        return onDenyDo(entity-> entity.sendBlockUpdate(x, y, z));
    }

    public TriggeredReaction onDenySendBlockUpdateAt(Point point)
    {
        return onDenySendBlockUpdateAt(point.x, point.y, point.z);
    }

    public TriggeredReaction onDenySendBlockUpdateAt(Point first, Point... points)
    {
        return onDenyDo(entity ->
                CollectionUtil.combine(first, Arrays.stream(points))
                        .forEach(entity::sendBlockUpdate)
        );
    }

    public TriggeredReaction onDenySendHunger()
    {
        return onDenyDo(EntityData::sendHungerUpdate);
    }

    public TriggeredReaction onDenyDo(Consumer<EntityData> entityConsumer)
    {
        return addDenialListener((mineCity, reaction, permissible, flag, pos, message) ->
                ReactiveLayer.getEntityData(permissible).ifPresent(entityConsumer)
        );
    }

    public TriggeredReaction onDenySendMessage(Message message)
    {
        return addDenialListener((mineCity, reaction, permissible, flag, pos, denial) -> permissible.send(message));
    }

    public TriggeredReaction onDenySendMessage(Message[] messages)
    {
        return addDenialListener((mineCity, reaction, permissible, flag, pos, denial) -> permissible.send(messages));
    }

    public TriggeredReaction onDenySendDenialMessage(Message message)
    {
        return addDenialListener((mineCity, reaction, permissible, flag, pos, denial) -> {
            message.lastFlag = flag;
            permissible.send(FlagHolder.wrapDeny(message));
        });
    }

    public TriggeredReaction onDenySendDenialMessage(Message[] messages)
    {
        return addDenialListener((mineCity, reaction, permissible, flag, pos, denial) -> {
            for(int i = 0; i < messages.length; i++)
            {
                messages[i].lastFlag = flag;
                messages[i] = FlagHolder.wrapDeny(messages[i]);
            }

            Arrays.stream(messages).forEach(message -> message.lastFlag = flag);
            permissible.send(messages);
        });
    }

    public TriggeredReaction onDenySendDenialMessage(PermissionFlag flag, Message message)
    {
        return addDenialListener((mineCity, reaction, permissible, denialFlag, pos, denial) -> {
            message.lastFlag = flag;
            permissible.send(FlagHolder.wrapDeny(message));
        });
    }

    public TriggeredReaction onDenySendDenialMessage(PermissionFlag flag, Message[] messages)
    {
        return addDenialListener((mineCity, reaction, permissible, denialFlag, pos, denial) -> {
            for(int i = 0; i < messages.length; i++)
            {
                messages[i].lastFlag = flag;
                messages[i] = FlagHolder.wrapDeny(messages[i]);
            }

            Arrays.stream(messages).forEach(message -> message.lastFlag = flag);
            permissible.send(messages);
        });
    }

    public TriggeredReaction onAllowExecNextTick(Runnable runnable)
    {
        return addAllowListener((mineCity, reaction, permissible, flag, pos, message) -> mineCity.server.callSyncMethod(runnable));
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

    protected void onDeny(MineCity mineCity, Permissible permissible, PermissionFlag flag, BlockPos pos,
                          Message message)
    {
        if(denialListeners == null)
            return;

        denialListeners.forEach(listener -> listener.postReaction(mineCity, this, permissible, flag, pos, message));
    }

    protected void onAllow(MineCity mineCity, Permissible permissible, PermissionFlag flag, BlockPos pos)
    {
        if(allowListeners == null)
            return;

        allowListeners.forEach(listener -> listener.postReaction(mineCity, this, permissible, flag, pos, null));
    }
}
