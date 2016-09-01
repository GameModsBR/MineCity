package br.com.gamemods.minecity.forge.mc_1_10_2.event;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

/**
 * @deprecated Use {@link PlayerInteractEvent.EntityInteractSpecific} instead
 */
@Deprecated
@Cancelable
public class PlayerInteractEntityPreciseEvent extends EntityEvent
{
    public final EntityPlayer player;
    public final Vec3d pos;
    public final ItemStack stack;
    public final EnumHand hand;

    public PlayerInteractEntityPreciseEvent(Entity interacted, EntityPlayer player, Vec3d pos, ItemStack stack, EnumHand hand)
    {
        super(interacted);
        this.player = player;
        this.pos = pos;
        this.stack = stack;
        this.hand = hand;
    }
}
