package br.com.gamemods.minecity.forge.base.protection.reaction;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.Permissible;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.block.IBlockSnapshot;
import br.com.gamemods.minecity.reactive.reaction.TriggeredReaction;
import br.com.gamemods.minecity.structure.ClaimedChunk;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Deprecated
public class RevertDeniedReaction extends TriggeredReaction
{
    private MineCityForge mod;
    private PermissionFlag flag;
    private Deque<IBlockSnapshot> snapshots;
    private boolean approved;
    private Consumer<IBlockSnapshot> reverter;
    private Message denial;

    public RevertDeniedReaction(MineCityForge mod,
                                Collection<IBlockSnapshot> snapshots,
                                PermissionFlag flag,
                                Consumer<IBlockSnapshot> reverter)
    {
        this.mod = mod;
        this.flag = flag;
        this.snapshots = new ArrayDeque<>(snapshots);
        this.reverter = reverter;
    }

    public RevertDeniedReaction(MineCityForge mod, Collection<IBlockSnapshot> snapshots, PermissionFlag modify)
    {
        this(mod, snapshots, modify, snap-> MineCityForge.snapshotHandler.restore(Collections.singletonList(snap)));
    }

    @Override
    public Stream<Message> stream(MineCity mineCity, Permissible permissible)
    {
        if(snapshots.isEmpty())
        {
            approved = true;
            return Stream.empty();
        }

        AtomicReference<BlockPos> lastPos = new AtomicReference<>(snapshots.element().getPosition(mod));
        AtomicReference<ClaimedChunk> lastClaim = new AtomicReference<>(mineCity.provideChunk(lastPos.get().getChunk()));
        Map<Object, Optional<Message>> results = new ConcurrentHashMap<>();
        return snapshots.stream().map(snap-> {
            BlockPos pos = snap.getPosition(mod, lastPos.get());
            lastPos.set(pos);
            ClaimedChunk claim = mineCity.provideChunk(pos.getChunk(), lastClaim.get());
            lastClaim.set(claim);

            Optional<Message> prev = results.get(pos);
            if(prev != null)
                return null;

            Optional<Message> denial = claim.getFlagHolder(pos).can(permissible, flag);
            results.put(pos, denial);
            if(!denial.isPresent())
            {
                onAllow(permissible, flag, pos);
                approved = true;
                return null;
            }

            Message message = denial.get();
            this.denial = message;
            onDeny(permissible, flag, pos, message);
            reverter.accept(snap);
            if(!approved && snapshots.getLast() == snap)
                return message;
            return null;
        }).filter(m-> m != null);
    }

    @Override
    public Optional<Message> can(MineCity mineCity, Permissible permissible)
    {
        stream(mineCity, permissible).forEach(never->{});
        if(approved)
            return Optional.empty();
        return Optional.of(denial);
    }
}
