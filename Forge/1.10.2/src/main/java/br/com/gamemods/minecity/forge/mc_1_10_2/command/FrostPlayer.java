package br.com.gamemods.minecity.forge.mc_1_10_2.command;

import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.forge.base.command.ForgePlayer;
import br.com.gamemods.minecity.forge.mc_1_10_2.MineCityFrost;
import br.com.gamemods.minecity.forge.mc_1_10_2.protection.ForstMovementMonitor;
import br.com.gamemods.minecity.forge.mc_1_10_2.protection.FrostMovementListener;
import br.com.gamemods.minecity.protection.MovementMonitor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketTitle;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

public class FrostPlayer extends ForgePlayer<MineCityFrost, EntityPlayerMP, FrostPlayerSender, Entity>
    implements FrostMovementListener
{
    public FrostPlayer(MineCityFrost mod, EntityPlayerMP sender)
    {
        super(new FrostPlayerSender(mod, sender));
    }

    @Override
    protected MovementMonitor<Entity, MineCityFrost> createMonitor()
    {
        return new ForstMovementMonitor(mod, player, mod.block(player), this);
    }

    @Override
    public boolean kick(Message message)
    {
        cmd.sender.connection.kickPlayerFromServer(cmd.mod.transformer.toLegacy(message));
        return true;
    }

    @Override
    public void sendTitle(Message title, Message subtitle)
    {
        cmd.sendPacket(new SPacketTitle(SPacketTitle.Type.CLEAR, new TextComponentString("")));
        if(title != null)
        {
            cmd.sendPacket(new SPacketTitle(SPacketTitle.Type.TITLE, ITextComponent.Serializer.jsonToComponent(
                    cmd.mod.transformer.toJson(title)
            )));
        }
        else
        {
            cmd.sendPacket(new SPacketTitle(SPacketTitle.Type.TITLE, new TextComponentString("")));
        }

        if(subtitle != null)
        {
            cmd.sendPacket(new SPacketTitle(SPacketTitle.Type.SUBTITLE, ITextComponent.Serializer.jsonToComponent(
                    cmd.mod.transformer.toJson(subtitle)
            )));
        }
    }
}
