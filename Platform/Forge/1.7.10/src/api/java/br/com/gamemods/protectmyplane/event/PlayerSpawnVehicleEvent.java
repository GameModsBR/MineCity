package br.com.gamemods.protectmyplane.event;

import cpw.mods.fml.common.eventhandler.Cancelable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;

@Cancelable
public class PlayerSpawnVehicleEvent extends PlayerEvent
{
    public final ItemStack stack;
    public final int x, y, z;

    public PlayerSpawnVehicleEvent(EntityPlayer player, ItemStack stack, int x, int y, int z)
    {
        super(player);
        this.stack = stack;
        this.x = x;
        this.y = y;
        this.z = z;
    }
}