package br.com.gamemods.minecity.reactive.reaction;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.Permissible;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public class ApproveReaction extends TriggeredReaction
{
    @NotNull
    private PermissionFlag flag;
    @NotNull
    private BlockPos pos;

    public ApproveReaction(@NotNull BlockPos pos, @NotNull PermissionFlag flag)
    {
        this.pos = pos;
        this.flag = flag;
    }

    @Override
    public Stream<Message> stream(MineCity mineCity, Permissible permissible)
    {
        onAllow(permissible, flag, pos);
        return Stream.empty();
    }
}
