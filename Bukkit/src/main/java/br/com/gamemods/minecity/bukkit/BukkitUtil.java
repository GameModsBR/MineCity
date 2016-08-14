package br.com.gamemods.minecity.bukkit;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class BukkitUtil
{
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
