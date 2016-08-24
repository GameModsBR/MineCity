package br.com.gamemods.minecity.forge.mc_1_7_10.command;

import br.com.gamemods.minecity.api.command.LegacyFormat;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.forge.mc_1_7_10.MineCityForge7;
import net.minecraft.entity.player.EntityPlayerMP;

public class SevenPlayer extends ForgePlayer<MineCityForge7, EntityPlayerMP, SevenPlayerSender>
{
    public SevenPlayer(MineCityForge7 mod, EntityPlayerMP player)
    {
        super(new SevenPlayerSender(mod, player));
    }

    @Override
    public void sendTitle(Message title, Message subtitle)
    {
        if(subtitle == null)
            send(new Message("",LegacyFormat.DARK_GRAY+" ~ "+LegacyFormat.GRAY+"${name}", new Object[]{"name", title}));
        else
            send(new Message("",LegacyFormat.DARK_GRAY+" ~ ${title} :"+LegacyFormat.GRAY+" ${sub}", new Object[][]{
                    {"sub", subtitle},
                    {"title", title}
            }));
    }

    @Override
    public boolean kick(Message message)
    {
        cmd.sender.playerNetServerHandler.kickPlayerFromServer(cmd.mod.transformer.toLegacy(message));
        return true;
    }
}
