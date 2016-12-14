package br.com.gamemods.minecity.bukkit.protection;

import br.com.gamemods.minecity.api.permission.FlagHolder;
import br.com.gamemods.minecity.bukkit.MineCityBukkit;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FlagHolderData
{
    public static final String KEY = "FlagHolderData";

    @NotNull
    public final FlagHolder home;

    public FlagHolderData(@NotNull FlagHolder home)
    {
        this.home = home;
    }

    @Nullable
    public static FlagHolder get(@NotNull MineCityBukkit bukkit, @NotNull Object obj)
    {
        if(obj instanceof Metadatable)
            for(MetadataValue meta: ((Metadatable)obj).getMetadata(FlagHolderData.KEY))
                if(meta.getOwningPlugin().equals(bukkit.plugin))
                    return ((FlagHolderData) meta.value()).home;
        return null;
    }
}
