package br.com.gamemods.minecity.forge.base.command;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.command.*;
import br.com.gamemods.minecity.api.unchecked.UFunction;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.api.world.EntityPos;
import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.block.IState;
import br.com.gamemods.minecity.forge.base.accessors.entity.base.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.world.IChunk;
import br.com.gamemods.minecity.structure.DisplayedSelection;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagByte;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class ForgePlayerSender<P extends IEntityPlayerMP, F extends MineCityForge> extends ForgeCommandSender<P, F>
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
        this.id = new PlayerID(sender.getUniqueID(), sender.getName());
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

    @Nullable
    @Override
    public Message teleport(@NotNull BlockPos pos)
    {
        return sender.teleport(mod, pos);
    }

    @Nullable
    @Override
    public Message teleport(@NotNull EntityPos pos)
    {
        return sender.teleport(mod, pos);
    }

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

    @Override
    public boolean isOp()
    {
        return mod.server.getIPlayerList().isOp(sender.getGameProfile());
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

    @Override
    public void giveSelectionTool()
    {
        ItemStack stack = new ItemStack(mod.selectionTool);
        stack.setTagInfo("MineCity", new NBTTagByte((byte)1));
        stack.setStackDisplayName(mod.transformer.toLegacy(new Message("tool.selection.title", LegacyFormat.AQUA+"Selection Tool")));
        //stack.setTagInfo("Lore", mod.transformer.toLore(new Message("tool.selection.lore", "Selects an area in the world")));
        if(!sender.getForgeEntity().inventory.addItemStackToInventory(stack))
            send(CommandFunction.messageFailed(new Message(
                    "action.give.tool.inventory-full",
                    "You haven't received the tool because your inventory is full."
            )));
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
    public ForgeSelection<IState> createSelection(@NotNull WorldDim world)
    {
        ForgeSelection<IState> selection = new ForgeSelection<>(world);
        mod.selectionPallet.accept(selection);
        return selection;
    }

    public class ForgeSelection<B extends IState> extends DisplayedSelection<B>
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
                    IChunk chunk = mod.chunk(p.getChunk());
                    if(chunk != null)
                    {
                        // This method loads the chunk when it's unloaded,
                        // that's why we check if it's loaded before
                        sender.sendBlock(p.x, p.y, p.z);
                    }
                }

                for(Map.Entry<BlockPos, B> entry : display.entrySet())
                {
                    BlockPos p = entry.getKey();
                    IChunk chunk = mod.chunk(p.getChunk());
                    if(chunk != null)
                    {
                        sender.sendFakeBlock(p.x, p.y, p.z, entry.getValue());
                    }
                }
            });
        }
    }
}
