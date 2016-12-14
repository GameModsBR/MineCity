package br.com.gamemods.minecity.sponge.cmd;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.sponge.MineCitySponge;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.entity.living.player.Player;

public class PlayerSender extends LivingSource<Player, Player>
{
    public PlayerSender(MineCitySponge server, Player source)
    {
        super(server, source, source);
    }

    @Override
    public boolean isPlayer()
    {
        return true;
    }

    @Override
    public PlayerID getPlayerId()
    {
        return PlayerID.get(source.getUniqueId(), source.getName());
    }

    @NotNull
    @Override
    public PlayerID identity()
    {
        return getPlayerId();
    }
}
