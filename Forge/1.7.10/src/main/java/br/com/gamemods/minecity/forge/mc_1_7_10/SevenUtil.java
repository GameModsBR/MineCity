package br.com.gamemods.minecity.forge.mc_1_7_10;

import br.com.gamemods.minecity.api.command.LegacyFormat;
import br.com.gamemods.minecity.api.world.Direction;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.util.ForgeDirection;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class SevenUtil
{
    /**
     * Maps chat formatting by it's code
     */
    private static final Map<Character, EnumChatFormatting> formattingMap = new HashMap<>(22);
    static
    {
        for(EnumChatFormatting formatting : EnumChatFormatting.values())
            formattingMap.put(formatting.getFormattingCode(), formatting);
    }

    @NotNull
    public static ForgeDirection toForgeDirection(Direction direction)
    {
        switch(direction)
        {
            case NORTH: return ForgeDirection.NORTH;
            case SOUTH: return ForgeDirection.SOUTH;
            case EAST: return ForgeDirection.EAST;
            case WEST: return ForgeDirection.WEST;
            case UP: return ForgeDirection.UP;
            case DOWN: return ForgeDirection.DOWN;
            default: return ForgeDirection.UNKNOWN;
        }
    }

    /**
     * Parses a legacy text
     *
     * @param message A formatted legacy text
     * @return The parsed text
     */
    public static ChatComponentText chatComponentFromLegacyText(String message)
    {
        ChatComponentText base;
        String[] parts = message.split(Character.toString(LegacyFormat.MARK));
        if(parts.length == 1)
            return new ChatComponentText(message);

        base = new ChatComponentText(parts[0]);

        ChatStyle chatStyle = new ChatStyle();
        for(int i = 1; i < parts.length; i++)
        {
            String current = parts[i];
            char code = current.charAt(0);
            String text = current.substring(1);

            if(code >= '0' && code <= '9' || code >= 'a' && code <= 'f' || code == 'r')
            {
                chatStyle = new ChatStyle();
                chatStyle.setColor(formattingMap.get(code));
            }
            else
            {
                chatStyle = chatStyle.createDeepCopy();
                switch(code)
                {
                    case 'k': chatStyle.setObfuscated(true); break;
                    case 'l': chatStyle.setBold(true); break;
                    case 'm': chatStyle.setStrikethrough(true); break;
                    case 'n': chatStyle.setUnderlined(true); break;
                    case 'o': chatStyle.setItalic(true); break;
                }
            }

            base.appendSibling(new ChatComponentText(text).setChatStyle(chatStyle));
        }

        return base;
    }
}
