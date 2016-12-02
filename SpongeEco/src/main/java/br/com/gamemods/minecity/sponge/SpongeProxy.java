package br.com.gamemods.minecity.sponge;

import br.com.gamemods.minecity.api.Async;
import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.command.CommandSender;
import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.economy.BalanceResult;
import br.com.gamemods.minecity.economy.EconomyProxy;
import br.com.gamemods.minecity.economy.OperationResult;
import br.com.gamemods.minecity.permission.Permission;
import br.com.gamemods.minecity.permission.PermissionProxy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.service.ChangeServiceProviderEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Plugin(id = "minecitysponge", dependencies = @Dependency(id="minecity"), name = "MineCity-Sponge",
        description = "MineCity's module that implements Sponge's Economy and Permission support.")
public class SpongeProxy implements EconomyProxy, PermissionProxy
{
    private final SpongeBalance NO_ACCOUNT = new SpongeBalance(false);

    @Nullable
    private EconomyService eco;

    @Nullable
    private UserStorageService userStorage;

    @Listener
    public void onEconomyChange(ChangeServiceProviderEvent event)
    {
        Class<?> service = event.getService();
        if(service.equals(EconomyService.class))
            eco = (EconomyService) event.getNewProvider();
        else if(service.equals(UserStorageService.class))
            userStorage = (UserStorageService) event.getNewProvider();
    }

    @Override
    public boolean hasPermission(CommandSender sender, Permission perm)
    {
        Object handler = sender.getHandler();
        if(handler instanceof Subject)
            return ((Subject) handler).hasPermission(perm.getKey());

        if(!sender.isPlayer())
            return sender.isOp();

        UserStorageService storage = this.userStorage;
        if(storage == null)
            this.userStorage = storage = Sponge.getServiceManager().provide(UserStorageService.class)
                    .orElseThrow(() -> new IllegalStateException("No user storage service was found!"));

        Optional<User> result = storage.get(sender.getPlayerId().getUniqueId());
        return result.isPresent() && result.get().hasPermission(perm.getKey());
    }

    @NotNull
    private EconomyService requireEconomy()
    {
        EconomyService eco = this.eco;
        if(eco == null)
            throw new IllegalStateException("The economy service was not set");
        return eco;
    }

    @NotNull
    private Set<Context> context(@NotNull WorldDim world)
    {
        Set<Context> context = new HashSet<>(1);
        context.add(new Context(Context.WORLD_KEY, world.dir));
        return context;
    }

    @NotNull
    private UniqueAccount account(@NotNull EconomyService eco, @NotNull PlayerID player)
    {
        return eco.getOrCreateAccount(player.uniqueId).orElseThrow(()-> new IllegalStateException("The "+player+"'s account has disappeared!"));
    }

    @Override
    @Async
    public BalanceResult has(@NotNull PlayerID player, double amount, @NotNull WorldDim world)
    {
        // TODO Disable this check? https://docs.spongepowered.org/master/plugin/economy/practices.html
        EconomyService eco = requireEconomy();
        if(!eco.hasAccount(player.uniqueId))
            return NO_ACCOUNT;

        UniqueAccount account = account(eco, player);
        Currency currency = eco.getDefaultCurrency();
        Set<Context> context = context(world);
        return new SpongeBalance(account, currency, context, account.hasBalance(currency, context));
    }

    @Override
    @Async
    public BalanceResult has(@NotNull PlayerID player, double amount)
    {
        // TODO Disable this check? https://docs.spongepowered.org/master/plugin/economy/practices.html
        EconomyService eco = requireEconomy();
        if(!eco.hasAccount(player.uniqueId))
            return NO_ACCOUNT;

        UniqueAccount account = account(eco, player);
        Currency currency = eco.getDefaultCurrency();
        return new SpongeBalance(account, currency, null, account.hasBalance(currency));
    }

    @NotNull
    private Currency currency(@NotNull EconomyService eco, @Nullable Set<Context> context, @Nullable BalanceResult balance)
    {
        Currency currency = null;
        if(balance instanceof SpongeBalance)
        {
            SpongeBalance bal = (SpongeBalance) balance;
            if(Objects.equals(context, bal.context))
                currency = ((SpongeBalance) balance).currency;
        }

        return currency == null? eco.getDefaultCurrency() : currency;
    }

    @Nullable
    private UniqueAccount account(@NotNull EconomyService eco, @Nullable Set<Context> context, @Nullable BalanceResult balance, PlayerID player)
    {
        UniqueAccount account = null;
        if(balance instanceof SpongeBalance)
        {
            SpongeBalance bal = (SpongeBalance) balance;
            if(Objects.equals(context, bal.context))
                account = ((SpongeBalance) balance).account;
        }

        return account != null? account : eco.getOrCreateAccount(player.uniqueId).orElse(null);
    }

    private OperationResult failed(ResultType result, PlayerID player, double amount)
    {
        switch(result)
        {
            case ACCOUNT_NO_FUNDS: return new OperationResult(false, amount, player.getName()+"'s has insufficient funds");
            case ACCOUNT_NO_SPACE: return new OperationResult(false, amount, player.getName()+"'s account is full");
            case FAILED: return new OperationResult(false, amount, "Unknown economy error on "+player.getName()+"'s account");
            default: return new OperationResult(false, amount, player.getName()+"'s account: "+result.name());
        }
    }

    @Override
    @Async
    public OperationResult take(@NotNull PlayerID player, double amount, @Nullable BalanceResult balance, boolean simulation) throws IllegalArgumentException
    {
        EconomyService eco = requireEconomy();
        UniqueAccount account = account(eco, null, balance, player);
        if(account == null)
            return new OperationResult(false, amount, "Failed to find "+player.getName()+"'s account.");

        Currency currency = currency(eco, null, balance);
        BigDecimal bigAmount = BigDecimal.valueOf(amount);
        if(simulation)
        {
            BigDecimal delta = account.getBalance(currency).subtract(bigAmount);
            if(delta.compareTo(BigDecimal.ZERO) < 0)
                return new OperationResult(false, delta.doubleValue(), "Insufficient funds (simulation)");
            return new OperationResult(true, delta.doubleValue());
        }

        TransactionResult transaction = account.withdraw(currency, bigAmount, Cause.source(this).build());
        ResultType result = transaction.getResult();
        if(result == ResultType.SUCCESS)
            return new OperationResult(true, transaction.getAmount().subtract(bigAmount).doubleValue());
        return failed(result, player, amount);
    }

    @Override
    @Async
    public OperationResult give(@NotNull PlayerID player, double amount, @Nullable BalanceResult balance, boolean simulation) throws IllegalArgumentException
    {
        EconomyService eco = requireEconomy();
        UniqueAccount account = account(eco, null, balance, player);
        if(account == null)
            return new OperationResult(false, amount, "Failed to find "+player.getName()+"'s account.");

        Currency currency = currency(eco, null, balance);
        BigDecimal bigAmount = BigDecimal.valueOf(amount);
        if(simulation)
        {
            BigDecimal delta = account.getBalance(currency).add(bigAmount);
            return new OperationResult(true, delta.doubleValue());
        }

        TransactionResult transaction = account.deposit(currency, bigAmount, Cause.source(this).build());
        ResultType result = transaction.getResult();
        if(result == ResultType.SUCCESS)
            return new OperationResult(true, transaction.getAmount().subtract(bigAmount).doubleValue());
        return failed(result, player, amount);
    }

    @Override
    @Async
    public OperationResult take(@NotNull PlayerID player, double amount, @Nullable BalanceResult balance, @NotNull WorldDim world, boolean simulation) throws IllegalArgumentException
    {
        EconomyService eco = requireEconomy();
        Set<Context> context = context(world);
        UniqueAccount account = account(eco, context, balance, player);
        if(account == null)
            return new OperationResult(false, amount, "Failed to find "+player.getName()+"'s account.");

        Currency currency = currency(eco, context, balance);
        BigDecimal bigAmount = BigDecimal.valueOf(amount);
        if(simulation)
        {
            BigDecimal delta = account.getBalance(currency, context).subtract(bigAmount);
            if(delta.compareTo(BigDecimal.ZERO) < 0)
                return new OperationResult(false, delta.doubleValue(), "Insufficient funds (simulation)");
            return new OperationResult(true, delta.doubleValue());
        }

        TransactionResult transaction = account.withdraw(currency, bigAmount, Cause.source(this).build(), context);
        ResultType result = transaction.getResult();
        if(result == ResultType.SUCCESS)
            return new OperationResult(true, transaction.getAmount().subtract(bigAmount).doubleValue());
        return failed(result, player, amount);
    }

    @Override
    @Async
    public OperationResult give(@NotNull PlayerID player, double amount, @Nullable BalanceResult balance, @NotNull WorldDim world, boolean simulation) throws IllegalArgumentException
    {
        EconomyService eco = requireEconomy();
        Set<Context> context = context(world);
        UniqueAccount account = account(eco, context, balance, player);
        if(account == null)
            return new OperationResult(false, amount, "Failed to find "+player.getName()+"'s account.");

        Currency currency = currency(eco, context, balance);
        BigDecimal bigAmount = BigDecimal.valueOf(amount);
        if(simulation)
        {
            BigDecimal delta = account.getBalance(currency, context).add(bigAmount);
            return new OperationResult(true, delta.doubleValue());
        }

        TransactionResult transaction = account.deposit(currency, bigAmount, Cause.source(this).build(), context);
        ResultType result = transaction.getResult();
        if(result == ResultType.SUCCESS)
            return new OperationResult(true, transaction.getAmount().subtract(bigAmount).doubleValue());
        return failed(result, player, amount);
    }

    @Override
    public String format(double amount)
    {
        EconomyService eco = requireEconomy();
        return TextSerializers.LEGACY_FORMATTING_CODE.serialize(eco.getDefaultCurrency().format(BigDecimal.valueOf(amount)));
    }

    private class SpongeBalance extends BalanceResult
    {
        @Nullable
        private UniqueAccount account;
        @Nullable
        private Currency currency;
        @Nullable
        private Set<Context> context;

        private SpongeBalance(@NotNull UniqueAccount account, @NotNull Currency currency, @Nullable Set<Context> context, boolean result)
        {
            super(result);
            this.account = account;
            this.currency = currency;
            this.context = context == null? null : new HashSet<>(context);
        }

        private SpongeBalance(boolean result)
        {
            super(result);
        }
    }
}
