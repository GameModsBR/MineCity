package br.com.gamemods.minecity.sponge.listeners;

import br.com.gamemods.minecity.api.permission.FlagHolder;
import br.com.gamemods.minecity.reactive.game.block.ReactiveBlock;
import br.com.gamemods.minecity.reactive.game.entity.data.Hand;
import br.com.gamemods.minecity.reactive.reaction.InteractReaction;
import br.com.gamemods.minecity.sponge.MineCitySponge;
import br.com.gamemods.minecity.sponge.cmd.SpongeCommandSource;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.util.Tristate;

import java.util.concurrent.atomic.AtomicBoolean;

public class ActionListener
{
    private final MineCitySponge sponge;

    public ActionListener(MineCitySponge sponge)
    {
        this.sponge = sponge;
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onInteractBlock(final InteractBlockEvent.Secondary event, @First Player player)
    {
        ReactiveBlock block = sponge.reactiveBlock(event.getTargetBlock(), player.getWorld());
        HandType type = event.getHandType();
        InteractReaction reaction = block.rightClick(
                null,
                Hand.from(type),
                sponge.reactiveStack(player.getItemInHand(type).orElse(null)),
                null,
                event.getInteractionPoint().map(sponge::precisePoint).orElse(null)
        );

        SpongeCommandSource<?> sender = sponge.sender(player);
        AtomicBoolean notify = new AtomicBoolean(true);
        reaction.getAction().can(sponge.mineCity, sender).ifPresent(denial-> {
            event.setCancelled(true);
            if(notify.get())
            {
                sender.send(FlagHolder.wrapDeny(denial));
                notify.set(false);
            }
        });

        reaction.getUseItem().can(sponge.mineCity, sender).ifPresent(denial-> {
            event.setUseItemResult(Tristate.FALSE);
            if(notify.get())
            {
                sender.send(FlagHolder.wrapDeny(denial));
                notify.set(false);
            }
        });

        reaction.getUseBlock().can(sponge.mineCity, sender).ifPresent(denial-> {
            event.setUseBlockResult(Tristate.FALSE);
            if(notify.get())
            {
                sender.send(FlagHolder.wrapDeny(denial));
                notify.set(false);
            }
        });
    }
}
