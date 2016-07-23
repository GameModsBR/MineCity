package br.com.gamemods.minecity.api.world;

import br.com.gamemods.minecity.api.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface MinecraftEntity
{
    @NotNull
    UUID getUniqueId();

    @NotNull
    String getName();

    @NotNull
    Type getType();

    @Nullable
    CommandSender getCommandSender();

    enum Type
    {
        /**
         * A player...
         */
        PLAYER,

        /**
         * A dropped item
         */
        ITEM,

        /**
         * A decorative entity that doesn't hold items, like paintings and banners
         */
        STRUCTURE,

        /**
         * A structural entity that holds items, like frames and armor stands
         */
        STORAGE,

        /**
         * Any vehicle that is not alive, like minecarts and boats
         */
        VEHICLE,

        /**
         * Arrows, snowballs, eggs..
         */
        PROJECTILE,

        /**
         * Pacific animals like chicken and cow
         */
        ANIMAL,

        /**
         * Shh BOOM
         */
        MONSTER,

        /**
         * Something very different...
         */
        UNCLASSIFIED
    }
}
