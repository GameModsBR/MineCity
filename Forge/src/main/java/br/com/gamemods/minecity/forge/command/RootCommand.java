package br.com.gamemods.minecity.forge.command;

import br.com.gamemods.minecity.api.command.CommandInfo;
import br.com.gamemods.minecity.api.command.CommandResult;
import br.com.gamemods.minecity.api.command.LegacyFormat;
import br.com.gamemods.minecity.forge.ForgeUtil;
import br.com.gamemods.minecity.forge.MineCityForgeMod;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RootCommand<T> implements ICommand
{
    public final MineCityForgeMod mod;
    public String name;
    public CommandInfo<T> info;

    public RootCommand(MineCityForgeMod mod, CommandInfo<T> info)
    {
        this.mod = mod;
        this.info = info;
        name = info.getName();
    }

    @Override
    public String getCommandName()
    {
        return name;
    }

    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return info.syntax;
    }

    @Override
    public List getCommandAliases()
    {
        return new ArrayList<>(info.aliases);
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args)
    {
        String[] path = new String[args.length+1];
        path[0] = name;
        System.arraycopy(args, 0, path, 1, args.length);
        CommandResult result = mod.mineCity.commands.invoke(mod.sender(sender), path);
        if(result.message == null)
            return;

        EnumChatFormatting header;
        EnumChatFormatting message;
        String messageL;
        if(result.success)
        {
            header = EnumChatFormatting.BLUE;
            message = EnumChatFormatting.GRAY;
            messageL = LegacyFormat.GRAY.toString();
        }
        else
        {
            header = EnumChatFormatting.DARK_RED;
            message = EnumChatFormatting.RED;
            messageL = LegacyFormat.RED.toString();
        }

        sender.addChatMessage(new ChatComponentText("MineCity> ")
                .setChatStyle(new ChatStyle().setColor(header))
                .appendSibling(
                        ForgeUtil.chatComponentFromLegacyText(mod.mineCity.messageTransformer.toLegacy(result.message, messageL))
                            .setChatStyle(new ChatStyle().setColor(message))
                )
        );
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender)
    {
        return true;
    }

    @Override
    public List addTabCompletionOptions(ICommandSender p_71516_1_, String[] p_71516_2_)
    {
        return null;
    }

    @Override
    public boolean isUsernameIndex(String[] p_82358_1_, int p_82358_2_)
    {
        return false;
    }

    @Override
    public int compareTo(@NotNull Object o)
    {
        return this.getCommandName().compareTo(((ICommand)o).getCommandName());
    }
}
