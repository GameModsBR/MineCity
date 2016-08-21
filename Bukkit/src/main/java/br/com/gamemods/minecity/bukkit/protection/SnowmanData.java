package br.com.gamemods.minecity.bukkit.protection;

import br.com.gamemods.minecity.api.permission.FlagHolder;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.bukkit.MineCityBukkit;
import br.com.gamemods.minecity.structure.ClaimedChunk;
import br.com.gamemods.minecity.structure.Nature;
import org.bukkit.block.Block;
import org.bukkit.entity.Snowman;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings("serial")
public class SnowmanData
{
    public static final String KEY = "SnowmanData";

    @NotNull
    public final FlagHolder home;

    @NotNull
    private final MineCityBukkit bukkit;

    @NotNull
    private ClaimedChunk claim;
    private Map<BlockPos, Boolean> last = new LinkedHashMap<BlockPos, Boolean>(4)
    {
        @Override
        protected boolean removeEldestEntry(Map.Entry eldest)
        {
            return size() >= 4;
        }
    };

    public SnowmanData(@NotNull MineCityBukkit bukkit, @NotNull ClaimedChunk claim, @NotNull BlockPos pos)
    {
        this.bukkit = bukkit;
        this.claim = claim;
        home = claim.getFlagHolder(pos);
        last.put(pos, home instanceof Nature);
    }

    public boolean checkFormSnow(Snowman entity, Block block)
    {
        if(claim.isInvalid())
            claim = bukkit.mineCity.provideChunk(claim.chunk);

        int cx = block.getX();
        int cy = block.getY();
        int cz = block.getZ();

        BlockPos pos = null;
        for(Map.Entry<BlockPos, Boolean> entry : last.entrySet())
        {
            pos = entry.getKey();
            if(pos.x == cx && pos.y == cy && pos.z == cz)
                return entry.getValue();
        }
        assert pos != null; // Pos is never null because the "last" map is never empty

        BlockPos current = bukkit.blockPos(pos, block);
        ClaimedChunk claim = this.claim = bukkit.mineCity.provideChunk(current.getChunk(), this.claim);
        FlagHolder holder = claim.getFlagHolder(current);
        if(holder.equals(home))
        {
            boolean result = holder instanceof Nature;
            last.put(current, result);
            return result;
        }

        BlockPos entityLoc = bukkit.blockPos(entity.getLocation());
        if(entityLoc.equals(pos) || !claim.getFlagHolder(entityLoc).equals(holder))
            entity.damage(1000);

        last.put(current, true);
        return true;
    }
}
