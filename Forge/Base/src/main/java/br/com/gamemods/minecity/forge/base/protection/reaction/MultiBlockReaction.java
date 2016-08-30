package br.com.gamemods.minecity.forge.base.protection.reaction;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.Permissible;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class MultiBlockReaction extends TriggeredReaction
{
    private PermissionFlag flag;
    private List<BlockPos> blocks;

    public MultiBlockReaction(@NotNull PermissionFlag flag, @NotNull Collection<BlockPos> blocks)
    {
        this.flag = flag;
        this.blocks = new ArrayList<>(blocks);
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
}
