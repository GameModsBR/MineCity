package br.com.gamemods.minecity.forge.mc_1_10_2;

import br.com.gamemods.minecity.api.command.CommandSender;
import br.com.gamemods.minecity.api.world.BlockPos;
import br.com.gamemods.minecity.forge.base.MineCityForge;
import br.com.gamemods.minecity.forge.base.accessors.ICommander;
import br.com.gamemods.minecity.forge.base.accessors.IEntityPlayerMP;
import br.com.gamemods.minecity.forge.base.command.ForgeCommandSender;
import br.com.gamemods.minecity.forge.mc_1_10_2.command.FrostPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public class MineCityFrost extends MineCityForge
{
    @Override
    protected FrostPlayer createPlayer(IEntityPlayerMP player)
    {
        return new FrostPlayer(this, player);
    }

    @Override
    protected CommandSender createSender(ICommander sender)
    {
        return new ForgeCommandSender<>(this, sender);
    }

    @Override
    public FrostPlayer player(EntityPlayer player)
    {
        return (FrostPlayer) super.player(player);
    }

    public BlockPos block(World world, Vec3i pos)
    {
        return new BlockPos(world(world), pos.getX(), pos.getY(), pos.getZ());
    }

    public BlockPos block(BlockPos base, Vec3i pos)
    {
        return new BlockPos(base, pos.getX(), pos.getY(), pos.getZ());
    }
}
