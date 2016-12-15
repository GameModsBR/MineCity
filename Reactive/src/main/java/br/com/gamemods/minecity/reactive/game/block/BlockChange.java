package br.com.gamemods.minecity.reactive.game.block;

import br.com.gamemods.minecity.reactive.game.block.data.BlockSnapshotData;
import org.jetbrains.annotations.NotNull;

public final class BlockChange
{
    @NotNull
    private final BlockSnapshotData original;

    @NotNull
    private final BlockSnapshotData replaced;

    private boolean valid = true;

    public BlockChange(@NotNull BlockSnapshotData original, @NotNull BlockSnapshotData replaced)
    {
        this.original = original;
        this.replaced = replaced;
    }

    @NotNull
    public BlockSnapshotData getOriginal()
    {
        return original;
    }

    @NotNull
    public BlockSnapshotData getReplaced()
    {
        return replaced;
    }

    public boolean isValid()
    {
        return valid;
    }

    public void setValid(boolean valid)
    {
        this.valid = valid;
    }

    @Override
    public boolean equals(Object o)
    {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        BlockChange that = (BlockChange) o;
        return valid == that.valid && original.equals(that.original) && replaced.equals(that.replaced);
    }

    @Override
    public int hashCode()
    {
        int result = original.hashCode();
        result = 31 * result+replaced.hashCode();
        result = 31 * result+(valid ? 1 : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return "BlockChange{"+
                "original="+original+
                ", replaced="+replaced+
                ", valid="+valid+
                '}';
    }
}
