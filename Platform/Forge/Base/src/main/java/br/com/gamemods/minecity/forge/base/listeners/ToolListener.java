package br.com.gamemods.minecity.forge.base.listeners;

import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.structure.DisplayedSelection;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ToolListener
{
    private MineCityForge forge;

    public ToolListener(MineCityForge forge)
    {
        this.forge = forge;
    }

    public boolean onPlayerInteract(EntityPlayer player, ItemStack heldItem, World world, int x, int y, int z, boolean left)
    {
        if(heldItem == null || !heldItem.hasTagCompound() || heldItem.getTagCompound().getByte("MineCity") != 1)
            return false;

        BlockPos block = new BlockPos(forge.world(world), x, y, z);
        DisplayedSelection<?> selection = forge.player(player).getCommandSender().getSelection(forge.world(world));

        if(player.isSneaking())
        {
            if(left)
                selection.b = block;
            else
                selection.a = block;

            selection.normalize();
            forge.runAsynchronously(selection::updateDisplay);
        }
        else
            forge.runAsynchronously(()->selection.select(block));

        return true;
    }
}
