package br.com.gamemods.minecity.forge.base.protection.vanilla;

import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.permission.FlagHolder;
import br.com.gamemods.minecity.api.permission.Identity;
import br.com.gamemods.minecity.api.permission.PermissionFlag;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.structure.ClaimedChunk;
import net.minecraft.entity.player.EntityPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;

import static br.com.gamemods.minecity.api.CollectionUtil.optionalStream;
import static br.com.gamemods.minecity.api.permission.FlagHolder.can;
import static br.com.gamemods.minecity.api.permission.FlagHolder.wrapDeny;

public class ForgeProtections
{
    public final MineCityForge mod;

    public ForgeProtections(MineCityForge mod)
    {
        this.mod = mod;
    }

    protected boolean check(@NotNull BlockPos pos, @NotNull EntityPlayer player, @NotNull PermissionFlag... flags)
    {
        ForgePlayer user = mod.player(player);
        Optional<Message> denial = silentCheck(pos, user, flags);

        if(denial.isPresent())
        {
            user.send(wrapDeny(denial.get()));
            return true;
        }

        return false;
    }

    protected Optional<Message> silentCheck(@NotNull BlockPos location, @NotNull EntityPlayer player, @NotNull PermissionFlag... flags)
    {
        return silentCheck(location, mod.player(player), flags);
    }

    protected Optional<Message> silentCheck(@NotNull BlockPos blockPos, @NotNull ForgePlayer user, @NotNull PermissionFlag... flags)
    {
        ClaimedChunk chunk = mod.mineCity.provideChunk(blockPos.getChunk());
        FlagHolder holder = chunk.getFlagHolder(blockPos);

        if(flags.length == 1)
            return holder.can(user, flags[0]);
        else
        {
            //noinspection unchecked
            Supplier<Optional<Message>>[] array = Arrays.stream(flags).map(flag -> can(user, flag, holder)).toArray(Supplier[]::new);
            return optionalStream(array).findFirst();
        }
    }

    protected Optional<Message> check(@NotNull BlockPos blockPos, @NotNull Identity<?> identity, @NotNull PermissionFlag... flags)
    {
        ClaimedChunk chunk = mod.mineCity.provideChunk(blockPos.getChunk());
        FlagHolder holder = chunk.getFlagHolder(blockPos);

        if(flags.length == 1)
            return holder.can(identity, flags[0]);
        else
        {
            //noinspection unchecked
            Supplier<Optional<Message>>[] array = Arrays.stream(flags).map(flag -> can(identity, flag, holder)).toArray(Supplier[]::new);
            return optionalStream(array).findFirst();
        }
    }
}
