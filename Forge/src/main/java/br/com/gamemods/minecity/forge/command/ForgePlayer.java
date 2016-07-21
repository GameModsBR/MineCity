package br.com.gamemods.minecity.forge.command;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.forge.MineCityForgeMod;
import net.minecraft.entity.player.EntityPlayer;

public class ForgePlayer extends ForgeCommandSender<EntityPlayer>
{
    public ForgePlayer(MineCityForgeMod mod, EntityPlayer player)
    {
        super(mod, player);
    }

    @Override
    public BlockPos getPosition()
    {
        return new BlockPos(mod.world(sender.worldObj), (int)sender.posX, (int)sender.posY, (int)sender.posZ);
    }

    @Override
    public boolean isPlayer()
    {
        return true;
    }

    @Override
    public PlayerID getPlayerId()
    {
        return new PlayerID(sender.getUniqueID(), sender.getCommandSenderName());
    }
}
