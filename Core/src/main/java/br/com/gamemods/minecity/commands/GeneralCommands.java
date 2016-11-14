package br.com.gamemods.minecity.commands;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.api.Async;
import br.com.gamemods.minecity.api.Slow;
import br.com.gamemods.minecity.api.command.*;
import br.com.gamemods.minecity.datasource.api.DataSourceException;
import br.com.gamemods.minecity.structure.Nature;
import org.xml.sax.SAXException;

import java.io.IOException;

public class GeneralCommands
{
    @Async
    @Command(value = "confirm", console = true, args = @Arg(name = "confirmation code", type = Arg.Type.PREDEFINED))
    public static CommandResult<?> confirm(CommandEvent cmd) throws Exception
    {
        if(!cmd.sender.isConfirmPending())
            return new CommandResult<>(new Message("cmd.confirm.not-pending", "You don't have any pending confirmation"));

        if(cmd.args.size() != 1)
            return new CommandResult<>(new Message("cmd.confirm.args", "Type the confirmation code to confirm the action or just ignore it if you don't want to continue."));

        CommandResult<CommandResult<?>> confirm = cmd.sender.confirm(cmd.args.get(0));
        if(!confirm.success)
            return new CommandResult<>(new Message("cmd.confirm.invalid", "You've typed an incorrect code, if you want to abort the operation just don't type the /confirm command."));

        return confirm.result;
    }

    @Command("reload")
    public static CommandResult<?> reload(CommandEvent cmd) throws IOException, SAXException
    {
        String lang = cmd.mineCity.locale.toLanguageTag();
        cmd.mineCity.messageTransformer.parseXML(MineCity.class.getResourceAsStream("/assets/minecity/messages-"+lang+".xml"));
        return CommandResult.success();
    }

    @Async
    @Slow
    @Command(value = "nature.rename", args = @Arg(name = "new-name", sticky = true))
    public static CommandResult<?> changeNatureName(CommandEvent cmd) throws DataSourceException
    {
        if(cmd.args.isEmpty())
            return new CommandResult<>(new Message("cmd.nature.rename.empty", "Please type a name"));

        Nature nature = cmd.mineCity.nature(cmd.position.world);
        Message old = nature.ownerName();
        nature.setName(String.join(" ", cmd.args));
        return new CommandResult<>(new Message("cmd.nature.rename.success",
                "The nature DIM:${dim} DIR:${dir} was renamed from \"${old}\" to \"${new}\"",
                new Object[][]{
                        {"dim", nature.world.dim},
                        {"dir", nature.world.dir},
                        {"old", old},
                        {"new", nature.ownerName()}
                }
        ), true);
    }

    @Command("admin")
    public static CommandResult<Boolean> adminMode(CommandEvent cmd)
    {
        cmd.sender.toggleAdminMode();
        if(cmd.sender.isAdminMode())
            return new CommandResult<>(new Message("cmd.mode.adm.enabled","Admin Mode is now enabled, everything will be free, unrestricted and unlimited"), Boolean.TRUE);
        else
            return new CommandResult<>(new Message("cmd.mode.adm.disabled","Admin Mode is now disabled, prices, restrictions and limits will be applied"), Boolean.FALSE);
    }
}
