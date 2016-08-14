package br.com.gamemods.minecity.bukkit;

import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static br.com.gamemods.minecity.bukkit.BukkitUtil.optional;

public class BukkitUtil19
{
    public static Optional<ItemStack> getItemInSlot(@NotNull PlayerInventory inventory, @NotNull EquipmentSlot slot)
    {
        switch(slot)
        {
            case HAND:
                return optional(inventory.getItemInMainHand());
            case OFF_HAND:
                return optional(inventory.getItemInOffHand());
            case HEAD:
                return optional(inventory.getHelmet());
            case CHEST:
                return optional(inventory.getChestplate());
            case LEGS:
                return optional(inventory.getLeggings());
            case FEET:
                return optional(inventory.getBoots());
            default:
                return Optional.empty();
        }
    }
}
