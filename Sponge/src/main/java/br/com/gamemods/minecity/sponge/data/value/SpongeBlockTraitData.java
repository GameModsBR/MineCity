package br.com.gamemods.minecity.sponge.data.value;

import br.com.gamemods.minecity.reactive.game.block.data.BlockTraitData;
import br.com.gamemods.minecity.reactive.reactor.Manipulator;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.block.trait.BlockTrait;

import java.util.Collection;
import java.util.function.Predicate;

public class SpongeBlockTraitData<T extends Comparable<T>> implements BlockTraitData<T>
{
    private final Manipulator manipulator;
    public final BlockTrait<T> trait;

    public SpongeBlockTraitData(Manipulator manipulator, BlockTrait<T> trait)
    {
        this.manipulator = manipulator;
        this.trait = trait;
    }

    @Override
    public BlockTrait<T> getBlockTrait()
    {
        return trait;
    }

    @NotNull
    @Override
    public String getName()
    {
        return trait.getName();
    }

    @NotNull
    @Override
    public Collection<T> getPossibleValues()
    {
        return trait.getPossibleValues();
    }

    @NotNull
    @Override
    public Class<T> getValueClass()
    {
        return trait.getValueClass();
    }

    @NotNull
    @Override
    public Predicate<T> getValidator()
    {
        return trait.getPredicate();
    }
}
