package br.com.gamemods.minecity.bukkit;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class BukkitUtil
{
    @Nullable
    @SuppressWarnings("unchecked")
    public static  <T> T getMeta(@NotNull Plugin plugin, @NotNull String key, @NotNull Metadatable metadatable)
    {
        for(MetadataValue meta : metadatable.getMetadata(key))
        {
            if(meta.getOwningPlugin().equals(plugin))
                return (T) meta.value();
        }

        return null;
    }

    @NotNull
    public static BlockFace right(@NotNull BlockFace face)
    {
        switch(face)
        {
            case NORTH:
                return BlockFace.EAST;
            case EAST:
                return BlockFace.SOUTH;
            case SOUTH:
                return BlockFace.WEST;
            case WEST:
                return BlockFace.NORTH;
            default:
                return face;
        }
    }

    public static Optional<ItemStack> optional(@Nullable ItemStack stack)
    {
        if(stack == null || stack.getType() == Material.AIR)
            return Optional.empty();

        return Optional.of(stack);
    }

    public static Optional<ItemStack> optional(@Nullable ItemStack stack, @NotNull Material type)
    {
        if(stack == null || stack.getType() == Material.AIR)
            return Optional.empty();

        if(stack.getType() == type)
            return Optional.of(stack);

        return Optional.empty();
    }
}
