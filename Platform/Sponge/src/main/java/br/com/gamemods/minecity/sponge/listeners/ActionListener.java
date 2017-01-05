package br.com.gamemods.minecity.sponge.listeners;

import br.com.gamemods.minecity.api.permission.FlagHolder;
import br.com.gamemods.minecity.api.permission.Permissible;
import br.com.gamemods.minecity.api.shape.PrecisePoint;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.api.world.MinecraftEntity;
import br.com.gamemods.minecity.reactive.ReactiveLayer;
import br.com.gamemods.minecity.reactive.game.block.BlockChange;
import br.com.gamemods.minecity.reactive.game.block.Modification;
import br.com.gamemods.minecity.reactive.game.block.PreModification;
import br.com.gamemods.minecity.reactive.game.block.ReactiveBlock;
import br.com.gamemods.minecity.reactive.game.block.data.BlockSnapshotData;
import br.com.gamemods.minecity.reactive.game.entity.ReactiveEntity;
import br.com.gamemods.minecity.reactive.game.entity.data.EntityData;
import br.com.gamemods.minecity.reactive.game.entity.data.Hand;
import br.com.gamemods.minecity.reactive.game.item.ReactiveItemStack;
import br.com.gamemods.minecity.reactive.reaction.InteractReaction;
import br.com.gamemods.minecity.reactive.reaction.Reaction;
import br.com.gamemods.minecity.sponge.MineCitySponge;
import br.com.gamemods.minecity.sponge.data.manipulator.boxed.MineCityKeys;
import br.com.gamemods.minecity.structure.DisplayedSelection;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.entity.ArmorEquipable;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.entity.living.humanoid.HandInteractEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.cause.Named;
import org.spongepowered.api.event.filter.type.Exclude;
import org.spongepowered.api.event.impl.AbstractEvent;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.Location;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;

import static java.util.stream.Collectors.toList;

public class ActionListener
{
    private final MineCitySponge sponge;

    @Nullable
    private HandInteractEvent lastEntityInteractEvent;

    public ActionListener(MineCitySponge sponge)
    {
        this.sponge = sponge;
    }

    @Listener(order = Order.PRE)
    @Exclude(MoveEntityEvent.class)
    public void debug(AbstractEvent event, @First Player player)
    {
        sponge.logger.info("Event: "+event);
    }

    @Listener(order = Order.POST)
    public void onInteract(HandInteractEvent event, @Named(NamedCause.SOURCE) Entity entity)
    {
        lastEntityInteractEvent = event;
    }

    @Nullable
    private ReactiveItemStack getStackFromEntity(Entity entity, HandType hand)
    {
        return entity instanceof ArmorEquipable
                ? sponge.reactiveStack(((ArmorEquipable)entity).getItemInHand(hand).orElse(null))
                : null;
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onItemSpawn(SpawnEntityEvent event)
    {
        event.getEntities().removeIf(subject-> {
            Optional<ReactiveEntity> opt = ReactiveLayer.getEntityReactor().getReactiveEntity(subject);
            if(!opt.isPresent())
                return false;

            MinecraftEntity entity = sponge.entity(subject);
            return opt.get().reactSpawn(entity, sponge.entityPos(subject.getLocation())).can(sponge.mineCity, entity).isPresent();
        });
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onInteractBlock(InteractBlockEvent.Secondary event, @Named(NamedCause.SOURCE) Entity subject)
    {
        HandType handType = event.getHandType();
        Hand hand = Hand.from(handType);
        if(subject instanceof Player)
        {
            Player player = (Player) subject;
            int tool = player
                    .getItemInHand(handType)
                    .flatMap(stack -> stack.get(MineCityKeys.ITEM_TOOL))
                    .orElse(0)
                    ;

            if(tool == 1)
            {
                event.setCancelled(true);

                BlockPos block = sponge.blockPos(event.getTargetBlock().getLocation().get());
                DisplayedSelection<?> selection = sponge.player(player).getSelection(block.world);

                if(player.get(Keys.IS_SNEAKING).orElse(false))
                {
                    if(hand == Hand.OFF)
                        selection.b = block;
                    else
                        selection.a = block;

                    selection.normalize();
                    Task.builder().async().execute(selection::updateDisplay).submit(sponge.plugin);
                }
                else
                    Task.builder().async().execute(()->selection.select(block)).submit(sponge.plugin);

                return;
            }
        }

        Direction side = sponge.direction(event.getTargetSide());
        PrecisePoint point = event.getInteractionPoint().map(sponge::precisePoint).orElse(null);

        EntityData entity = ReactiveLayer.getEntityData(subject).get();
        ReactiveBlock block = sponge.reactiveBlock(event.getTargetBlock(), subject.getWorld());
        ReactiveItemStack stack = getStackFromEntity(subject, handType);

        InteractReaction reaction = entity.onRightClick(hand, stack, block, side, point);

        Permissible sender = sponge.permissible(subject);
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

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onBlockPlaceByEntity(ChangeBlockEvent.Place event, @Named(NamedCause.SOURCE) Entity subject)
    {
        onBlockChangeByEntity(event, subject, EntityData::onBlockPlace);
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onBlockBreakByEntity(ChangeBlockEvent.Break event, @Named(NamedCause.SOURCE) Entity subject)
    {
        onBlockChangeByEntity(event, subject, EntityData::onBlockBreak);
    }

    /*
    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onBlockReplaceByEntity(ChangeBlockEvent.Modify event, @Named(NamedCause.SOURCE) Entity subject)
    {
        onBlockChangeByEntity(event, subject, EntityData::onBlockReplace);
    }
    */

    @Listener(order  = Order.FIRST, beforeModifications = true)
    public void onBlockGrowByEntity(ChangeBlockEvent.Grow event, @Named(NamedCause.SOURCE) Entity subject)
    {
        onBlockChangeByEntity(event, subject, EntityData::onBlockGrow);
    }

    private EntityData requireEntity(Entity entity)
    {
        return ReactiveLayer.getEntityData(entity).get();
    }

    private void onBlockChangeByEntity(ChangeBlockEvent event, Entity subject, BiFunction<EntityData, Modification, Reaction> operation)
    {
        Hand hand = null;
        ReactiveItemStack stack = null;

        HandInteractEvent interact = this.lastEntityInteractEvent;
        if(interact != null)
        {
            HandType handType = interact.getHandType();
            hand = Hand.from(handType);
            stack = getStackFromEntity(subject, handType);
        }

        onBlockChange(event, subject, requireEntity(subject), hand, stack, operation);
    }

    private BlockSnapshotData requireBlock(BlockSnapshot snapshot)
    {
        return ReactiveLayer.getBlockSnapshotData(snapshot).get();
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onBlockPlaceByBlock(ChangeBlockEvent.Place event, @Named(NamedCause.SOURCE) BlockSnapshot subject)
    {
        onBlockChangeByBlock(event, subject, BlockSnapshotData::onBlockPlace);
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onBlockBreakByBlock(ChangeBlockEvent.Break event, @Named(NamedCause.SOURCE) BlockSnapshot subject)
    {
        onBlockChangeByBlock(event, subject, BlockSnapshotData::onBlockBreak);
    }

    /*
    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onBlockReplaceByBlock(ChangeBlockEvent.Modify event, @Named(NamedCause.SOURCE) BlockSnapshot subject)
    {
        onBlockChangeByBlock(event, subject, BlockSnapshotData::onBlockReplace);
    }
    */

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onBlockGrowByBlock(ChangeBlockEvent.Grow event, @Named(NamedCause.SOURCE) BlockSnapshot subject)
    {
        onBlockChangeByBlock(event, subject, BlockSnapshotData::onBlockGrow);
    }

    private void onBlockChangeByBlock(ChangeBlockEvent event, BlockSnapshot source, BiFunction<BlockSnapshotData, Modification, Reaction> operation)
    {
        onBlockChange(event, source, requireBlock(source), null, null, operation);
    }

    private <D> void onBlockChange(ChangeBlockEvent event, Object subject, D cause, @Nullable Hand hand,
                                   @Nullable ReactiveItemStack stack, BiFunction<D, Modification, Reaction> operation)
    {
        List<BlockChange> changeList = event.getTransactions().stream()
                .map(tran -> new BlockChange(
                        ReactiveLayer.getBlockSnapshotData(tran.getOriginal()).get(),
                        ReactiveLayer.getBlockSnapshotData(tran.getFinal()).get()
                )).collect(toList());

        Modification modification = new Modification(changeList, cause, stack, hand);
        Reaction reaction = operation.apply(cause, modification);

        Permissible sender = sponge.permissible(subject);
        reaction.can(sponge.mineCity, sender).ifPresent(reason-> {
            event.setCancelled(true);
            sender.send(FlagHolder.wrapDeny(reason));
        });
    }

    //@Listener(order = Order.FIRST, beforeModifications = true)
    public void onBlockChangePre(ChangeBlockEvent.Pre event, @Named(NamedCause.SOURCE) Entity subject)
    {
        HandInteractEvent interact = this.lastEntityInteractEvent;
        if(interact != null && interact.getCause().first(Entity.class).orElse(null) != subject)
            interact = null;

        Hand hand = null;
        ReactiveItemStack stack = null;

        if(interact != null)
        {
            HandType handType = interact.getHandType();
            hand = Hand.from(handType);
            stack = getStackFromEntity(subject, handType);
        }

        EntityData entity = ReactiveLayer.getEntityData(subject).get();
        List<BlockSnapshotData> changeList = event.getLocations().stream()
                .map(Location::createSnapshot).map(ReactiveLayer::getBlockSnapshotData)
                .map(Optional::get).collect(toList());

        PreModification modification = new PreModification(changeList, entity, stack, hand);
        Reaction reaction = entity.onBlockChangePre(modification);

        Permissible sender = sponge.permissible(subject);
        reaction.can(sponge.mineCity, sender).ifPresent(reason-> {
            event.setCancelled(true);
            sender.send(FlagHolder.wrapDeny(reason));
        });
    }

    //@Listener(order = Order.FIRST, beforeModifications = true)
    public void onBlockChangePre(ChangeBlockEvent.Pre event, @Named(NamedCause.SOURCE) BlockSnapshot subject)
    {
        BlockSnapshotData snapshot = ReactiveLayer.getBlockSnapshotData(subject).get();
        List<BlockSnapshotData> changeList = event.getLocations().stream()
                .map(Location::createSnapshot).map(ReactiveLayer::getBlockSnapshotData)
                .map(Optional::get).collect(toList());

        PreModification modification = new PreModification(changeList, snapshot, null, null);
        Reaction reaction = snapshot.onBlockChangePre(modification);

        Permissible sender = sponge.permissible(subject);
        reaction.can(sponge.mineCity, sender).ifPresent(reason-> {
            event.setCancelled(true);
            sender.send(FlagHolder.wrapDeny(reason));
        });
    }

    @Override
    public String toString()
    {
        return "ActionListener{"+
                "lastEntityInteractEvent="+lastEntityInteractEvent+
                '}';
    }
}
