package br.com.gamemods.minecity.forge.base.protection.reaction;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.Permissible;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class SingleBlockReaction extends TriggeredReaction
{
    @NotNull
    private BlockPos pos;

    @NotNull
    private PermissionFlag flag;

    public SingleBlockReaction(@NotNull BlockPos pos, @NotNull PermissionFlag flag)
    {
        this.pos = pos;
        this.flag = flag;
    }

    @Override
    public Stream<Message> stream(MineCity mineCity, Permissible permissible)
    {
        Supplier<Optional<Message>> check = ()-> can(mineCity, permissible);
        return Stream.of(check).map(Supplier::get).filter(Optional::isPresent).map(Optional::get);
    }

    @Override
    public Optional<Message> can(MineCity mineCity, Permissible permissible)
    {
        Optional<Message> denial = mineCity.provideChunk(pos.getChunk()).getFlagHolder(pos).can(permissible, flag);
        if(denial.isPresent())
            onDeny(permissible, flag, pos, denial.get());
        else
            onAllow(permissible, flag, pos);
        return denial;
    }

    @Override
    public boolean equals(Object o)
    {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        SingleBlockReaction reaction = (SingleBlockReaction) o;
        return pos.equals(reaction.pos) && flag == reaction.flag;
    }

    @Override
    public int hashCode()
    {
        int result = pos.hashCode();
        result = 31*result + flag.hashCode();
        return result;
    }
}
