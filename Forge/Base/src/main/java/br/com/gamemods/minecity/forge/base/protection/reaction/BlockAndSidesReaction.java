package br.com.gamemods.minecity.forge.base.protection.reaction;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.Permissible;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.structure.ClaimedChunk;

import java.util.EnumSet;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Stream;

public class BlockAndSidesReaction extends TriggeredReaction
{
    private BlockPos base;
    private PermissionFlag flag;
    private EnumSet<Direction> sides;

    public BlockAndSidesReaction(PermissionFlag flag, BlockPos base, EnumSet<Direction> sides)
    {
        this.base = base;
        this.flag = flag;
        this.sides = sides;
    }

    public BlockAndSidesReaction(PermissionFlag flag, BlockPos base, Direction d1)
    {
        this(flag, base, EnumSet.of(d1));
    }

    public BlockAndSidesReaction(PermissionFlag flag, BlockPos base, Direction d1, Direction d2)
    {
        this(flag, base, EnumSet.of(d1, d2));
    }

    public BlockAndSidesReaction(PermissionFlag flag, BlockPos base)
    {
        this(flag, base, EnumSet.of(Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.EAST));
        sides.add(Direction.WEST);
    }

    @Override
    public Stream<Message> stream(MineCity mineCity, Permissible permissible)
    {
        AtomicReference<ClaimedChunk> lastClaim = new AtomicReference<>();
        return Stream.of(
                Stream.of(base),
                sides.stream().map(base::add)
        ).flatMap(Function.identity()).map(pos-> {
            ClaimedChunk claim = mineCity.provideChunk(pos.getChunk(), lastClaim.get());
            lastClaim.set(claim);
            Optional<Message> denial = claim.getFlagHolder(pos).can(permissible, flag);
            if(denial.isPresent())
            {
                Message message = denial.get();
                onDeny(permissible, flag, pos, message);
                return message;
            }
            else
            {
                onAllow(permissible, flag, pos);
                return null;
            }
        }).filter(m-> m != null);
    }
}
