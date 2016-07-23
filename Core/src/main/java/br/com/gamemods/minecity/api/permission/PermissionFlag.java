package br.com.gamemods.minecity.api.permission;

public enum PermissionFlag
{
    /**
     * Allows to enter the zone
     */
    ENTER,

    /**
     * Allow to click on clickable blocks that doesn't store content, like doors and buttons.
     */
    CLICK,

    /**
     * Allows to pickup drops
     */
    PICKUP,

    /**
     * Allows to open containers, like chests and furnaces
     */
    OPEN,

    /**
     * Allows to leave the zone
     */
    LEAVE,

    /**
     * Allows to attack players
     */
    PVP(false),

    /**
     * Allows to attack creatures that aren't monsters
     */
    PVC,

    /**
     * Allows to attack monsters
     */
    PVM,

    /**
     * Allows to spawn vehicles
     */
    SPAWN_VEHICLES,

    /**
     * Allows to ride vehicles
     */
    RIDE;

    public final boolean canBypass;

    PermissionFlag(boolean canBypass)
    {
        this.canBypass = canBypass;
    }

    PermissionFlag()
    {
        canBypass = true;
    }
}
