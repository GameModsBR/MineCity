package br.com.gamemods.minecity.forge.command;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.command.CommandSender;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.forge.ForgeUtil;
import br.com.gamemods.minecity.forge.MineCityForgeMod;
import net.minecraft.command.ICommandSender;

public class ForgeCommandSender<S extends ICommandSender> implements CommandSender
{
    public final MineCityForgeMod mod;
    public final S sender;

    public ForgeCommandSender(MineCityForgeMod mod, S sender)
    {
        this.mod = mod;
        this.sender = sender;
    }

    @Override
    public BlockPos getPosition()
    {
        return null;
    }

    @Override
    public boolean isPlayer()
    {
        return false;
    }

    @Override
    public PlayerID getPlayerId()
    {
        return null;
    }

    @Override
    public void send(Message message)
    {
        sender.addChatMessage(ForgeUtil.chatComponentFromLegacyText(mod.mineCity.messageTransformer.toLegacy(message)));
    }
}
