package br.com.gamemods.minecity.api.permission;

public enum PermissionFlag
{
    /**
     * Allows to enter the zone
     */
    ENTER(true, true, true, true),

    /**
     * Allow to click on clickable blocks that doesn't store content, like doors and buttons.
     */
    CLICK(true, true, false, false),

    /**
     * Allows to pickup drops
     */
    PICKUP(true, true, true, false),

    /**
     * Allows to harvest crops, milk and wool. The crops will auto-replant.
     */
    HARVEST(true, true, true, false),

    /**
     * Allows to open containers, like chests and furnaces
     */
    OPEN(true, true, false, false),

    /**
     * Allows to do structural modifications
     */
    MODIFY(true, false, false, false),

    /**
     * Allows to leave the zone
     */
    LEAVE(true, true, true, true),

    /**
     * Allows to attack players
     */
    PVP(true, true, false, false, false),

    /**
     * Allows to attack creatures that aren't monsters
     */
    PVC(true, true, false, false),

    /**
     * Allows to attack monsters
     */
    PVM(true, true, true, false),

    /**
     * Allows to spawn vehicles
     */
    VEHICLE(true, true, false, false),

    /**
     * Allows to ride vehicles
     */
    RIDE(true, true, true, false);

    public final boolean defaultNature;
    public final boolean defaultReserve;
    public final boolean defaultCity;
    public final boolean defaultPlot;
    public final boolean canBypass;

    PermissionFlag(boolean defaultNature, boolean defaultReserve, boolean defaultCity, boolean defaultPlot)
    {
        this.defaultNature = defaultNature;
        this.defaultReserve = defaultReserve;
        this.defaultCity = defaultCity;
        this.defaultPlot = defaultPlot;
        canBypass = true;
    }

    PermissionFlag(boolean defaultNature, boolean defaultReserve, boolean defaultCity, boolean defaultPlot, boolean canBypass)
    {
        this.defaultNature = defaultNature;
        this.defaultReserve = defaultReserve;
        this.defaultCity = defaultCity;
        this.defaultPlot = defaultPlot;
        this.canBypass = canBypass;
    }
}
