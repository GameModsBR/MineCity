package br.com.gamemods.minecity.forge.mc_1_7_10.command;

import br.com.gamemods.minecity.api.PlayerID;
import br.com.gamemods.minecity.api.command.CommandFunction;
import br.com.gamemods.minecity.api.command.LegacyFormat;
import br.com.gamemods.minecity.api.command.Message;
import br.com.gamemods.minecity.api.world.WorldDim;
import br.com.gamemods.minecity.forge.base.accessors.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.command.ForgePlayerSender;
import br.com.gamemods.minecity.forge.mc_1_7_10.MineCitySeven;
import br.com.gamemods.minecity.forge.mc_1_7_10.accessors.SevenBlock;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagByte;
import org.jetbrains.annotations.NotNull;

public class SevenPlayerSender extends ForgePlayerSender<IEntityPlayerMP, MineCitySeven>
{
    public SevenPlayerSender(MineCitySeven mod, IEntityPlayerMP sender)
    {
        super(mod, sender);
    }

    @Override
    protected PlayerID createId(IEntityPlayerMP player)
    {
        return new PlayerID(player.getUniqueID(), player.getName());
    }

    @Override
    public void giveSelectionTool()
    {
        ItemStack stack = new ItemStack(Items.wooden_hoe);
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
    public ForgeSelection<SevenBlock> createSelection(@NotNull WorldDim world)
    {
        ForgeSelection<SevenBlock> selection = new ForgeSelection<>(world);
        selection.cornerA = (SevenBlock) Blocks.glowstone;
        selection.cornerB = (SevenBlock) Blocks.lit_redstone_lamp;
        selection.corners = (SevenBlock) Blocks.lit_furnace;
        selection.linesA = (SevenBlock) Blocks.gold_block;
        selection.linesB = (SevenBlock) Blocks.lapis_block;
        selection.lines = (SevenBlock) Blocks.sponge;
        selection.extension = (SevenBlock) Blocks.glowstone;
        return selection;
    }
}
