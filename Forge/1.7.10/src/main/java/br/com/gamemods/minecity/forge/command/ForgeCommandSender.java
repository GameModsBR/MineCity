package br.com.gamemods.minecity.forge.command;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.Server;
import br.com.gamemods.minecity.api.command.CommandSender;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.api.world.EntityPos;
import br.com.gamemods.minecity.forge.MineCityForgeMod;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.IChatComponent;
import org.jetbrains.annotations.NotNull;

public class ForgeCommandSender<S extends ICommandSender> implements CommandSender
{
    public final MineCityForgeMod mod;
    public final S sender;

    public ForgeCommandSender(MineCityForgeMod mod, S sender)
    {
        this.mod = mod;
        this.sender = sender;
    }

    @NotNull
    @Override
    public Server getServer()
    {
        return mod;
    }

    @Override
    public EntityPos getPosition()
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
    public boolean hasPermission(String perm)
    {
        return true;
    }

    @Override
    public void send(Message[] message)
    {
        /*  /tellraw ["a/nb"] does not work in 1.7.10
        ChatComponentText merge = new ChatComponentText("");
        for(int i = 0;; i++)
        {
            merge.appendSibling(ForgeUtil.chatComponentFromLegacyText(mod.mineCity.messageTransformer.toLegacy(message[i])));
            if(i+1 < message.length)
                merge.appendText("\n");
            else
                break;
        }

        sender.addChatMessage(merge);
        */

        for(Message msg : message)
            send(msg);
    }

    @Override
    public void send(Message message)
    {
        for(IChatComponent msg: mod.transformer.toMultilineForge(message))
            sender.addChatMessage(msg);
    }

    @Override
    public Direction getCardinalDirection()
    {
        return null;
    }
}
