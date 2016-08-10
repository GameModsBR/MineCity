package br.com.gamemods.minecity.api.permission;

public enum PermissionFlag
{
    /**
     * Allows to enter the zone
     */
    ENTER(true, true, true),

    /**
     * Allow to click on clickable blocks that doesn't store content, like doors and buttons.
     */
    CLICK(true, false, false),

    /**
     * Allows to pickup drops
     */
    PICKUP(true, true, false),

    /**
     * Allows to open containers, like chests and furnaces
     */
    OPEN(true, false, false),

    /**
     * Allows to do structural modifications
     */
    MODIFY(true, false, false),

    /**
     * Allows to leave the zone
     */
    LEAVE(true, true, true),

    /**
     * Allows to attack players
     */
    PVP(true, false, false, false),

    /**
     * Allows to attack creatures that aren't monsters
     */
    PVC(true, false, false),

    /**
     * Allows to attack monsters
     */
    PVM(true, true, false),

    /**
     * Allows to spawn vehicles
     */
    SPAWN_VEHICLES(true, false, false),

    /**
     * Allows to ride vehicles
     */
    RIDE(true, true, false);

    public final boolean defaultNature;
    public final boolean defaultCity;
    public final boolean defaultPlot;
    public final boolean canBypass;

    PermissionFlag(boolean defaultNature, boolean defaultCity, boolean defaultPlot)
    {
        this.defaultNature = defaultNature;
        this.defaultCity = defaultCity;
        this.defaultPlot = defaultPlot;
        canBypass = true;
    }

    PermissionFlag(boolean defaultNature, boolean defaultCity, boolean defaultPlot, boolean canBypass)
    {
        this.defaultNature = defaultNature;
        this.defaultCity = defaultCity;
        this.defaultPlot = defaultPlot;
        this.canBypass = canBypass;
    }
}
