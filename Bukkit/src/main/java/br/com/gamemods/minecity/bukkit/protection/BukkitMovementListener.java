package br.com.gamemods.minecity.bukkit.protection;

import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.bukkit.MineCityBukkit;
import br.com.gamemods.minecity.protection.MovementListener;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public interface BukkitMovementListener extends MovementListener<Entity, MineCityBukkit>
{
    @Override
    default boolean isSafeToStep(MineCityBukkit bukkit, Entity entity, WorldDim world, int x, int y, int z)
    {
        return isSafeToStep(entity, bukkit.world(world).get().getBlockAt(x, y, z));
    }

    default boolean isSafeToStep(Entity entity, Block block)
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
                return type.isOccluding() || entity instanceof LivingEntity && ((LivingEntity) entity).isGliding() ||
                        entity instanceof Player && ((Player) entity).isFlying();
        }
    }
}
