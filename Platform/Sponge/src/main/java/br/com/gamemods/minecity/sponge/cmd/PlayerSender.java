package br.com.gamemods.minecity.sponge.cmd;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.command.*;
import br.com.gamemods.minecity.api.unchecked.UFunction;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.Direction;
import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.sponge.MineCitySponge;
import br.com.gamemods.minecity.sponge.data.manipulator.boxed.ItemToolManipulator;
import br.com.gamemods.minecity.structure.DisplayedSelection;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.World;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class PlayerSender extends LivingSource<Player, Player>
{
    private boolean adminMode;
    private boolean autoClaim;
    private SpongeSelection selection;
    private UFunction<CommandSender, CommandResult<?>> confirmAction;
    private String confirmCode;

    public PlayerSender(MineCitySponge server, Player source)
    {
        super(server, source, source);
        server.logger.info("New PlayerSender instance: "+this);
    }

    @Override
    public boolean isPlayer()
    {
        return true;
    }

    @Override
    public PlayerID getPlayerId()
    {
        return PlayerID.get(subject.getUniqueId(), subject.getName());
    }

    @NotNull
    @Override
    public PlayerID identity()
    {
        return getPlayerId();
    }

    @Override
    public boolean isConfirmPending()
    {
        return confirmCode != null;
    }

    @Override
    public String confirm(@NotNull UFunction<CommandSender, CommandResult<?>> onConfirm)
    {
        String code = confirmCode = new BigInteger(28, MineCity.RANDOM).toString(32).toUpperCase();
        confirmAction = onConfirm;
        Task.builder().delay(30, TimeUnit.SECONDS).execute(()-> {
            if(code.equals(confirmCode))
            {
                confirmCode = null;
                confirmAction = null;
                send(CONFIRM_EXPIRED);
            }
        }).submit(server.plugin);
        return code;
    }

    @Override
    public CommandResult<CommandResult<?>> confirm(String code) throws ExecutionException
    {
        if(!confirmCode.equals(code.toUpperCase()))
            return CommandResult.failed();

        UFunction<CommandSender, CommandResult<?>> action = this.confirmAction;
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
    public DisplayedSelection<BlockState> getSelection(@NotNull WorldDim world)
    {
        if(selection == null || !selection.world.equals(world))
            selection = new SpongeSelection(world);
        return selection;
    }

    @Override
    public void giveSelectionTool()
    {
        ItemStack stack = ItemStack.of(ItemTypes.WOODEN_HOE, 1);
        SpongeTransformer transformer = server.transformer;
        DataTransactionResult tool = stack.offer(new ItemToolManipulator(1));
        if(!tool.isSuccessful())
            throw new UnsupportedOperationException("Failed to offer ITEM_TOOL to the stack. "+tool);

        stack.offer(Keys.DISPLAY_NAME, transformer.toText(new Message("tool.selection.title", LegacyFormat.AQUA+"Selection Tool")));
        stack.offer(Keys.ITEM_LORE, Arrays.asList(transformer.toMultilineText(
                new Message("","<msg><white>${lore}</white></msg>", new Object[]
                        {"lore", new Message("tool.selection.lore","Selects an area in the world")}
                ))
        ));

        InventoryTransactionResult result = subject.getInventory().offer(stack);
        if(result.getType() != InventoryTransactionResult.Type.SUCCESS)
            send(CommandFunction.messageFailed(new Message(
                    "action.give.tool.inventory-full",
                    "You haven't received the tool because your inventory is full."
            )));
    }

    @Override
    public void toggleAutoClaim()
    {
        autoClaim = !autoClaim;
    }

    @Override
    public boolean getAutoClaim()
    {
        return autoClaim;
    }

    @Override
    public void toggleAdminMode()
    {
        adminMode = !adminMode;
    }

    @Override
    public boolean isAdminMode()
    {
        if(adminMode)
        {
            send(new Message("mode.adm.notification", "The Admin Mode is enabled and has affected a check result"));
            return true;
        }

        return false;
    }

    public class SpongeSelection extends DisplayedSelection<BlockState>
    {
        private Task autoHide;
        private Task autoClear;
        protected SpongeSelection(@NotNull WorldDim world)
        {
            super(world);
            cornerA = BlockTypes.GLOWSTONE.getDefaultState();
            cornerB = BlockTypes.LIT_REDSTONE_LAMP.getDefaultState();
            corners = BlockTypes.SEA_LANTERN.getDefaultState();
            linesA = BlockTypes.GOLD_BLOCK.getDefaultState();
            linesB = BlockTypes.LAPIS_BLOCK.getDefaultState();
            lines = BlockTypes.PRISMARINE.getDefaultState();
            extension = BlockTypes.GLOWSTONE.getDefaultState();
        }

        @Override
        public void updateDisplay()
        {
            super.updateDisplay();
            if(autoHide != null)
                autoHide.cancel();
            if(autoClear != null)
                autoClear.cancel();

            autoHide = Task.builder().execute(this::hide).delay(60, TimeUnit.SECONDS).submit(server.plugin);
            autoClear = Task.builder().execute(this::clear).delay(5, TimeUnit.MINUTES).submit(server.plugin);
        }

        @Override
        protected void send(Map<BlockPos, BlockState> last)
        {
            BlockPos pos = getPosition().getBlock();
            display.remove(pos);
            display.remove(pos.add(Direction.UP));
            Task.builder().execute(()->{
                World worldObj = subject.getWorld();
                if(!server.world(worldObj).equals(world))
                    return;

                Set<BlockPos> removed = last.keySet();
                removed.removeAll(display.keySet());
                for(BlockPos p: removed)
                {
                    // This would load the chunk when it's unloaded,
                    // that's why we check if it's loaded before
                    server.mineCity.getChunk(p.getChunk())
                            .ifPresent(c -> subject.sendBlockChange(p.x, p.y, p.z, worldObj.getBlock(p.x, p.y, p.z)));
                }

                for(Map.Entry<BlockPos, BlockState> entry: display.entrySet())
                {
                    BlockPos p = entry.getKey();
                    server.mineCity.getChunk(p.getChunk())
                            .ifPresent(c -> subject.sendBlockChange(p.x, p.y, p.z, entry.getValue()));
                }
            }).submit(server.plugin);
        }
    }
}
