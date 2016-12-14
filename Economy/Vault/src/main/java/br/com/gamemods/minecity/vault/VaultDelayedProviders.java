package br.com.gamemods.minecity.vault;

import br.com.gamemods.minecity.economy.DelayedEconomy;
import br.com.gamemods.minecity.economy.EconomyProvider;
import br.com.gamemods.minecity.permission.DelayedPermission;
import br.com.gamemods.minecity.permission.PermissionProvider;

public final class VaultDelayedProviders
{
    public static final EconomyProvider ECONOMY = mineCity -> new DelayedEconomy(mineCity, VaultProviders.ECONOMY);
    public static final PermissionProvider PERMISSION = mineCity -> new DelayedPermission(mineCity, VaultProviders.PERMISSION);

    private VaultDelayedProviders(){}
}
