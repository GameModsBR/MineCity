package br.com.gamemods.minecity.forge.mc_1_10_2.command;

import br.com.gamemods.minecity.api.command.CommandFunction;
import br.com.gamemods.minecity.api.command.LegacyFormat;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.forge.base.accessors.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.accessors.IState;
import br.com.gamemods.minecity.forge.base.command.ForgePlayerSender;
import br.com.gamemods.minecity.forge.mc_1_10_2.MineCityFrost;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagByte;
import org.jetbrains.annotations.NotNull;

public class FrostPlayerSender extends ForgePlayerSender<IEntityPlayerMP, MineCityFrost>
{
    public FrostPlayerSender(MineCityFrost mod, IEntityPlayerMP sender)
    {
        super(mod, sender);
    }

    @Override
    public void giveSelectionTool()
    {
        ItemStack stack = new ItemStack(Items.WOODEN_HOE);
        stack.setTagInfo("MineCity", new NBTTagByte((byte)1));
        stack.setStackDisplayName(mod.transformer.toLegacy(new Message("tool.selection.title", LegacyFormat.AQUA+"Selection Tool")));
        //stack.setTagInfo("Lore", mod.transformer.toLore(new Message("tool.selection.lore", "Selects an area in the world")));
        if(!sender.getEntityPlayerMP().inventory.addItemStackToInventory(stack))
            send(CommandFunction.messageFailed(new Message(
                    "action.give.tool.inventory-full",
                    "You haven't received the tool because your inventory is full."
            )));
    }

    @NotNull
    @Override
    public ForgeSelection<IState> createSelection(@NotNull WorldDim world)
    {
        ForgeSelection<IState> selection = new ForgeSelection<>(world);
        selection.cornerA = (IState) Blocks.GLOWSTONE.getDefaultState();
        selection.cornerB = (IState) Blocks.LIT_REDSTONE_LAMP.getDefaultState();
        selection.corners = (IState) Blocks.SEA_LANTERN.getDefaultState();
        selection.linesA = (IState) Blocks.GOLD_BLOCK.getDefaultState();
        selection.linesB = (IState) Blocks.LAPIS_BLOCK.getDefaultState();
        selection.lines = (IState) Blocks.PRISMARINE.getDefaultState();
        selection.extension = (IState) Blocks.GLOWSTONE.getDefaultState();
        return selection;
    }
}
