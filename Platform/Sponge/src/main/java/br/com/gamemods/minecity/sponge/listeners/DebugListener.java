package br.com.gamemods.minecity.sponge.listeners;

import br.com.gamemods.minecity.sponge.MineCitySponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.action.CollideEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.type.Exclude;
import org.spongepowered.api.event.impl.AbstractEvent;

public class DebugListener
{
    private final MineCitySponge sponge;

    public DebugListener(MineCitySponge sponge)
    {
        this.sponge = sponge;
    }

    @Listener(order = Order.PRE)
    @Exclude({MoveEntityEvent.class, CollideEvent.class})
    public void debug(AbstractEvent event, @First Player player)
    {
        sponge.logger.info("Event: "+event);
    }
}
