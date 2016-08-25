package br.com.gamemods.minecity.forge.mc_1_7_10.command;

import br.com.gamemods.minecity.api.command.LegacyFormat;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.forge.base.accessors.ICommander;
import br.com.gamemods.minecity.forge.base.command.ForgeTransformer;
import br.com.gamemods.minecity.forge.mc_1_7_10.SevenUtil;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SevenTransformer extends ForgeTransformer
{
    @Override
    public void send(Message message, ICommander commander)
    {
        for(IChatComponent msg: toMultilineForge(message))
            commander.getCommandSender().addChatMessage(msg);
    }

    @Override
    public void send(Message[] messages, ICommander commander)
    {
        for(Message message: messages)
            send(message, commander);
    }

    public IChatComponent toForge(Message message)
    {
        Component component = toComponent(message);
        component.apply(Locale.getDefault(), message.getArgs());
        return toForge(component);
    }

    public NBTTagList toLore(Message message)
    {
        String[] lines = toMultilineLegacy(message);

        NBTTagList tagList = new NBTTagList();
        for(String line : lines)
            tagList.appendTag(new NBTTagString(line));

        return tagList;
    }

    public IChatComponent[] toMultilineForge(Message message)
    {
        Component component = toComponent(message);
        component.apply(Locale.getDefault(), message.getArgs());
        List<Component> split = new ArrayList<>(1);
        split.add(component);
        component.splitNewLines(split);
        return split.stream().map(this::toForge).toArray(IChatComponent[]::new);
    }

    protected IChatComponent toForge(Component component)
    {
        IChatComponent chat;
        if(component instanceof TextComponent)
            chat = toForge((TextComponent) component);
        else
            return SevenUtil.chatComponentFromLegacyText(component.toString());

        component.extra.forEach((c)-> chat.appendSibling(toForge(c)));
        return chat;
    }

    protected IChatComponent toForge(TextComponent component)
    {
        ChatComponentText chat = new ChatComponentText(component.literalValue());
        applyProperties(component, chat);
        return chat;
    }

    protected ClickEvent toForge(Click click)
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

    protected HoverEvent toForge(Hover hover)
    {
        if(hover instanceof HoverMessage)
            return new HoverEvent(HoverEvent.Action.SHOW_TEXT, toForge((Component) ((HoverMessage) hover).message));
        if(hover instanceof HoverAchievement)
            return new HoverEvent(HoverEvent.Action.SHOW_ACHIEVEMENT, new ChatComponentText(((HoverAchievement) hover).id));

        return null;
    }


    protected void applyProperties(Component from, IChatComponent to)
    {
        boolean colorized = from.color != null && from.color != LegacyFormat.RESET;
        if((from.color == null || from.color == LegacyFormat.RESET) && from.style.isEmpty() && from.hover == null && from.click == null)
            return;

        ChatStyle style = new ChatStyle();
        if(colorized)
            style.setColor(from.color.server());

        for(LegacyFormat format : from.style)
        {
            switch(format)
            {
                case BOLD: style.setBold(true); break;
                case ITALIC: style.setItalic(true); break;
                case STRIKE: style.setStrikethrough(true); break;
                case MAGIC: style.setObfuscated(true); break;
                case UNDERLINE: style.setUnderlined(true); break;
            }
        }

        if(from.hover != null)
            style.setChatHoverEvent(toForge(from.hover));

        if(from.click != null)
            style.setChatClickEvent(toForge(from.click));

        to.setChatStyle(style);
    }
}
