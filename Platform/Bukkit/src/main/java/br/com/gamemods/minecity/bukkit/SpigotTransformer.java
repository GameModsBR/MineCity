package br.com.gamemods.minecity.bukkit;

import br.com.gamemods.minecity.api.command.LegacyFormat;
import br.com.gamemods.minecity.api.command.Message;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.entity.Player;

import java.util.Locale;

public class SpigotTransformer extends BukkitTransformer
{
    @Override
    public void send(Player player, Message message)
    {
        player.spigot().sendMessage(toSpigot(message));
    }

    @Override
    public void send(Player player, Message[] messages)
    {
        send(player, Message.list(messages, new Message("\n")));
    }

    public BaseComponent toSpigot(Message message)
    {
        Component component = toComponent(message);
        component.apply(Locale.getDefault(), message.getArgs());
        return toSpigot(component);
    }

    protected BaseComponent toSpigot(Component component)
    {
        net.md_5.bungee.api.chat.TextComponent text;
        if(component instanceof TextComponent)
            text = toSpigot((TextComponent) component);
        else
            return new net.md_5.bungee.api.chat.TextComponent(
                    net.md_5.bungee.api.chat.TextComponent.fromLegacyText(
                            component.toString()
                    )
            );

        component.extra.forEach((c)-> text.addExtra(toSpigot(c)));
        return text;
    }

    protected net.md_5.bungee.api.chat.TextComponent toSpigot(TextComponent component)
    {
        net.md_5.bungee.api.chat.TextComponent text =
                new net.md_5.bungee.api.chat.TextComponent(component.literalValue());
        applyProperties(component, text);
        return text;
    }

    protected ClickEvent toSpigot(Click click)
    {
        if(click instanceof ClickCommand)
        {
            ClickEvent.Action action;
            ClickCommand cmd = (ClickCommand) click;
            switch(cmd.action)
            {
                case RUN: action = ClickEvent.Action.RUN_COMMAND; break;
                case SUGGEST: action = ClickEvent.Action.SUGGEST_COMMAND; break;
                case OPEN_URL: action = ClickEvent.Action.OPEN_URL; break;
                default: return null;
            }

            return new ClickEvent(action, cmd.value);
        }

        return null;
    }


    protected HoverEvent toSpigot(Hover hover)
    {
        if(hover instanceof HoverMessage)
            return new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{
                toSpigot((Component) (((HoverMessage) hover).message))
            });

        if(hover instanceof HoverAchievement)
            return new HoverEvent(HoverEvent.Action.SHOW_ACHIEVEMENT, new BaseComponent[]{
                    new net.md_5.bungee.api.chat.TextComponent(
                            ((HoverAchievement) hover).id
                    )
            });

        return null;
    }

    protected void applyProperties(Component from, BaseComponent to)
    {
        boolean colorized = from.color != null && from.color != LegacyFormat.RESET;
        if((from.color == null || from.color == LegacyFormat.RESET) && from.style.isEmpty() && from.hover == null && from.click == null)
            return;

        if(colorized)
            to.setColor((ChatColor) from.color.server);

        for(LegacyFormat format: from.style)
        {
            switch(format)
            {
                case BOLD: to.setBold(true); break;
                case ITALIC: to.setItalic(true); break;
                case STRIKE: to.setStrikethrough(true); break;
                case MAGIC: to.setObfuscated(true); break;
                case UNDERLINE: to.setUnderlined(true); break;
            }
        }

        if(from.hover != null)
            to.setHoverEvent(toSpigot(from.hover));

        if(from.click != null)
            to.setClickEvent(toSpigot(from.click));
    }
}
