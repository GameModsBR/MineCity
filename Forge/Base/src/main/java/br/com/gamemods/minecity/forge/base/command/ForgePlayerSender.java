package br.com.gamemods.minecity.forge.base.command;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.command.CommandResult;
import br.com.gamemods.minecity.api.command.CommandSender;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.unchecked.UFunction;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.api.world.EntityPos;
import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.IEntityPlayerMP;
import br.com.gamemods.minecity.structure.DisplayedSelection;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public abstract class ForgePlayerSender<P extends IEntityPlayerMP, F extends MineCityForge> extends ForgeCommandSender<P, F>
{
    public final PlayerID id;
    private UFunction<CommandSender, CommandResult<?>> confirmAction;
    private String confirmCode;
    private short confirmExpires;
    private ForgeSelection<?> selection;
    private short hideSelectionTimer = 0;
    private short clearSelectionTimer = 0;

    public ForgePlayerSender(F mod, P sender)
    {
        super(mod, sender);
        this.id = createId(sender);
    }

    protected PlayerID createId(P player)
    {
        return new PlayerID(sender.getUniqueID(), sender.getName());
    }

    public void tick()
    {
        selectionTimer();
        tickConfirm();
    }

    public void selectionTimer()
    {
        if(hideSelectionTimer > 0)
        {
            if(--hideSelectionTimer == 0)
                selection.hide();
        }

        if(clearSelectionTimer > 0)
        {
            if(--clearSelectionTimer == 0)
                selection.clear();
        }
    }

    public void tickConfirm()
    {
        if(confirmExpires > 0)
        {
            confirmExpires--;
            if(confirmExpires == 0)
            {
                confirmCode = null;
                confirmAction = null;
                send(CONFIRM_EXPIRED);
            }
        }
    }

    @Override
    public abstract void giveSelectionTool();

    @Nullable
    @Override
    public abstract Message teleport(@NotNull BlockPos pos);

    @Nullable
    @Override
    public abstract Message teleport(@NotNull EntityPos pos);

    public ForgeSelection<?> getSelection()
    {
        return selection;
    }

    @Override
    public boolean isPlayer()
    {
        return true;
    }

    @Override
    public PlayerID getPlayerId()
    {
        return id;
    }

    public abstract boolean isOp();

    @Override
    public boolean hasPermission(String perm)
    {
        //TODO Mini permission system for forge and attempt to integrate to Cauldron or ForgeEssentials
        return isOp() || !perm.contains("reload") && !perm.contains("nature.deny") && !perm.contains("nature.allow") && !perm.contains("bypass");
    }

    @Override
    public EntityPos getPosition()
    {
        return sender.getEntityPos(mod);
    }

    @Override
    public Direction getCardinalDirection()
    {
        return sender.getCardinalDirection();
    }

    @Override
    public boolean isConfirmPending()
    {
        return confirmExpires > 0;
    }

    @Override
    public String confirm(@NotNull UFunction<CommandSender, CommandResult<?>> onConfirm)
    {
        confirmExpires = 20*30;
        confirmCode = new BigInteger(28, MineCity.RANDOM).toString(32).toUpperCase();
        confirmAction = onConfirm;
        return confirmCode;
    }

    @Override
    public CommandResult<CommandResult<?>> confirm(String code) throws ExecutionException
    {
        if(confirmExpires == 0 || !confirmCode.equals(code.toUpperCase()))
            return CommandResult.failed();

        UFunction<CommandSender, CommandResult<?>> action = this.confirmAction;
        confirmExpires = 0;
        confirmCode = null;
        confirmAction = null;
        try
        {
            return new CommandResult<>(null, action.apply(this), true);
        }
        catch(Exception e)
        {
            throw new ExecutionException(e);
        }
    }

    @NotNull
    @Override
    public ForgeSelection<?> getSelection(@NotNull WorldDim world)
    {
        if(selection == null || !selection.world.equals(world))
            selection = createSelection(world);
        return selection;
    }

    @NotNull
    public abstract ForgeSelection<?> createSelection(@NotNull WorldDim world);

    public abstract void sendBlock(int x, int y, int z);

    /**
     * @throws ClassCastException if the block param type is not valid
     */
    public abstract void sendFakeBlock(int x, int y, int z, Object block);

    public class ForgeSelection<B> extends DisplayedSelection<B>
    {
        public ForgeSelection(@NotNull WorldDim world)
        {
            super(world);
        }

        @Override
        public void updateDisplay()
        {
            super.updateDisplay();
            hideSelectionTimer = 60*20;
            clearSelectionTimer = 5*60*20;
        }

        @Override
        protected void send(Map<BlockPos, B> last)
        {
            BlockPos pos = getPosition().getBlock();
            display.remove(pos);
            display.remove(pos.add(Direction.UP));
            mod.callSyncMethod(() ->
            {
                if(!mod.world(sender.getIWorld()).equals(world))
                    return;

                Set<BlockPos> removed = last.keySet();
                removed.removeAll(display.keySet());
                for(BlockPos p : removed)
                {
                    Chunk chunk = mod.chunk(p.getChunk());
                    if(chunk != null)
                    {
                        // This method loads the chunk when it's unloaded,
                        // that's why we check if it's loaded before
                        sendBlock(p.x, p.y, p.z);
                    }
                }

                for(Map.Entry<BlockPos, B> entry : display.entrySet())
                {
                    BlockPos p = entry.getKey();
                    Chunk chunk = mod.chunk(p.getChunk());
                    if(chunk != null)
                    {
                        sendFakeBlock(p.x, p.y, p.z, entry.getValue());
                    }
                }
            });
        }
    }
}
