package br.com.gamemods.minecity.bukkit.protection;

import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.structure.City;
import br.com.gamemods.minecity.structure.Nature;
import br.com.gamemods.minecity.structure.Plot;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface MovementListener
{
    Optional<Message> onCityChange(@NotNull City city, @Nullable Plot plot);
    Optional<Message> onPlotEnter(@NotNull Plot plot);
    Optional<Message> onPlotLeave(@NotNull City city);
    Optional<Message> onCityLeave(@NotNull Nature nature);
    Optional<Message> onNatureChange(@NotNull Nature nature);

    default boolean isSafeToStep(LivingEntity entity, Block block)
    {
        Material type = block.getType();
        switch(type)
        {
            case WATER:
            case STATIONARY_WATER:
            case LAVA:
            case STATIONARY_LAVA:
            case GLASS:
            case LEAVES:
            case LEAVES_2:
            case PISTON_STICKY_BASE:
            case PISTON_BASE:
            case SOIL:
            case ICE:
            case GLOWSTONE:
            case STAINED_GLASS:
            case DRAGON_EGG:
            case BEACON:
            case COBBLE_WALL:
            case SEA_LANTERN:
            case GRASS_PATH:
                return true;
            default:
                return type.isOccluding() || entity.isGliding() ||
                        entity instanceof Player && ((Player) entity).isFlying();
        }
    }
}
