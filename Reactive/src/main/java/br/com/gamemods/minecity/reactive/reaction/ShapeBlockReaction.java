package br.com.gamemods.minecity.reactive.reaction;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.api.CollectionUtil;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.Permissible;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.shape.Shape;
import br.com.gamemods.minecity.api.world.WorldDim;

import java.util.Optional;
import java.util.stream.Stream;

public class ShapeBlockReaction extends TriggeredReaction
{
    private WorldDim world;
    private Shape shape;
    private PermissionFlag flag;

    public ShapeBlockReaction(WorldDim world, Shape shape, PermissionFlag flag)
    {
        this.world = world;
        this.shape = shape;
        this.flag = flag;
    }

    @Override
    public Stream<Message> stream(MineCity mineCity, Permissible permissible)
    {
        return CollectionUtil.stream(shape.blockIterator(world))
                .map(pos-> {
                    Optional<Message> denial = mineCity.provideChunk(pos.getChunk()).getFlagHolder(pos)
                            .can(permissible, flag);
                    if(denial.isPresent())
                        onDeny(mineCity, permissible, flag, pos, denial.get());
                    else
                        onAllow(mineCity, permissible, flag, pos);

                    return denial;
                })
                .filter(Optional::isPresent).map(Optional::get)
                ;
    }
}
