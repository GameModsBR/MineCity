package br.com.gamemods.minecity.reactive.reaction;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.Permissible;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.stream.Stream;

public class DoubleBlockReaction extends TriggeredReaction
{
    @NotNull
    private BlockPos a, b;

    @NotNull
    PermissionFlag flag;

    public DoubleBlockReaction(@NotNull PermissionFlag flag, @NotNull BlockPos a, @NotNull BlockPos b)
    {
        this.a = a;
        this.b = b;
        this.flag = flag;
    }

    @Override
    public Stream<Message> stream(MineCity mineCity, Permissible permissible)
    {
        return Stream.of(a, b).map(pos -> {
            Optional<Message> denial = mineCity.provideChunk(pos.getChunk()).getFlagHolder(pos)
                    .can(permissible, flag);
            if(denial.isPresent())
                onDeny(permissible, flag, pos, denial.get());
            else
                onAllow(permissible, flag, pos);
            return denial;
        }).filter(Optional::isPresent).map(Optional::get);
    }
}
