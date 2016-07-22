package br.com.gamemods.minecity.forge.command;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.forge.MineCityForgeMod;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    @Override
    public Direction getCardinalDirection()
    {
        //TODO Do actual implementation
        return Direction.NORTH;
    }

    @Nullable
    @Override
    public Message teleport(@NotNull BlockPos pos)
    {
        WorldDim current = mod.world(sender.worldObj);
        double x = pos.x+0.5, y = pos.y+0.5, z = pos.z+0.5;
        if(current.equals(pos.world))
        {
            sender.mountEntity(null);
            sender.setPositionAndUpdate(x, y, z);
            return null;
        }

        WorldServer worldServer = mod.world(pos.world);
        if(worldServer == null)
            return new Message("action.teleport.world-not-found",
                    "The destiny world ${name} was not found or is not loaded",
                    new Object[]{"name", pos.world.name()}
            );

        sender.mountEntity(null);
        mod.server.getConfigurationManager().transferPlayerToDimension((EntityPlayerMP) sender, pos.world.dim, worldServer.getDefaultTeleporter());
        sender.setPositionAndUpdate(x, y, z);

        return null;
    }
}
