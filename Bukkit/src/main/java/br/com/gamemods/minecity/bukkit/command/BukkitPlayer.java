package br.com.gamemods.minecity.bukkit.command;

import br.com.gamemods.minecity.MineCity;
import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.command.*;
import br.com.gamemods.minecity.api.unchecked.UFunction;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.api.world.EntityPos;
import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.bukkit.MineCityBukkit;
import br.com.gamemods.minecity.structure.ClaimedChunk;
import br.com.gamemods.minecity.structure.DisplayedSelection;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.*;

public class BukkitPlayer extends BukkitLocatableSender<Player>
{
    public final PlayerID playerId;
    private BukkitSelection selection;
    private UFunction<CommandSender, CommandResult<?>> confirmAction;
    private String confirmCode;

    public BukkitPlayer(MineCityBukkit plugin, Player player)
    {
        super(plugin, player);
        this.playerId = new PlayerID(player.getUniqueId(), player.getName());
    }

    @Override
    public String confirm(@NotNull UFunction<CommandSender, CommandResult<?>> onConfirm)
    {
        String code = confirmCode = new BigInteger(28, MineCity.RANDOM).toString(32).toUpperCase();
        confirmAction = onConfirm;
        sender.getServer().getScheduler().runTaskLater(plugin.plugin, ()->{
            if(code.equals(confirmCode))
            {
                confirmCode = null;
                confirmAction = null;
                send(CONFIRM_EXPIRED);
            }
        }, 20*30);
        return null;
    }

    @Override
    public CommandResult<CommandResult<?>> confirm(String code) throws Exception
    {
        if(!confirmCode.equals(code.toUpperCase()))
            return CommandResult.failed();

        UFunction<CommandSender, CommandResult<?>> action = this.confirmAction;
        confirmCode = null;
        confirmAction = null;
        return new CommandResult<>(null, action.apply(this), true);
    }

    @NotNull
    @Override
    public BukkitSelection getSelection(@NotNull WorldDim world)
    {
        if(selection == null || !selection.world.equals(world))
            selection = new BukkitSelection(world);
        return selection;
    }

    @Override
    public void giveSelectionTool()
    {
        ItemStack stack = new ItemStack(Material.WOOD_HOE);
        ItemMeta meta = stack.getItemMeta();
        MessageTransformer transformer = plugin.mineCity.messageTransformer;
        meta.setDisplayName(transformer.toLegacy(new Message("tool.selection.title", LegacyFormat.AQUA+"Selection Tool")));
        meta.setLore(Arrays.asList(transformer.toMultilineLegacy(
                new Message("","<white>${lore}</white>", new Object[]
                        {"lore", new Message("tool.selection.lore","Selects an area in the world")}
                ))
        ));
        stack.setItemMeta(meta);
        HashMap<Integer, ItemStack> rejected = sender.getInventory().addItem(stack);
        if(!rejected.isEmpty())
            send(CommandFunction.messageFailed(new Message(
                    "action.give.tool.inventory-full",
                    "You haven't received the tool because your inventory is full."
            )));
        sender.updateInventory();
    }

    @Override
    public EntityPos getPosition()
    {
        return plugin.entityPos(sender.getLocation());
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

    public class BukkitSelection extends DisplayedSelection<Material>
    {

        protected BukkitSelection(@NotNull WorldDim world)
        {
            super(world);
            cornerA = Material.GLOWSTONE;
            cornerB = Material.REDSTONE_LAMP_ON;
            corners = Material.BURNING_FURNACE;
            linesA = Material.GOLD_BLOCK;
            linesB = Material.LAPIS_BLOCK;
            lines = Material.GLASS;
            extension = Material.GLOWSTONE;
        }

        @Override
        protected void send(Map<BlockPos, Material> last)
        {
            plugin.callSyncMethod(()->{
                World worldObj = sender.getWorld();
                if(!plugin.world(worldObj).equals(world))
                    return;

                Set<BlockPos> removed = last.keySet();
                removed.removeAll(display.keySet());
                for(BlockPos p: removed)
                {
                    Optional<ClaimedChunk> chunk = plugin.mineCity.getChunk(p.getChunk());
                    if(chunk.isPresent())
                    {
                        // This would load the chunk when it's unloaded,
                        // that's why we check if it's loaded before
                        Block block = worldObj.getBlockAt(p.x, p.y, p.z);
                        //noinspection deprecation
                        sender.sendBlockChange(new Location(worldObj, p.x, p.y, p.z), block.getType(), block.getData());
                    }
                }

                for(Map.Entry<BlockPos, Material> entry: display.entrySet())
                {
                    BlockPos p = entry.getKey();
                    Optional<ClaimedChunk> chunk = plugin.mineCity.getChunk(p.getChunk());
                    if(chunk.isPresent())
                    {
                        //noinspection deprecation
                        sender.sendBlockChange(new Location(worldObj, p.x, p.y, p.z), entry.getValue(), (byte) 0);
                    }
                }
            });
        }
    }
}
