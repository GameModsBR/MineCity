package br.com.gamemods.minecity.forge.command;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.command.CommandSender;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.forge.ForgeUtil;
import br.com.gamemods.minecity.forge.MineCityForgeMod;
import net.minecraft.command.ICommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    @Nullable
    @Override
    public Message teleport(@NotNull BlockPos spawn)
    {
        return new Message("action.teleport.unsupported", "Unsupported operation");
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
        sender.addChatMessage(ForgeUtil.chatComponentFromLegacyText(mod.mineCity.messageTransformer.toLegacy(message)));
    }

    @Override
    public Direction getCardinalDirection()
    {
        return null;
    }
}
