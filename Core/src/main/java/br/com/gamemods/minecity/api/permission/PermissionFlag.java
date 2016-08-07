package br.com.gamemods.minecity.api.permission;

public enum PermissionFlag
{
    /**
     * Allows to enter the zone
     */
    ENTER(true, true),

    /**
     * Allow to click on clickable blocks that doesn't store content, like doors and buttons.
     */
    CLICK(true, false),

    /**
     * Allows to pickup drops
     */
    PICKUP(true, true),

    /**
     * Allows to open containers, like chests and furnaces
     */
    OPEN(true, false),

    /**
     * Allows to do structural modifications
     */
    MODIFY(true, false),

    /**
     * Allows to leave the zone
     */
    LEAVE(true, true),

    /**
     * Allows to attack players
     */
    PVP(true, false, false),

    /**
     * Allows to attack creatures that aren't monsters
     */
    PVC(true, false),

    /**
     * Allows to attack monsters
     */
    PVM(true, true),

    /**
     * Allows to spawn vehicles
     */
    SPAWN_VEHICLES(true, false),

    /**
     * Allows to ride vehicles
     */
    RIDE(true, true);

    public final boolean defaultNature;
    public final boolean defaultCity;
    public final boolean canBypass;

    PermissionFlag(boolean defaultNature, boolean defaultCity)
    {
        this.defaultNature = defaultNature;
        this.defaultCity = defaultCity;
        canBypass = true;
    }

    PermissionFlag(boolean defaultNature, boolean defaultCity, boolean canBypass)
    {
        this.defaultNature = defaultNature;
        this.defaultCity = defaultCity;
        this.canBypass = canBypass;
    }
}
