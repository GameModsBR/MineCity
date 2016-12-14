package br.com.gamemods.minecity.vault;

import br.com.gamemods.minecity.economy.EconomyProvider;
import br.com.gamemods.minecity.permission.PermissionProvider;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

public final class VaultProviders
{
    public static final PermissionProvider PERMISSION = mineCity->
    {
        try
        {
            if(Bukkit.getPluginManager().isPluginEnabled("Vault"))
            {
                RegisteredServiceProvider<Permission> registration = Bukkit.getServicesManager().getRegistration(Permission.class);
                if(registration != null)
                {
                    Permission permission = registration.getProvider();
                    if(permission != null)
                        return new VaultPermission(permission);
                }
            }
        }
        catch(Exception e)
        {
            throw new UnsupportedOperationException("An error has occurred while enabling Vault vault integration", e);
        }

        throw new UnsupportedOperationException("Vault is not installed or is unable to provide vault support in this server");
    };

    public static final EconomyProvider ECONOMY = mineCity->
    {
        try
        {
            if(Bukkit.getPluginManager().isPluginEnabled("Vault"))
            {
                RegisteredServiceProvider<Economy> registration = Bukkit.getServicesManager().getRegistration(Economy.class);
                if(registration != null)
                {
                    Economy economy = registration.getProvider();
                    if(economy != null)
                        return new VaultEconomy(economy);
                }
            }
        }
        catch(Exception e)
        {
            throw new UnsupportedOperationException("An error has occurred while enabling Vault economy integration", e);
        }

        throw new UnsupportedOperationException("Vault is not installed or is unable to provide economy support in this server");
    };

    private VaultProviders(){}
}
