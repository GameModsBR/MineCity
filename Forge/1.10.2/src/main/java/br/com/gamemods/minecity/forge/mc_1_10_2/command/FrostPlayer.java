package br.com.gamemods.minecity.forge.mc_1_10_2.command;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.command.CommandSender;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.api.world.EntityPos;
import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.forge.base.command.IForgePlayer;
import br.com.gamemods.minecity.forge.mc_1_10_2.MineCityFrost;
import br.com.gamemods.minecity.structure.DisplayedSelection;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class FrostPlayer extends FrostCommandSender<EntityPlayerMP> implements IForgePlayer
{
    private final PlayerID playerId;

    public FrostPlayer(MineCityFrost mod, EntityPlayerMP sender)
    {
        super(mod, sender);
        playerId = new PlayerID(sender.getUniqueID(), sender.getName());
    }

    @NotNull
    @Override
    public String getName()
    {
        return sender.getName();
    }

    @Override
    public void tick()
    {

    }

    @Override
    public Direction getCardinalDirection()
    {
        return Direction.cardinal8.get(MathHelper.floor_double((double)((sender.rotationYaw + 180.0F) * 8.0F / 360.0F) + 0.5D) & 7);
    }

    @Override
    public EntityPos getPosition()
    {
        return new EntityPos(mod.world(sender.worldObj), sender.posX, sender.posY, sender.posZ, sender.rotationPitch, sender.rotationYaw);
    }

    @NotNull
    @Override
    public DisplayedSelection<Block> getSelection(@NotNull WorldDim world)
    {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public UUID getUniqueId()
    {
        return sender.getUniqueID();
    }

    @NotNull
    @Override
    public Type getType()
    {
        return Type.PLAYER;
    }

    @Override
    public boolean isPlayer()
    {
        return true;
    }

    @Override
    public PlayerID getPlayerId()
    {
        return playerId;
    }

    @Nullable
    @Override
    public CommandSender getCommandSender()
    {
        return this;
    }

    @Override
    public boolean kick(Message message)
    {
        sender.connection.kickPlayerFromServer(mod.transformer.toLegacy(message));
        return true;
    }
}
