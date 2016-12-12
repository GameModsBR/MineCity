package br.com.gamemods.minecity.reactive.reaction;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.Permissible;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.stream.Stream;

public class MultiBlockReaction extends TriggeredReaction
{
    private final PermissionFlag flag;
    private final Collection<BlockPos> blocks;

    public MultiBlockReaction(@NotNull PermissionFlag flag, @NotNull Collection<BlockPos> blocks)
    {
        this(flag, new LinkedHashSet<>(blocks));
    }

    private MultiBlockReaction(@NotNull PermissionFlag flag, @NotNull  LinkedHashSet<BlockPos> blocks)
    {
        this.flag = flag;
        this.blocks = blocks;
    }

    @Override
    public Stream<Message> stream(MineCity mineCity, Permissible permissible)
    {
        return blocks.stream()
                .map(pos-> {
                    Optional<Message> denial = mineCity.provideChunk(pos.getChunk()).getFlagHolder(pos)
                            .can(permissible, flag);
                    if(denial.isPresent())
                        onDeny(permissible, flag, pos, denial.get());
                    else
                        onAllow(permissible, flag, pos);

                    return denial;
                })
                .filter(Optional::isPresent).map(Optional::get)
        ;
    }

    public static TriggeredReaction create(@NotNull PermissionFlag flag, @NotNull Collection<BlockPos> blocks)
    {
        int size = blocks.size();
        if(size == 1)
            return new SingleBlockReaction(blocks.iterator().next(), flag);
        else if(size == 2)
        {
            Iterator<BlockPos> iterator = blocks.iterator();
            return new DoubleBlockReaction(flag, iterator.next(), iterator.next());
        }

        return new MultiBlockReaction(flag, blocks);
    }
}
