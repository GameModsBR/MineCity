package br.com.gamemods.minecity.api.permission;

import br.com.gamemods.minecity.api.command.Message;

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
    public final Message header;

    PermissionFlag(boolean defaultNature, boolean defaultReserve, boolean defaultCity, boolean defaultPlot)
    {
        header = header();
        this.defaultNature = defaultNature;
        this.defaultReserve = defaultReserve;
        this.defaultCity = defaultCity;
        this.defaultPlot = defaultPlot;
        canBypass = true;
    }

    PermissionFlag(boolean defaultNature, boolean defaultReserve, boolean defaultCity, boolean defaultPlot, boolean canBypass)
    {
        header = header();
        this.defaultNature = defaultNature;
        this.defaultReserve = defaultReserve;
        this.defaultCity = defaultCity;
        this.defaultPlot = defaultPlot;
        this.canBypass = canBypass;
    }

    @SuppressWarnings("LanguageMismatch")
    private Message header()
    {
        String name = name();
        if(name.length() != 3)
            name = name.charAt(0) + name.substring(1).toLowerCase();
        return new Message("action.denied.perm."+name.toLowerCase(), name);
    }
}
